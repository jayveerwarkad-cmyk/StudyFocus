import android.os.Bundle
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class BlockActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val store = Storage(this)
        val root = FrameLayout(this)
        root.addView(ThemeView(this, store.getTheme(), store.getWallpaperUri(store.getTheme())))
        val tv = TextView(this).apply {
            text = intent.getStringExtra("msg") ?: "Focus karo!"
            textSize = 26f
            setTextColor(0xFFFFFFFF.toInt())
            gravity = Gravity.CENTER
            setShadowLayer(12f, 0f, 0f, 0xFF000000.toInt())
        }
        root.addView(tv, FrameLayout.LayoutParams(-1, -1))
        setContentView(root)
    }

    // Back button does nothing while blocked.
    override fun onBackPressed() {}
}
