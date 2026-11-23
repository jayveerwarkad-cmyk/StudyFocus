package com.example.studyfocus

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View
import kotlin.math.sin
import kotlin.random.Random

class LiveThemeView(context: Context, private val type: String) : View(context) {

    private class Particle(var x: Float, var y: Float, val speed: Float, val size: Float)

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val list = mutableListOf<Particle>()

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        list.clear()
        val n = if (type == "rain") 120 else 70
        repeat(n) {
            list.add(
                Particle(
                    Random.nextFloat() * w, Random.nextFloat() * h,
                    Random.nextFloat() * 4f + 1f, Random.nextFloat() * 5f + 3f
                )
            )
        }
    }

    override fun onDraw(canvas: Canvas) {
        val t = (System.currentTimeMillis() % 100000L) / 400f
        for (p in list) {
            when (type) {
                "stars" -> {
                    val a = (170 + 80 * sin(t * p.speed * 0.3f + p.x)).toInt().coerceIn(0, 255)
                    paint.color = Color.argb(a, 255, 255, 255)
                    canvas.drawCircle(p.x, p.y, p.size / 2f, paint)
                    p.y += p.speed * 0.15f
                }
                "leaves" -> {
                    paint.color = Color.parseColor("#8BC34A")
                    canvas.drawOval(p.x, p.y, p.x + p.size * 3f, p.y + p.size * 1.6f, paint)
                    p.y += p.speed
                    p.x += (Random.nextFloat() - 0.5f) * 3f
                }
                "rain" -> {
                    paint.color = Color.parseColor("#B3E5FC")
                    paint.strokeWidth = 2f
                    canvas.drawLine(p.x, p.y, p.x, p.y + p.size * 4f, paint)
                    p.y += p.speed * 6f
                }
                "sparks" -> {
                    paint.color = Color.parseColor("#FFB74D")
                    canvas.drawCircle(p.x, p.y, p.size / 2f, paint)
                    p.y -= p.speed
                    p.x += (Random.nextFloat() - 0.5f) * 2f
                }
            }
            if (type == "sparks") {
                if (p.y < -10f) { p.y = height + 10f; p.x = Random.nextFloat() * width }
            } else if (p.y > height) {
                p.y = -10f
                p.x = Random.nextFloat() * width
            }
        }
        postInvalidateOnAnimation()
    }
}
