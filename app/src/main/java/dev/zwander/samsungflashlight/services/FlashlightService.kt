package dev.zwander.samsungflashlight.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraManager.TorchCallback
import android.os.IBinder
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import dev.zwander.samsungflashlight.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor

class FlashlightService : Service() {
    companion object {
        private const val EXTRA_FLASHLIGHT_MODE = "flashlight_mode"

        fun start(context: Context, mode: FlashlightMode? = null) {
            val intent = Intent(context, FlashlightService::class.java)
            mode?.let { intent.putExtra(EXTRA_FLASHLIGHT_MODE, it) }

            context.startForegroundService(intent)
        }
    }

    enum class FlashlightMode {
        ON,
        OFF,
        TOGGLE
    }

    private val camera by lazy { getSystemService(Context.CAMERA_SERVICE) as CameraManager }
    private val cameraId by lazy {
        camera.cameraIdList.map { it to camera.getCameraCharacteristics(it) }
            .firstOrNull { it.second[CameraCharacteristics.FLASH_INFO_AVAILABLE] }
            ?.first
    }
    private val modeCallback = object : TorchCallback() {
        override fun onTorchModeChanged(cameraId: String?, enabled: Boolean) {
            if (cameraId == this@FlashlightService.cameraId) {
                if (!enabled && this@FlashlightService.enabled) {
                    stopSelf()
                }

                this@FlashlightService.enabled = enabled
                isAvailable = true
            }
        }

        override fun onTorchModeUnavailable(cameraId: String?) {
            if (cameraId == this@FlashlightService.cameraId) {
                isAvailable = false
            }
        }
    }

    private var enabled = false
    private var isAvailable = true

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        val nm = NotificationManagerCompat.from(this)
        nm.createNotificationChannel(NotificationChannelCompat.Builder("main", NotificationManagerCompat.IMPORTANCE_LOW)
            .setName(resources.getString(R.string.app_name)).build())

        startForeground(
            100,
            NotificationCompat.Builder(this, "main")
                .setContentTitle(resources.getString(R.string.app_name))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build()
        )

        camera.registerTorchCallback(Dispatchers.Main.asExecutor(), modeCallback)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.hasExtra(EXTRA_FLASHLIGHT_MODE) == true) {
            val mode = intent.getSerializableExtra(EXTRA_FLASHLIGHT_MODE) as FlashlightMode

            setTorchMode(when (mode) {
                FlashlightMode.ON -> true
                FlashlightMode.OFF -> false
                FlashlightMode.TOGGLE -> !enabled
            })
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()

        camera.unregisterTorchCallback(modeCallback)
    }

    private fun setTorchMode(enabled: Boolean) {
        CameraManager::class.java.getDeclaredMethod("setTorchMode", String::class.java, Boolean::class.java, Int::class.java)
            .invoke(camera, cameraId, enabled, 5)
    }
}