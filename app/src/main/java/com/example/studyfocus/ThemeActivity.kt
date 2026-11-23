package com.example.studyfocus

import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ThemeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val list = LinearLayout(this)
        list.orientation = LinearLayout.VERTICAL
        list.setPadding(40, 60, 40, 60)
        setContentView(ScrollView(this).apply { addView(list) })

        for (t in Themes.all) {
            val b = Button(this)
            b.text = t.name
            b.setBackgroundColor(Color.parseColor(t.bgColor))
            b.setTextColor(Color.parseColor(t.textColor))
            b.setOnClickListener {
                Themes.saveChoice(this, t.id)
                Toast.makeText(this, t.name + " थीम लग गई", Toast.LENGTH_SHORT).show()
                finish()
            }
            list.addView(b)
        }
    }
}
