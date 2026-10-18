package com.streamvault.app.update

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageInstaller
import android.os.Bundle
import android.widget.Toast
import com.streamvault.app.R

/** Explicit, non-exported PendingIntent target. Android supplies the confirmation intent. */
class UpdateInstallResultActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE)
        if (status == PackageInstaller.STATUS_PENDING_USER_ACTION) {
            val confirmation = if (android.os.Build.VERSION.SDK_INT >= 33) {
                intent.getParcelableExtra(Intent.EXTRA_INTENT, Intent::class.java)
            } else {
                androidx.core.content.IntentCompat.getParcelableExtra(intent, Intent.EXTRA_INTENT, Intent::class.java)
            }
            if (confirmation != null) {
                runCatching { startActivity(confirmation) }.onFailure {
                    Toast.makeText(this, R.string.updater_failed, Toast.LENGTH_LONG).show()
                }
            }
        } else {
            getSharedPreferences("update-integrity", MODE_PRIVATE).edit().remove("active-session").apply()
            if (status != PackageInstaller.STATUS_SUCCESS) {
                Toast.makeText(this, R.string.updater_failed, Toast.LENGTH_LONG).show()
            }
        }
        finish()
    }
}
