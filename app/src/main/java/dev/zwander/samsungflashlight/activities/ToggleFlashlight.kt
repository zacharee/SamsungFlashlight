package dev.zwander.samsungflashlight.activities

import android.app.Activity
import android.content.Intent
import dev.zwander.samsungflashlight.R
import dev.zwander.samsungflashlight.services.FlashlightService

class ToggleFlashlight : BaseActivity() {
    override fun onPermissionsDenied() {
        setResult(Activity.RESULT_CANCELED)
        super.onPermissionsDenied()
    }

    @Suppress("DEPRECATION")
    override fun onPermissionsGranted() {
        if (intent.action == Intent.ACTION_CREATE_SHORTCUT) {
            val shortcutIntent = Intent(this, ToggleFlashlight::class.java)

            val resultIntent = Intent()
            resultIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent)
            resultIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, resources.getString(R.string.app_name))

            val iconRes = Intent.ShortcutIconResource.fromContext(
                this, R.mipmap.ic_launcher
            )

            resultIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconRes)

            setResult(Activity.RESULT_OK, resultIntent)
        } else {
            FlashlightService.start(this, FlashlightService.FlashlightMode.TOGGLE)
        }

        finish()
    }
}