package com.jaydev.studyfocus

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import jaydev. studyfocus. Storage
class Storage(context: Context) {
    private val sp: SharedPreferences =
        context.getSharedPreferences("focus", Context.MODE_PRIVATE)

    private fun today(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    private fun tomorrow(): String {
        val c = Calendar.getInstance(); c.add(Calendar.DAY_OF_YEAR, 1)
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(c.time)
    }

    // ---------- Important apps (never blocked) ----------
    fun getAllowedApps(): MutableSet<String> =
        sp.getStringSet("allowed", mutableSetOf())!!.toMutableSet()

    fun setAllowedApps(s: Set<String>) =
        sp.edit().putStringSet("allowed", s).apply()

    // ---------- Study slots: max 2 per day, locked once saved ----------
    // Each slot is stored as "startMinute-endMinute" for that date.
    fun getSlots(): List<Pair<Int, Int>> {
        if (sp.getString("slotsDate", "") != today()) return emptyList()
        val arr = JSONArray(sp.getString("slots", "[]"))
        return (0 until arr.length()).map {
            val o = arr.getJSONObject(it); Pair(o.getInt("s"), o.getInt("e"))
        }
    }

    fun addSlot(startMin: Int, endMin: Int): Boolean {
        val cur = getSlots().toMutableList()
        if (cur.size >= 2 || endMin <= startMin) return false
        cur.add(Pair(startMin, endMin))
        val arr = JSONArray()
        cur.forEach { arr.put(JSONObject().put("s", it.first).put("e", it.second)) }
        sp.edit().putString("slotsDate", today()).putString("slots", arr.toString()).apply()
        return true
    }

    fun isStudyActiveNow(): Boolean {
        val c = Calendar.getInstance()
        val now = c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE)
        if (getSlots().any { now >= it.first && now < it.second }) return true
        return timerEndMillis() > System.currentTimeMillis()
    }

    // ---------- Focus timer ----------
    fun startTimer(minutes: Int) =
        sp.edit().putLong("timerEnd", System.currentTimeMillis() + minutes * 60_000L).apply()

    fun timerEndMillis(): Long = sp.getLong("timerEnd", 0L)

    // ---------- Daily app limits: change once per day, effective next day ----------
    fun getLimitMinutes(pkg: String): Int {
        val o = JSONObject(sp.getString("limits", "{}"))
        if (!o.has(pkg)) return 0
        val e = o.getJSONObject(pkg)
        // A pending limit becomes active from its start date.
        return if (e.has("nextFrom") && e.getString("nextFrom") <= today())
            e.getInt("next") else e.getInt("cur")
    }

    fun setLimit(pkg: String, minutes: Int): Boolean {
        val o = JSONObject(sp.getString("limits", "{}"))
        val e = if (o.has(pkg)) o.getJSONObject(pkg) else JSONObject().put("cur", 0)
        if (e.optString("changedOn") == today()) return false   // already changed today
        // First time ever: apply from today. After that: from tomorrow.
        if (!o.has(pkg)) { e.put("cur", minutes) }
        else { e.put("next", minutes); e.put("nextFrom", tomorrow()) }
        e.put("changedOn", today())
        o.put(pkg, e)
        sp.edit().putString("limits", o.toString()).apply()
        return true
    }

    fun limitedPackages(): List<String> {
        val o = JSONObject(sp.getString("limits", "{}"))
        return o.keys().asSequence().toList()
    }

    // ---------- Theme ----------
    fun getTheme(): String = sp.getString("theme", "naruto")!!
    fun setTheme(t: String) = sp.edit().putString("theme", t).apply()
    fun getWallpaperUri(theme: String): String? = sp.getString("wp_$theme", null)
    fun setWallpaperUri(theme: String, uri: String) =
        sp.edit().putString("wp_$theme", uri).apply()
}
