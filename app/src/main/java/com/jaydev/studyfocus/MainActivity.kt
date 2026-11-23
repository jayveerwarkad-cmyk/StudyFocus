import android.app.AlertDialog
import android.app.TimePickerDialog
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var store: Storage
    private lateinit var info: TextView
    private lateinit var themeView: ThemeView
    private val PICK_WP = 11

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        store = Storage(this)

        val root = FrameLayout(this)
        themeView = ThemeView(this, store.getTheme(), store.getWallpaperUri(store.getTheme()))
        root.addView(themeView)

        val scroll = ScrollView(this)
        val col = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; setPadding(32, 64, 32, 64)
        }
        scroll.addView(col)
        root.addView(scroll)
        setContentView(root)

        info = TextView(this).apply {
            setTextColor(0xFFFFFFFF.toInt()); textSize = 16f
            setShadowLayer(8f, 0f, 0f, 0xFF000000.toInt())
        }
        col.addView(info)

        fun btn(label: String, action: () -> Unit) {
            col.addView(Button(this).apply { text = label; setOnClickListener { action() } })
        }

        btn("Start focus timer") { askTimer() }
        btn("Add study slot (max 2 per day, locked)") { addSlot() }
        btn("Choose important apps (never blocked)") { pickAllowed() }
        btn("Set daily app limit (once per day)") { pickLimitApp() }
        btn("Choose theme") { pickTheme() }
        btn("Set my own wallpaper for this theme") {
            startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                type = "*/*"; addCategory(Intent.CATEGORY_OPENABLE)
                putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*", "video/*"))
                addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION or
                        Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }, PICK_WP)
        }
        btn("Permission 1: Usage access") {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)) }
        btn("Permission 2: Accessibility (turn on Study Focus)") {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
        btn("Permission 3: Display over other apps") {
            startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName"))) }
        btn("Permission 4: Device admin (blocks uninstall)") {
            startActivity(Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,
                    ComponentName(this@MainActivity, AdminReceiver::class.java))
            })
        }
        refresh()
    }

    private fun refresh() {
        val slots = store.getSlots().joinToString("\n") {
            "  %02d:%02d to %02d:%02d".format(it.first / 60, it.first % 60,
                it.second / 60, it.second % 60)
        }
        val left = (store.timerEndMillis() - System.currentTimeMillis()) / 60000
        info.text = "Study Focus\n" +
            (if (left > 0) "Timer running: $left min left\n" else "No timer running\n") +
            "Today's study slots:\n" + (if (slots.isEmpty()) "  none" else slots) +
            "\nTheme: ${store.getTheme()}"
    }

    private fun askTimer() {
        val input = EditText(this).apply { hint = "Minutes (e.g. 45)" }
        AlertDialog.Builder(this).setTitle("Focus timer").setView(input)
            .setPositiveButton("Start") { _, _ ->
                val m = input.text.toString().toIntOrNull() ?: return@setPositiveButton
                store.startTimer(m); refresh()
                Toast.makeText(this, "Timer started. No turning back!", Toast.LENGTH_SHORT).show()
            }.show()
    }

    private fun addSlot() {
        if (store.getSlots().size >= 2) {
            Toast.makeText(this, "Aaj ke 2 slots ho chuke hain", Toast.LENGTH_LONG).show(); return
        }
        TimePickerDialog(this, { _, sh, sm ->
            TimePickerDialog(this, { _, eh, em ->
                val ok = store.addSlot(sh * 60 + sm, eh * 60 + em)
                Toast.makeText(this, if (ok) "Slot lock ho gaya" else "End time start se bada rakho",
                    Toast.LENGTH_LONG).show()
                refresh()
            }, sh + 1, sm, true).apply { setTitle("End time") }.show()
        }, 9, 0, true).apply { setTitle("Start time") }.show()
    }

    private fun installedApps(): List<Pair<String, String>> {
        val i = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        return packageManager.queryIntentActivities(i, 0)
            .map { Pair(it.loadLabel(packageManager).toString(), it.activityInfo.packageName) }
            .filter { it.second != packageName }.sortedBy { it.first.lowercase() }
    }

    private fun pickAllowed() {
        val apps = installedApps()
        val allowed = store.getAllowedApps()
        val checked = BooleanArray(apps.size) { apps[it].second in allowed }
        AlertDialog.Builder(this).setTitle("Important apps")
            .setMultiChoiceItems(apps.map { it.first }.toTypedArray(), checked) { _, i, c ->
                checked[i] = c }
            .setPositiveButton("Save") { _, _ ->
                store.setAllowedApps(apps.filterIndexed { i, _ -> checked[i] }
                    .map { it.second }.toSet())
            }.show()
    }

    private fun pickLimitApp() {
        val apps = installedApps()
        AlertDialog.Builder(this).setTitle("Which app?")
            .setItems(apps.map { it.first }.toTypedArray()) { _, i ->
                val input = EditText(this).apply { hint = "Daily limit in minutes" }
                AlertDialog.Builder(this).setTitle(apps[i].first).setView(input)
                    .setPositiveButton("Set") { _, _ ->
                        val m = input.text.toString().toIntOrNull() ?: return@setPositiveButton
                        val ok = store.setLimit(apps[i].second, m)
                        Toast.makeText(this,
                            if (ok) "Limit saved (naya limit kal se lagega)"
                            else "Aaj ek baar badal chuke ho", Toast.LENGTH_LONG).show()
                    }.show()
            }.show()
    }

    private fun pickTheme() {
        val names = ThemeView.THEMES
        AlertDialog.Builder(this).setTitle("Theme")
            .setItems(names.toTypedArray()) { _, i ->
                store.setTheme(names[i]); recreate()
            }.show()
    }

    override fun onActivityResult(req: Int, res: Int, data: Intent?) {
        super.onActivityResult(req, res, data)
        if (req == PICK_WP && res == RESULT_OK && data?.data != null) {
            val u = data.data!!
            contentResolver.takePersistableUriPermission(u, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            store.setWallpaperUri(store.getTheme(), u.toString())
            recreate()
        }
    }

    override fun onResume() { super.onResume(); refresh() }
}
