package dev.zwander.samsungflashlight.widget

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraManager.TorchCallback
import android.widget.RemoteViews
import android.widget.RemoteViewsService.RemoteViewsFactory
import dev.zwander.samsungflashlight.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor

class ToggleWidgetViewFactory(private val context: Context) : RemoteViewsFactory {
    private val camera by lazy { context.getSystemService(Context.CAMERA_SERVICE) as CameraManager }
    private val cameraId by lazy {
        camera.cameraIdList.map { it to camera.getCameraCharacteristics(it) }
            .firstOrNull { it.second[CameraCharacteristics.FLASH_INFO_AVAILABLE] }
            ?.first
    }

    private var flashlightOn = false
        set(value) {
            field = value
            ToggleWidget.sendUpdate(context)
        }

    private val listener = object : TorchCallback() {
        override fun onTorchModeChanged(cameraId: String?, enabled: Boolean) {
            if (cameraId == this@ToggleWidgetViewFactory.cameraId) {
                flashlightOn = enabled
            }
        }

        override fun onTorchModeUnavailable(cameraId: String?) {
            if (cameraId == this@ToggleWidgetViewFactory.cameraId) {
                flashlightOn = false
            }
        }
    }

    override fun getCount(): Int {
        return 1
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getLoadingView(): RemoteViews? {
        return null
    }

    override fun getViewAt(position: Int): RemoteViews {
        return RemoteViews(context.packageName, R.layout.toggle_widget_item).apply {
            setImageViewResource(R.id.image, if (flashlightOn) R.drawable.baseline_flashlight_on_24 else R.drawable.baseline_flashlight_off_24)
        }
    }

    override fun getViewTypeCount(): Int {
        return 1
    }

    override fun hasStableIds(): Boolean {
        return true
    }

    override fun onCreate() {
        camera.registerTorchCallback(Dispatchers.Main.asExecutor(), listener)
    }

    override fun onDestroy() {
        camera.unregisterTorchCallback(listener)
    }

    override fun onDataSetChanged() {}
}