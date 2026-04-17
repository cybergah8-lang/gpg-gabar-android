package com.kardoxi.gpg_gabar

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ShareActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        val action = intent?.action
        val type = intent?.type

        try {
            if (action == Intent.ACTION_SEND) {
                // Prefer text if provided
                val sharedText: String? = intent.getStringExtra(Intent.EXTRA_TEXT)
                if (!sharedText.isNullOrBlank()) {
                    val looksArmored = sharedText.contains("-----BEGIN PGP MESSAGE-----")
                            || sharedText.contains("-----BEGIN PGP ARMORED FILE-----")
                    val forward = Intent(this, MainActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                        putExtra("share_action", if (looksArmored) "text_decrypt" else "text")
                        putExtra("share_text", sharedText)
                    }
                    startActivity(forward)
                    finish()
                    return
                }

                // Else, handle file stream
                val uri: Uri? = intent.getParcelableExtra(Intent.EXTRA_STREAM)
                if (uri != null) {
                    // Grant read permission to our app
                    grantUriPermission(packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)

                    val name = try {
                        contentResolver.query(uri, null, null, null, null)?.use { c ->
                            val idx = c.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                            if (idx >= 0 && c.moveToFirst()) c.getString(idx) else null
                        }
                    } catch (_: Exception) { null }

                    val ext = name?.substringAfterLast('.', missingDelimiterValue = "")?.lowercase()
                    val mime = try { contentResolver.getType(uri) } catch (_: Exception) { type }
                    val isGpg = (ext == "gpg" || ext == "pgp")
                    val isEncryptedMime = (mime == "application/pgp-encrypted" || mime == "application/pgp-signature")

                    val forward = Intent(this, MainActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        putExtra("share_action", if (isGpg || isEncryptedMime) "decrypt" else "encrypt")
                        putExtra("share_uri", uri.toString())
                    }
                    startActivity(forward)
                    finish()
                    return
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, getString(R.string.error_with_reason, e.message ?: ""), Toast.LENGTH_LONG).show()
        }
        finish()
    }
}
