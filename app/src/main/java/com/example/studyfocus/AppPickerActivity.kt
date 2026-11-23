package com.example.studyfocus

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class AppPickerActivity : AppCompatActivity() {

    private fun toast(s: String) = Toast.makeText(this, s, Toast.LENGTH_SHORT).show()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mode = intent.getStringExtra("mode") ?: "block"
        val t = Themes.current(this)
        val fg = Color.parseColor(t.textColor)
        val pm = packageManager

        val apps = pm.queryIntentActivities(
            Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER), 0
        )
            .map { Pair(it.activityInfo.packageName, it.loadLabel(pm).toString()) }
            .filter { it.first != packageName }
            .distinctBy { it.first }
            .sortedBy { it.second.lowercase() }

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setPadding(40, 60, 40, 60)
        val sv = ScrollView(this)
        sv.setBackgroundColor(Color.parseColor(t.bgColor))
        sv.addView(root)
        setContentView(sv)

        val title = TextView(this)
        title.textSize = 18f
        title.setTextColor(fg)
        title.setPadding(0, 0, 0, 30)
        title.text = when (mode) {
            "block" -> "जो ऐप बंद रखने हैं उन्हें टिक करें।\nआज जोड़ा हुआ ऐप आज हट नहीं सकता।"
            "allow" -> "स्टडी टाइम में जो ऐप खुले रहें उन्हें टिक करें।\nस्टडी टाइम में बदलाव नहीं होगा।"
            else -> "ऐप पर टैप करके आज की लिमिट (मिनट) डालें।\nदिन में एक बार ही, 0 मतलब आज पूरा बंद।"
        }
        root.addView(title)

        val current: Set<String> = if (mode == "block") Prefs.getBlocked(this) else Prefs.getAllowed(this)

        for ((pkg, label) in apps) {
            if (mode == "limit") {
                val b = Button(this)
                fun refresh() {
                    val l = Prefs.getLimit(this, pkg)
                    b.text = if (l >= 0) label + "  —  " + l + " मिनट" else label
                }
                refresh()
                b.setOnClickListener {
                    if (Prefs.isLimitLockedToday(this, pkg)) {
                        toast("आज की लिमिट बदल नहीं सकते, कल बदलना")
                    } else {
                        val et = EditText(this)
                        et.inputType = InputType.TYPE_CLASS_NUMBER
                        et.hint = "कितने मिनट"
                        AlertDialog.Builder(this)
                            .setTitle(label)
                            .setView(et)
                            .setPositiveButton("आज के लिए लॉक करें") { _, _ ->
                                val m = et.text.toString().toIntOrNull()
                                if (m == null) toast("मिनट लिखें")
                                else { Prefs.setLimit(this, pkg, m); refresh() }
                            }
                            .setNegativeButton("रद्द", null)
                            .show()
                    }
                }
                root.addView(b)
            } else {
                val cb = CheckBox(this)
                cb.text = label
                cb.textSize = 16f
                cb.setTextColor(fg)
                cb.isChecked = pkg in current
                cb.setOnClickListener {
                    val on = cb.isChecked
                    val ok: Boolean
                    if (mode == "block") {
                        if (on) { Prefs.addBlocked(this, pkg); ok = true }
                        else ok = Prefs.removeBlocked(this, pkg)
                    } else {
                        ok = Prefs.setAllowed(this, pkg, on)
                    }
                    if (!ok) {
                        cb.isChecked = !on
                        toast("अभी बदलाव नहीं हो सकता (आज जोड़ा था या स्टडी टाइम है)")
                    }
                }
                root.addView(cb)
            }
        }
    }
}
