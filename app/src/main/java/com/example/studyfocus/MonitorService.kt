package com.example.studyfocus

import android.app.ActivityManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Handler
import android.os.IBinder
import android.os.Looper

class MonitorService : Service() {

    private val handler = Handler(Looper.getMainLooper())
    private var lastBlock = 0L

    private val tick = object : Runnable {
        override fun run() {
            try { check() } catch (e: Exception) { }
            handler.postDelayed(this, 1000)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(1, notification())
        handler.removeCallbacks(tick)
        handler.post(tick)
        return START_STICKY
    }

    private fun launcherPkg(): String? {
        val i = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        return packageManager.resolveActivity(i, PackageManager.MATCH_DEFAULT_ONLY)?.activityInfo?.packageName
    }

    private fun always(): Set<String> {
        val s = hashSetOf(
            packageName,
            "com.android.systemui",
            "com.android.dialer",
            "com.google.android.dialer",
            "com.samsung.android.dialer",
            "com.google.android.permissioncontroller",
            "com.android.permissioncontroller"
        )
        launcherPkg()?.let { s.add(it) }
        return s
    }

    private fun check() {
        val pkg = UsageHelper.foreground(this) ?: return
        if (pkg in always()) return

        val reason: String? = when {
            Prefs.isStudyActiveNow(this) && pkg !in Prefs.getAllowed(this) ->
                "अभी पढ़ाई का समय है!\nयह ऐप बंद है।"
            pkg in Prefs.getBlocked(this) ->
                "यह ऐप बंद है।"
            Prefs.getLimit(this, pkg) >= 0 &&
                UsageHelper.usedTodayMinutes(this, pkg) >= Prefs.getLimit(this, pkg) ->
                "आज की लिमिट पूरी हो गई।\nकल फिर खुलेगा।"
            else -> null
        }
        if (reason == null) return

        val now = System.currentTimeMillis()
        if (now - lastBlock < 1500) return
        lastBlock = now

        // ऐप को बैकग्राउंड से भी बंद करने की कोशिश
        (getSystemService(ACTIVITY_SERVICE) as ActivityManager).killBackgroundProcesses(pkg)

        startActivity(
            Intent(this, BlockActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .putExtra("msg", reason)
        )
    }

    private fun notification(): Notification {
        val ch = "focus_channel"
        getSystemService(NotificationManager::class.java)
            .createNotificationChannel(NotificationChannel(ch, "Study Focus", NotificationManager.IMPORTANCE_LOW))
        return Notification.Builder(this, ch)
            .setContentTitle("Study Focus चालू है")
            .setSmallIcon(android.R.drawable.ic_lock_idle_lock)
            .build()
    }

    override fun onDestroy() {
        handler.removeCallbacks(tick)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
