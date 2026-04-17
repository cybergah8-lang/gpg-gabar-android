package com.kardoxi.gpg_gabar

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ShareDecryptActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        val action = intent?.action
        val type = intent?.type
        val uri: Uri? = intent?.getParcelableExtra(Intent.EXTRA_STREAM)

        if (action == Intent.ACTION_SEND && type != null && uri != null) {
            try {
                // Ensure our app can read this URI
                grantUriPermission(packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)

                val forward = Intent(this, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    putExtra("share_action", "decrypt")
                    putExtra("share_uri", uri.toString())
                }
                startActivity(forward)
            } catch (e: Exception) {
                Toast.makeText(this, getString(R.string.read_failed_with_reason, e.message ?: ""), Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(this, getString(R.string.please_choose_gpg_file), Toast.LENGTH_SHORT).show()
        }

        finish()
    }
}
