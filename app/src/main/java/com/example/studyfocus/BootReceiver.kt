package com.example.studyfocus

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Settings

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(c: Context, i: Intent) {
        if (i.action == Intent.ACTION_BOOT_COMPLETED && Settings.canDrawOverlays(c)) {
            c.startForegroundService(Intent(c, MonitorService::class.java))
        }
    }
}
