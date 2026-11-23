package com.example.studyfocus

import android.app.AppOpsManager
import android.app.TimePickerDialog
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private var startMin = -1
    private var endMin = -1
    private var studyInfo: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= 33) {
            requestPermissions(arrayOf("android.permission.POST_NOTIFICATIONS"), 1)
        }
    }

    override fun onResume() {
        super.onResume()
        build()
    }

    private fun toast(s: String) = Toast.makeText(this, s, Toast.LENGTH_SHORT).show()

    private fun hasUsageAccess(): Boolean {
        val ops = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = ops.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun refreshStudy() {
        val locked = Prefs.isStudyLockedToday(this)
        studyInfo?.text = if (locked) Prefs.studyText(this)
        else "चुना: " + (if (startMin >= 0) Prefs.fmt(startMin) else "--") +
            " से " + (if (endMin >= 0) Prefs.fmt(endMin) else "--")
    }

    private fun build() {
        val t = Themes.current(this)
        val fg = Color.parseColor(t.textColor)

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setPadding(40, 60, 40, 60)
        val sv = ScrollView(this)
        sv.setBackgroundColor(Color.parseColor(t.bgColor))
        sv.addView(root)
        setContentView(sv)

        fun label(s: String, size: Float = 16f): TextView {
            val tv = TextView(this)
            tv.text = s
            tv.textSize = size
            tv.setTextColor(fg)
            tv.setPadding(0, 36, 0, 12)
            root.addView(tv)
            return tv
        }

        fun button(s: String, onClick: () -> Unit) {
            val b = Button(this)
            b.text = s
            b.setOnClickListener { onClick() }
            root.addView(b)
        }

        fun openPicker(mode: String) {
            startActivity(Intent(this, AppPickerActivity::class.java).putExtra("mode", mode))
        }

        label("Study Focus", 30f)

        // 1. Permissions
        label("1. पहले ये permissions दें")
        button("Usage Access चालू करें") {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }
        button("Display over other apps चालू करें") {
            startActivity(
                Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + packageName))
            )
        }
        button("अनइंस्टॉल सुरक्षा चालू करें") {
            val comp = ComponentName(this, AdminReceiver::class.java)
            startActivity(
                Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
                    .putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, comp)
                    .putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "स्टडी टाइम में ऐप डिलीट होने से रोकने के लिए")
            )
        }

        // 2. स्टडी टाइमर
        label("2. स्टडी टाइमर (दिन में एक बार)")
        studyInfo = label("")
        refreshStudy()
        button("शुरू होने का समय चुनें") {
            TimePickerDialog(this, { _, h, m -> startMin = h * 60 + m; refreshStudy() }, 18, 0, false).show()
        }
        button("खत्म होने का समय चुनें") {
            TimePickerDialog(this, { _, h, m -> endMin = h * 60 + m; refreshStudy() }, 20, 0, false).show()
        }
        button("स्टडी टाइमर आज के लिए लॉक करें") {
            if (startMin < 0 || endMin <= startMin) {
                toast("पहले सही शुरू और खत्म का समय चुनें")
            } else if (Prefs.setStudyTime(this, startMin, endMin)) {
                toast("स्टडी टाइमर लॉक हो गया")
                refreshStudy()
            } else {
                toast("आज पहले ही सेट हो चुका है, कल बदलें")
            }
        }

        // 3. ऐप्स
        label("3. ऐप्स")
        button("बंद रखने वाले ऐप चुनें") { openPicker("block") }
        button("हर ऐप की रोज़ की लिमिट") { openPicker("limit") }
        button("स्टडी टाइम में खुले रहने वाले ऐप") { openPicker("allow") }

        // 4. थीम
        label("4. थीम")
        button("थीम चुनें  (अभी: " + t.name + ")") {
            startActivity(Intent(this, ThemeActivity::class.java))
        }

        // 5. चालू करें
        label("5. चालू करें")
        button("Study Focus चालू करें") {
            if (!hasUsageAccess()) {
                toast("पहले Usage Access दें")
            } else if (!Settings.canDrawOverlays(this)) {
                toast("पहले Display over other apps दें")
            } else {
                startForegroundService(Intent(this, MonitorService::class.java))
                toast("चालू हो गया")
            }
        }
    }
}
