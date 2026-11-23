package com.example.studyfocus

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class BlockActivity : AppCompatActivity() {

    private var going = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val t = Themes.current(this)

        val frame = FrameLayout(this)
        frame.setBackgroundColor(Color.parseColor(t.bgColor))

        t.imageName?.let { name ->
            val id = resources.getIdentifier(name, "drawable", packageName)
            if (id != 0) {
                val iv = ImageView(this)
                iv.setImageResource(id)
                iv.scaleType = ImageView.ScaleType.CENTER_CROP
                iv.alpha = 0.6f
                frame.addView(iv)
            }
        }

        t.live?.let { frame.addView(LiveThemeView(this, it)) }

        val tv = TextView(this)
        tv.text = intent.getStringExtra("msg") ?: "ऐप बंद है"
        tv.textSize = 28f
        tv.gravity = Gravity.CENTER
        tv.setTextColor(Color.parseColor(t.textColor))
        tv.setShadowLayer(8f, 0f, 0f, Color.BLACK)
        frame.addView(tv)

        setContentView(frame)
    }

    override fun onResume() {
        super.onResume()
        if (going) return
        going = true
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(
                Intent(Intent.ACTION_MAIN)
                    .addCategory(Intent.CATEGORY_HOME)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
            finish()
        }, 2000)
    }
}
