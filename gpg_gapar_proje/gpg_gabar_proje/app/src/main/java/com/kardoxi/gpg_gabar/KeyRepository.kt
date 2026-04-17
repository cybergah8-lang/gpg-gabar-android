package com.kardoxi.gpg_gabar

import android.content.ContentValues
import android.content.Context
import java.text.Collator
import java.util.Locale

class KeyRepository(context: Context) {
    private val dbHelper = DataKeyDbHelper(context.applicationContext)
    private val appContext = context.applicationContext

    private inline fun <T> withDbRetry(action: () -> T): T {
        return try {
            action()
        } catch (e: Exception) {
            // If DB was deleted/corrupted while in use, reset and retry once
            DataKeyDbHelper.resetShared()
            action()
        }
    }

    fun insertKey(
        name: String,
        armoredPublic: String,
        armoredSecret: String,
    ): Long {
        // Normalize and prevent duplicate names at the repository level as a safety net
        val normName = name.trim()
        if (nameExists(normName)) return -1L
        return withDbRetry {
            val db = dbHelper.writable()
            val values = ContentValues().apply {
                put(DataKeyDbHelper.COL_NAME, normName)
                put(DataKeyDbHelper.COL_PUBLIC_KEY_ARMORED, armoredPublic)
                put(DataKeyDbHelper.COL_SECRET_KEY_ARMORED, armoredSecret)
                put(DataKeyDbHelper.COL_CREATED_AT, System.currentTimeMillis())
            }
            db.insert(DataKeyDbHelper.TABLE_KEYS, null, values)
        }
    }

    fun insertPublicKey(name: String, armoredPublic: String): Long {
        // Store empty string for secret to denote public-only
        return insertKey(name, armoredPublic, "")
    }

    fun listKeyNames(): List<String> {
        val names = withDbRetry {
            val db = dbHelper.readable()
            val projection = arrayOf(DataKeyDbHelper.COL_NAME)
            val cursor = db.query(
                DataKeyDbHelper.TABLE_KEYS,
                projection,
                null,
                null,
                null,
                null,
                DataKeyDbHelper.COL_CREATED_AT + " DESC"
            )
            val n = mutableListOf<String>()
            cursor.use {
                val idx = it.getColumnIndexOrThrow(DataKeyDbHelper.COL_NAME)
                while (it.moveToNext()) {
                    n.add(it.getString(idx))
                }
            }
            n
        }
        // Sort using Turkish alphabetical order
        val collator = Collator.getInstance(Locale("tr", "TR")).apply {
            strength = Collator.PRIMARY
        }
        return names.sortedWith { a, b -> collator.compare(a, b) }
    }

    fun nameExists(name: String): Boolean {
        return withDbRetry {
            val db = dbHelper.readable()
            val projection = arrayOf(DataKeyDbHelper.COL_ID)
            val selection = "LOWER(TRIM(" + DataKeyDbHelper.COL_NAME + ")) = LOWER(TRIM(?))"
            val args = arrayOf(name)
            val cursor = db.query(
                DataKeyDbHelper.TABLE_KEYS,
                projection,
                selection,
                args,
                null,
                null,
                null,
                "1"
            )
            cursor.use { it.moveToFirst() }
        }
    }

    fun getArmoredPublicByName(name: String): String? {
        return withDbRetry {
            val db = dbHelper.readable()
            val projection = arrayOf(DataKeyDbHelper.COL_PUBLIC_KEY_ARMORED)
            val selection = DataKeyDbHelper.COL_NAME + " = ?"
            val args = arrayOf(name)
            val cursor = db.query(
                DataKeyDbHelper.TABLE_KEYS,
                projection,
                selection,
                args,
                null,
                null,
                null,
                "1"
            )
            cursor.use {
                if (it.moveToFirst()) {
                    it.getString(it.getColumnIndexOrThrow(DataKeyDbHelper.COL_PUBLIC_KEY_ARMORED))
                } else null
            }
        }
    }

    fun getArmoredSecretByName(name: String): String? {
        return withDbRetry {
            val db = dbHelper.readable()
            val projection = arrayOf(DataKeyDbHelper.COL_SECRET_KEY_ARMORED)
            val selection = DataKeyDbHelper.COL_NAME + " = ?"
            val args = arrayOf(name)
            val cursor = db.query(
                DataKeyDbHelper.TABLE_KEYS,
                projection,
                selection,
                args,
                null,
                null,
                null,
                "1"
            )
            cursor.use {
                if (it.moveToFirst()) {
                    it.getString(it.getColumnIndexOrThrow(DataKeyDbHelper.COL_SECRET_KEY_ARMORED))
                } else null
            }
        }
    }

    fun hasSecret(name: String): Boolean {
        return withDbRetry {
            val db = dbHelper.readable()
            val projection = arrayOf(DataKeyDbHelper.COL_SECRET_KEY_ARMORED)
            val selection = DataKeyDbHelper.COL_NAME + " = ?"
            val args = arrayOf(name)
            val cursor = db.query(
                DataKeyDbHelper.TABLE_KEYS,
                projection,
                selection,
                args,
                null,
                null,
                null,
                "1"
            )
            cursor.use {
                if (it.moveToFirst()) {
                    val v = it.getString(it.getColumnIndexOrThrow(DataKeyDbHelper.COL_SECRET_KEY_ARMORED))
                    !v.isNullOrEmpty()
                } else false
            }
        }
    }

    fun deleteByName(name: String): Int {
        return withDbRetry {
            val db = dbHelper.writable()
            val where = DataKeyDbHelper.COL_NAME + " = ?"
            val args = arrayOf(name)
            db.delete(DataKeyDbHelper.TABLE_KEYS, where, args)
        }
    }

    fun updateKeysByName(name: String, armoredPublic: String?, armoredSecret: String?): Int {
        val values = ContentValues()
        if (armoredPublic != null) values.put(DataKeyDbHelper.COL_PUBLIC_KEY_ARMORED, armoredPublic)
        if (armoredSecret != null) values.put(DataKeyDbHelper.COL_SECRET_KEY_ARMORED, armoredSecret)
        if (values.size() == 0) return 0
        return withDbRetry {
            val db = dbHelper.writable()
            val where = DataKeyDbHelper.COL_NAME + " = ?"
            val args = arrayOf(name)
            db.update(DataKeyDbHelper.TABLE_KEYS, values, where, args)
        }
    }
}
