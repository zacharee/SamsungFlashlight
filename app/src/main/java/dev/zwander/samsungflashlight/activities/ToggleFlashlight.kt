package dev.zwander.samsungflashlight.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dev.zwander.samsungflashlight.R
import dev.zwander.samsungflashlight.services.FlashlightService

class ToggleFlashlight : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent.action == Intent.ACTION_CREATE_SHORTCUT) {
            val shortcutIntent = Intent(this, ToggleFlashlight::class.java)

            val resultIntent = Intent()
            resultIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent)
            resultIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, resources.getString(R.string.app_name))

            val iconRes = Intent.ShortcutIconResource.fromContext(
                this, R.mipmap.ic_launcher
            )

            intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconRes)

            setResult(Activity.RESULT_OK, resultIntent)
        } else {
            FlashlightService.start(this, FlashlightService.FlashlightMode.TOGGLE)
        }

        finish()
    }
}