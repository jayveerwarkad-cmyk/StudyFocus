package com.jaydev.studyfocus

import android.accessibilityservice.AccessibilityService
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import java.util.*

class BlockerService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val pkg = event.packageName?.toString() ?: return
        if (pkg == packageName || pkg == "com.android.systemui") return

        val store = Storage(this)
        val homeApps = homePackages()
        if (pkg in homeApps) return

        // Rule 1: during study time, only important apps are allowed.
        if (store.isStudyActiveNow()) {
            if (pkg !in store.getAllowedApps() && !isDialerOrSettingsCritical(pkg)) {
                block("Study time chal raha hai. Focus karo!")
            }
            return
        }

        // Rule 2: daily limit per app.
        val limit = store.getLimitMinutes(pkg)
        if (limit > 0 && usedTodayMinutes(pkg) >= limit) {
            block("Aaj ki limit khatam ho gayi is app ki.")
        }
    }

    private fun block(msg: String) {
        val i = Intent(this, BlockActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            .putExtra("msg", msg)
        startActivity(i)
        // Also push the user to the home screen so the blocked app closes.
        performGlobalAction(GLOBAL_ACTION_HOME)
    }

    private fun usedTodayMinutes(pkg: String): Int {
        val usm = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val c = Calendar.getInstance()
        c.set(Calendar.HOUR_OF_DAY, 0); c.set(Calendar.MINUTE, 0); c.set(Calendar.SECOND, 0)
        val stats = usm.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY, c.timeInMillis, System.currentTimeMillis())
        val ms = stats.filter { it.packageName == pkg }.sumOf { it.totalTimeInForeground }
        return (ms / 60000).toInt()
    }

    private fun homePackages(): Set<String> {
        val i = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        return packageManager.queryIntentActivities(i, 0)
            .map { it.activityInfo.packageName }.toSet()
    }

    // Keep phone calls and system dialogs usable.
    private fun isDialerOrSettingsCritical(pkg: String): Boolean =
        pkg.contains("dialer") || pkg.contains("incallui") || pkg == "com.android.settings"

    override fun onInterrupt() {}
}
