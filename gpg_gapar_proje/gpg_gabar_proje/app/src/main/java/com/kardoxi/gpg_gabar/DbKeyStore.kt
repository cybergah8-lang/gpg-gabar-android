package com.kardoxi.gpg_gabar

import android.content.Context
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.SecureRandom

object DbKeyStore {
    private const val PREF_NAME = "db_secure_prefs"
    private const val KEY_DB_PASSPHRASE = "db_passphrase"
    private const val KEY_DB_ENCRYPTED_FLAG = "db_encrypted"

    private fun prefs(context: Context) = EncryptedSharedPreferences.create(
        context,
        PREF_NAME,
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun getOrCreatePassphrase(context: Context): ByteArray {
        val p = prefs(context)
        val existing = p.getString(KEY_DB_PASSPHRASE, null)
        if (!existing.isNullOrEmpty()) {
            return Base64.decode(existing, Base64.DEFAULT)
        }
        val rnd = SecureRandom()
        val bytes = ByteArray(32)
        rnd.nextBytes(bytes)
        p.edit().putString(KEY_DB_PASSPHRASE, Base64.encodeToString(bytes, Base64.NO_WRAP)).apply()
        return bytes
    }

    fun isEncrypted(context: Context): Boolean = prefs(context).getBoolean(KEY_DB_ENCRYPTED_FLAG, false)

    fun markEncrypted(context: Context) {
        prefs(context).edit().putBoolean(KEY_DB_ENCRYPTED_FLAG, true).apply()
    }
}
