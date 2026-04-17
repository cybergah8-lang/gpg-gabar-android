package com.kardoxi.gpg_gabar

import android.content.Context
import android.content.SharedPreferences

object PassphraseStore {
    private const val PREFS = "pgp_passphrases"
    private const val KEY_ID_PREFIX = "kid_"

    private fun prefs(ctx: Context): SharedPreferences =
        ctx.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun save(ctx: Context, keyName: String, passphrase: String) {
        prefs(ctx).edit().putString(keyName, passphrase).apply()
    }

    fun get(ctx: Context, keyName: String): String? =
        prefs(ctx).getString(keyName, null)

    fun remove(ctx: Context, keyName: String) {
        prefs(ctx).edit().remove(keyName).apply()
    }

    fun saveByKeyId(ctx: Context, keyId: Long, passphrase: String) {
        prefs(ctx).edit().putString(KEY_ID_PREFIX + keyId.toString(), passphrase).apply()
    }

    fun getByKeyId(ctx: Context, keyId: Long): String? =
        prefs(ctx).getString(KEY_ID_PREFIX + keyId.toString(), null)
}
