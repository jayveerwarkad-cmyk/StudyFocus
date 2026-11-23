package com.example.studyfocus

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object Prefs {
    private fun sp(c: Context) = c.getSharedPreferences("study_focus", Context.MODE_PRIVATE)

    fun today(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    fun fmt(m: Int): String = String.format(Locale.US, "%02d:%02d", m / 60, m % 60)

    // ---------- स्टडी टाइमर (दिन में एक बार) ----------
    fun isStudyLockedToday(c: Context) = sp(c).getString("study_date", "") == today()

    fun setStudyTime(c: Context, startMin: Int, endMin: Int): Boolean {
        if (isStudyLockedToday(c)) return false
        sp(c).edit()
            .putInt("study_start", startMin)
            .putInt("study_end", endMin)
            .putString("study_date", today())
            .apply()
        return true
    }

    fun studyText(c: Context): String {
        if (!isStudyLockedToday(c)) return "आज का स्टडी टाइम सेट नहीं है"
        return "आज लॉक: " + fmt(sp(c).getInt("study_start", 0)) + " से " + fmt(sp(c).getInt("study_end", 0))
    }

    fun isStudyActiveNow(c: Context): Boolean {
        if (!isStudyLockedToday(c)) return false
        val cal = Calendar.getInstance()
        val now = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
        return now >= sp(c).getInt("study_start", -1) && now < sp(c).getInt("study_end", -1)
    }

    // ---------- ऐप की रोज़ की लिमिट (हर ऐप के लिए दिन में एक बार) ----------
    fun getLimit(c: Context, pkg: String): Int = sp(c).getInt("limit_$pkg", -1)

    fun isLimitLockedToday(c: Context, pkg: String) =
        sp(c).getString("limit_date_$pkg", "") == today()

    fun setLimit(c: Context, pkg: String, minutes: Int): Boolean {
        if (isLimitLockedToday(c, pkg)) return false
        sp(c).edit()
            .putInt("limit_$pkg", minutes)
            .putString("limit_date_$pkg", today())
            .apply()
        return true
    }

    // ---------- ब्लॉक लिस्ट (जोड़ सकते हैं, उसी दिन हटा नहीं सकते) ----------
    fun getBlocked(c: Context): Set<String> =
        HashSet(sp(c).getStringSet("blocked", emptySet()) ?: emptySet())

    fun addBlocked(c: Context, pkg: String) {
        val s = HashSet(getBlocked(c))
        s.add(pkg)
        sp(c).edit().putStringSet("blocked", s).putString("bdate_$pkg", today()).apply()
    }

    fun removeBlocked(c: Context, pkg: String): Boolean {
        if (sp(c).getString("bdate_$pkg", "") == today()) return false
        if (isStudyActiveNow(c)) return false
        val s = HashSet(getBlocked(c))
        s.remove(pkg)
        sp(c).edit().putStringSet("blocked", s).apply()
        return true
    }

    // ---------- स्टडी टाइम में खुले रहने वाले ऐप ----------
    private val defaultAllowed = setOf(
        "com.google.android.apps.bard",
        "com.google.android.googlequicksearchbox",
        "com.whatsapp"
    )

    fun getAllowed(c: Context): Set<String> =
        HashSet(sp(c).getStringSet("allowed", defaultAllowed) ?: defaultAllowed)

    fun setAllowed(c: Context, pkg: String, on: Boolean): Boolean {
        if (isStudyActiveNow(c)) return false
        val s = HashSet(getAllowed(c))
        if (on) s.add(pkg) else s.remove(pkg)
        sp(c).edit().putStringSet("allowed", s).apply()
        return true
    }
}
