package dev.zwander.samsungflashlight.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraManager.TorchCallback
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import dev.zwander.samsungflashlight.R
import dev.zwander.samsungflashlight.util.Event
import dev.zwander.samsungflashlight.util.PrefManager
import dev.zwander.samsungflashlight.util.eventManager
import dev.zwander.samsungflashlight.util.prefManager
import dev.zwander.samsungflashlight.widget.ToggleWidget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor

class FlashlightService : Service(), OnSharedPreferenceChangeListener {
    companion object {
        private const val EXTRA_FLASHLIGHT_MODE = "flashlight_mode"

        fun start(context: Context, mode: FlashlightMode? = null) {
            context.startForegroundService(createIntent(context, mode))
        }

        fun createIntent(context: Context, mode: FlashlightMode? = null): Intent {
            return Intent(context, FlashlightService::class.java).apply {
                mode?.let { putExtra(EXTRA_FLASHLIGHT_MODE, it) }
            }
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

                eventManager.sendEvent(Event.FlashlightModeChange(enabled))
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
        prefManager.registerOnSharedPreferenceChangedListener(this)
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

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            PrefManager.Keys.KEY_LIGHT_STRENGTH -> if (enabled) setTorchMode(true)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        camera.unregisterTorchCallback(modeCallback)
        prefManager.unregisterOnSharedPreferencesChangedListener(this)
    }

    private fun setTorchMode(enabled: Boolean) {
        CameraManager::class.java.getDeclaredMethod(if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) "setTorchMode" else "semSetTorchMode", String::class.java, Boolean::class.java, Int::class.java)
            .invoke(camera, cameraId, enabled, prefManager.lightStrength)
        ToggleWidget.sendUpdate(this)
    }
}