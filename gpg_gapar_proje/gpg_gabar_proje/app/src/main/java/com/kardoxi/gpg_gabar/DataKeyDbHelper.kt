package com.kardoxi.gpg_gabar

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase as PlainSQLite
import net.zetetic.database.sqlcipher.SQLiteDatabase
import net.zetetic.database.sqlcipher.SQLiteOpenHelper
import kotlin.concurrent.thread

class DataKeyDbHelper(private val appContext: Context) : SQLiteOpenHelper(
    appContext,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {
    init {
        // Load native SQLCipher core library (required before any SQLCipher API usage)
        System.loadLibrary("sqlcipher")
        // Perform one-time migration if needed (only once per process)
        if (!migrationDone) {
            synchronized(MigrationLock) {
                if (!migrationDone) {
                    thread(name = "db-migration", isDaemon = true) {
                        migrateIfNeeded(appContext)
                    }
                    migrationDone = true
                }
            }
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS $TABLE_KEYS (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_NAME TEXT NOT NULL,
                $COL_PUBLIC_KEY_ARMORED TEXT NOT NULL,
                $COL_SECRET_KEY_ARMORED TEXT NOT NULL,
                $COL_CREATED_AT INTEGER NOT NULL
            )
            """.trimIndent()
        )
        // Ensure a unique index on name (case-insensitive) to prevent duplicates
        db.execSQL(
            """
            CREATE UNIQUE INDEX IF NOT EXISTS idx_keys_name_nocase
            ON $TABLE_KEYS($COL_NAME COLLATE NOCASE)
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Simple migration: drop and recreate (data loss acceptable for migration step)
        db.execSQL("DROP TABLE IF EXISTS $TABLE_KEYS")
        onCreate(db)
    }

    fun writable(): SQLiteDatabase {
        // Reuse a single encrypted connection per process to avoid repeated open/close overhead
        var db = sharedDb
        if (db != null) return db
        synchronized(DbLock) {
            db = sharedDb
            if (db != null) return db as SQLiteDatabase
            val pass = DbKeyStore.getOrCreatePassphrase(appContext)
            val file = appContext.getDatabasePath(DATABASE_NAME)
            file.parentFile?.mkdirs()
            val opened = SQLiteDatabase.openOrCreateDatabase(file, pass, null, null, null)
            // Initialize schema only once
            if (!schemaInitialized) {
                onCreate(opened)
                schemaInitialized = true
            }
            sharedDb = opened
            return opened
        }
    }

    fun readable(): SQLiteDatabase {
        // For simplicity and to ensure schema availability, open as writable (reused)
        return writable()
    }

    private fun migrateIfNeeded(context: Context) {
        if (DbKeyStore.isEncrypted(context)) return
        val oldFile = context.getDatabasePath(OLD_DATABASE_NAME)
        if (!oldFile.exists()) {
            // Nothing to migrate, just mark encrypted so we don't try again
            DbKeyStore.markEncrypted(context)
            return
        }

        // Read all rows from plaintext DB
        val items = mutableListOf<Triple<String, String, String>>()
        var hasData = false
        try {
            val db = PlainSQLite.openDatabase(oldFile.path, null, PlainSQLite.OPEN_READONLY)
            db.rawQuery("SELECT $COL_NAME, $COL_PUBLIC_KEY_ARMORED, $COL_SECRET_KEY_ARMORED FROM $TABLE_KEYS ORDER BY $COL_CREATED_AT DESC", null)
                .use { c: Cursor ->
                    val idxName = c.getColumnIndexOrThrow(COL_NAME)
                    val idxPub = c.getColumnIndexOrThrow(COL_PUBLIC_KEY_ARMORED)
                    val idxSec = c.getColumnIndexOrThrow(COL_SECRET_KEY_ARMORED)
                    while (c.moveToNext()) {
                        hasData = true
                        items += Triple(c.getString(idxName), c.getString(idxPub), c.getString(idxSec))
                    }
                }
            db.close()
        } catch (_: Exception) {
            // If reading fails, continue to create empty encrypted DB
        }

        // Create encrypted DB and insert copied rows
        kotlin.runCatching {
            val encDb = writable()
            encDb.beginTransaction()
            try {
                if (hasData) {
                    for ((name, pub, sec) in items) {
                        val cv = ContentValues().apply {
                            put(COL_NAME, name)
                            put(COL_PUBLIC_KEY_ARMORED, pub)
                            put(COL_SECRET_KEY_ARMORED, sec)
                            put(COL_CREATED_AT, System.currentTimeMillis())
                        }
                        encDb.insert(TABLE_KEYS, null, cv)
                    }
                }
                encDb.setTransactionSuccessful()
            } finally {
                encDb.endTransaction()
                encDb.close()
            }

            // Remove old plaintext DB and its -wal/-shm if any
            fun delIfExists(path: java.io.File?) { if (path != null && path.exists()) path.delete() }
            delIfExists(oldFile)
            delIfExists(java.io.File(oldFile.parentFile, OLD_DATABASE_NAME + "-journal"))
            delIfExists(java.io.File(oldFile.parentFile, OLD_DATABASE_NAME + "-wal"))
            delIfExists(java.io.File(oldFile.parentFile, OLD_DATABASE_NAME + "-shm"))

            // Mark as encrypted so we don't run again
            DbKeyStore.markEncrypted(context)
        }
    }

    companion object {
        // New encrypted DB filename
        const val DATABASE_NAME = "datakey_enc"
        const val OLD_DATABASE_NAME = "datakey"
        const val DATABASE_VERSION = 2

        const val TABLE_KEYS = "keys"
        const val COL_ID = "id"
        const val COL_NAME = "name"
        const val COL_PUBLIC_KEY_ARMORED = "public_key_armored"
        const val COL_SECRET_KEY_ARMORED = "secret_key_armored"
        const val COL_CREATED_AT = "created_at"

        // Shared connection and guards
        @Volatile
        private var sharedDb: SQLiteDatabase? = null
        @Volatile
        private var schemaInitialized: Boolean = false
        @Volatile
        private var migrationDone: Boolean = false
        private val DbLock = Any()
        private val MigrationLock = Any()

        // Reset the shared DB connection so that after file deletion the next access
        // will reopen and (re)initialize the schema.
        fun resetShared() {
            synchronized(DbLock) {
                try {
                    sharedDb?.close()
                } catch (_: Exception) { }
                sharedDb = null
                schemaInitialized = false
            }
        }
    }
}
