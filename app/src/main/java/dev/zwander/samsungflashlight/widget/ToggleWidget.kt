package dev.zwander.samsungflashlight.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import dev.zwander.samsungflashlight.R
import dev.zwander.samsungflashlight.services.FlashlightService
import dev.zwander.samsungflashlight.services.ToggleWidgetService

class ToggleWidget : AppWidgetProvider() {
    companion object {
        fun sendUpdate(context: Context) {
            //There's a new list of IDs; make sure the factory is notified
            val manager = AppWidgetManager.getInstance(context)
            val component = ComponentName(context, ToggleWidget::class.java)
            manager.notifyAppWidgetViewDataChanged(manager.getAppWidgetIds(component), R.id.grid)
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val view = RemoteViews(context.packageName, R.layout.toggle_widget_layout)
        view.setRemoteAdapter(R.id.grid, Intent(context, ToggleWidgetService::class.java))
        view.setOnClickPendingIntent(
            R.id.root,
            PendingIntent.getForegroundService(
                context,
                100,
                FlashlightService.createIntent(context, FlashlightService.FlashlightMode.TOGGLE),
                PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        )

        appWidgetIds.forEach {
            appWidgetManager.updateAppWidget(it, view)
        }
    }
}