package dev.zwander.samsungflashlight

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraManager.TorchCallback
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Switch
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import dev.zwander.samsungflashlight.ui.theme.SamsungFlashlightTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import org.lsposed.hiddenapibypass.HiddenApiBypass

class MainActivity : ComponentActivity() {
    private val permissionsLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (!granted) {
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        HiddenApiBypass.setHiddenApiExemptions("")

        if (checkCallingOrSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissionsLauncher.launch(android.Manifest.permission.CAMERA)
        }

        setContent {
            MainContent()
        }
    }
}

@Composable
fun MainContent() {
    val context = LocalContext.current

    val camera = remember {
        context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }

    val id = remember {
        camera.cameraIdList.map { it to camera.getCameraCharacteristics(it) }
            .firstOrNull { it.second[CameraCharacteristics.FLASH_INFO_AVAILABLE] }
            ?.first
    }

    var cameraEnabled by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(key1 = cameraEnabled) {
        CameraManager::class.java.getDeclaredMethod("setTorchMode", String::class.java, Boolean::class.java, Int::class.java)
            .invoke(camera, id, cameraEnabled, 5)
    }

    DisposableEffect(key1 = null) {
        val listener = object : TorchCallback() {
            override fun onTorchModeChanged(cameraId: String?, enabled: Boolean) {
                if (cameraId == id) {
                    cameraEnabled = enabled
                }
            }

            override fun onTorchModeUnavailable(cameraId: String?) {
                if (cameraId == id) {
                    cameraEnabled = false
                }
            }
        }

        camera.registerTorchCallback(Dispatchers.Main.asExecutor(), listener)

        onDispose {
            camera.unregisterTorchCallback(listener)
        }
    }

    SamsungFlashlightTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colors.background
        ) {
            Switch(checked = cameraEnabled, onCheckedChange = {
                cameraEnabled = it
            })
        }
    }
}