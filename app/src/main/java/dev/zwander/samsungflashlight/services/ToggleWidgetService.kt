package dev.zwander.samsungflashlight.services

import android.content.Intent
import android.content.pm.PackageManager
import android.widget.RemoteViewsService
import dev.zwander.samsungflashlight.widget.ToggleWidgetViewFactory

class ToggleWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent?): RemoteViewsFactory {
        return ToggleWidgetViewFactory(this)
    }

    override fun onCreate() {
        if (checkCallingOrSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            FlashlightService.start(this, FlashlightService.FlashlightMode.OFF)
            stopSelf()
            return
        }

        super.onCreate()
    }
}