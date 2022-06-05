package dev.zwander.samsungflashlight.activities

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts

abstract class BaseActivity : ComponentActivity() {
    private val permissionsLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            onPermissionsGranted()
        } else {
            onPermissionsDenied()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (checkCallingOrSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissionsLauncher.launch(android.Manifest.permission.CAMERA)
        } else {
            onPermissionsGranted()
        }
    }

    protected open fun onPermissionsGranted() {}
    protected open fun onPermissionsDenied() {
        finish()
    }
}