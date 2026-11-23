package com.example.studyfocus

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import java.util.Calendar

object UsageHelper {
    private var lastTime = 0L
    private var current: String? = null

    // अभी स्क्रीन पर सबसे ऊपर कौन सा ऐप है
    fun foreground(c: Context): String? {
        val usm = c.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val now = System.currentTimeMillis()
        val from = if (lastTime == 0L) now - 3_600_000L else lastTime
        val ev = usm.queryEvents(from, now)
        val e = UsageEvents.Event()
        while (ev.hasNextEvent()) {
            ev.getNextEvent(e)
            if (e.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                current = e.packageName
            } else if (e.eventType == UsageEvents.Event.ACTIVITY_PAUSED) {
                if (e.packageName == current) current = null
            }
        }
        lastTime = now
        return current
    }

    // आज रात 12 बजे से अब तक इस ऐप का इस्तेमाल (मिनट)
    fun usedTodayMinutes(c: Context, pkg: String): Long {
        val usm = c.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val now = System.currentTimeMillis()
        val ev = usm.queryEvents(cal.timeInMillis, now)
        val e = UsageEvents.Event()
        var total = 0L
        var start = -1L
        while (ev.hasNextEvent()) {
            ev.getNextEvent(e)
            if (e.packageName != pkg) continue
            if (e.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                if (start < 0) start = e.timeStamp
            } else if (e.eventType == UsageEvents.Event.ACTIVITY_PAUSED) {
                if (start >= 0) {
                    total += e.timeStamp - start
                    start = -1L
                }
            }
        }
        if (start >= 0) total += now - start
        return total / 60_000L
    }
}
