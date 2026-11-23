import android.content.Context
import android.graphics.*
import android.net.Uri
import android.view.View
import kotlin.math.*
import kotlin.random.Random

class ThemeView(ctx: Context, private val theme: String, wpUri: String?) : View(ctx) {

    companion object {
        val THEMES = listOf(
            "naruto", "sasuke_chidori", "one_piece", "jujutsu_kaisen", "dragon_ball",
            "demon_slayer", "attack_on_titan", "my_hero_academia", "chainsaw_man", "space"
        )
    }

    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    private var t = 0f
    private var wallpaper: Bitmap? = null

    private class Particle(var x: Float, var y: Float, var vx: Float, var vy: Float,
                           var r: Float, var life: Float)
    private val parts = MutableList(70) { newParticle(true) }

    init {
        // Still images only. If you chose a video, the gradient is used instead.
        if (wpUri != null) {
            try {
                val st = ctx.contentResolver.openInputStream(Uri.parse(wpUri))
                wallpaper = BitmapFactory.decodeStream(st); st?.close()
            } catch (e: Exception) { wallpaper = null }
        }
    }

    private fun newParticle(anywhere: Boolean): Particle {
        val w = if (width > 0) width.toFloat() else 1080f
        val h = if (height > 0) height.toFloat() else 1920f
        return Particle(Random.nextFloat() * w,
            if (anywhere) Random.nextFloat() * h else h + 20f,
            (Random.nextFloat() - 0.5f) * 1.5f, -(0.5f + Random.nextFloat() * 2.5f),
            2f + Random.nextFloat() * 5f, Random.nextFloat())
    }

    // Colors per theme: top, bottom, particle, glow
    private fun palette(): IntArray = when (theme) {
        "naruto" -> intArrayOf(0xFFFF8A00.toInt(), 0xFF3A1C00.toInt(), 0xFFFFD27F.toInt(), 0xFF3DA5FF.toInt())
        "sasuke_chidori" -> intArrayOf(0xFF0B1030.toInt(), 0xFF000000.toInt(), 0xFF9FD8FF.toInt(), 0xFF5CC8FF.toInt())
        "one_piece" -> intArrayOf(0xFF2BB5FF.toInt(), 0xFF003A66.toInt(), 0xFFFFFFFF.toInt(), 0xFFFFD84D.toInt())
        "jujutsu_kaisen" -> intArrayOf(0xFF1A0033.toInt(), 0xFF000000.toInt(), 0xFF8A2BE2.toInt(), 0xFF3D8BFF.toInt())
        "dragon_ball" -> intArrayOf(0xFFFFB300.toInt(), 0xFF8A2A00.toInt(), 0xFFFFF59D.toInt(), 0xFF4DD0FF.toInt())
        "demon_slayer" -> intArrayOf(0xFF002B3A.toInt(), 0xFF000000.toInt(), 0xFF3DDBFF.toInt(), 0xFFFF4D4D.toInt())
        "attack_on_titan" -> intArrayOf(0xFF3B4A3B.toInt(), 0xFF0B0F0B.toInt(), 0xFFCFCFCF.toInt(), 0xFFFF6A00.toInt())
        "my_hero_academia" -> intArrayOf(0xFF00B84D.toInt(), 0xFF002B14.toInt(), 0xFFB6FF6A.toInt(), 0xFF39FF88.toInt())
        "chainsaw_man" -> intArrayOf(0xFF8A0000.toInt(), 0xFF000000.toInt(), 0xFFFF3B3B.toInt(), 0xFFFF8A00.toInt())
        else -> intArrayOf(0xFF05001A.toInt(), 0xFF000000.toInt(), 0xFFFFFFFF.toInt(), 0xFF8A5CFF.toInt())
    }

    override fun onDraw(c: Canvas) {
        super.onDraw(c)
        t += 0.03f
        val w = width.toFloat(); val h = height.toFloat()
        val pal = palette()

        // 1. Background: your wallpaper if set, else gradient
        val wp = wallpaper
        if (wp != null) {
            val scale = max(w / wp.width, h / wp.height)
            val m = Matrix().apply {
                postScale(scale, scale)
                postTranslate((w - wp.width * scale) / 2, (h - wp.height * scale) / 2)
            }
            c.drawBitmap(wp, m, null)
            p.shader = null; p.color = 0x66000000; c.drawRect(0f, 0f, w, h, p)
        } else {
            p.shader = LinearGradient(0f, 0f, 0f, h, pal[0], pal[1], Shader.TileMode.CLAMP)
            c.drawRect(0f, 0f, w, h, p); p.shader = null
        }

        // 2. Floating particles (embers, petals, sparks, stars)
        p.style = Paint.Style.FILL
        for (q in parts) {
            q.x += q.vx + sin(t + q.life * 6) * 0.6f; q.y += q.vy
            if (q.y < -20f) { val n = newParticle(false); q.x = n.x; q.y = n.y; q.vx = n.vx; q.vy = n.vy }
            p.color = pal[2]; p.alpha = (120 + 100 * sin(t * 2 + q.life * 9)).toInt().coerceIn(30, 220)
            c.drawCircle(q.x, q.y, q.r, p)
        }
        p.alpha = 255

        // 3. Signature effect
        val cx = w / 2; val cy = h * 0.38f
        when (theme) {
            "naruto" -> rasengan(c, cx, cy, pal[3])
            "sasuke_chidori" -> chidori(c, cx, cy, pal[3])
            "one_piece" -> waves(c, w, h, pal[3])
            "jujutsu_kaisen" -> domain(c, cx, cy, pal[2], pal[3])
            "dragon_ball" -> kamehameha(c, w, cy, pal[3])
            "demon_slayer" -> waterBreathing(c, w, h, pal[2], pal[3])
            "attack_on_titan" -> odmSlash(c, w, h, pal[3])
            "my_hero_academia" -> plusUltra(c, cx, cy, pal[2], pal[3])
            "chainsaw_man" -> chainsaw(c, cx, cy, pal[2], pal[3])
            else -> galaxy(c, cx, cy, pal[3])
        }
        postInvalidateOnAnimation()
    }

    // ---------- Effects ----------

    private fun glow(c: Canvas, x: Float, y: Float, r: Float, col: Int) {
        p.shader = RadialGradient(x, y, r, intArrayOf(col, (col and 0x00FFFFFF)),
            null, Shader.TileMode.CLAMP)
        c.drawCircle(x, y, r, p); p.shader = null
    }

    // Naruto: spinning blue Rasengan
    private fun rasengan(c: Canvas, cx: Float, cy: Float, col: Int) {
        glow(c, cx, cy, 260f + 15 * sin(t * 3), (0x88 shl 24) or (col and 0xFFFFFF))
        p.style = Paint.Style.STROKE; p.strokeCap = Paint.Cap.ROUND
        for (i in 0 until 14) {
            val a0 = t * (4 + i * 0.3f) + i
            val r = 30f + i * 11f
            p.color = Color.WHITE; p.alpha = 230 - i * 10; p.strokeWidth = 5f
            c.drawArc(cx - r, cy - r, cx + r, cy + r, Math.toDegrees(a0.toDouble()).toFloat(), 110f, false, p)
        }
        p.alpha = 255; p.style = Paint.Style.FILL
        p.color = Color.WHITE; c.drawCircle(cx, cy, 24f, p)
    }

    // Sasuke: crackling Chidori lightning
    private fun chidori(c: Canvas, cx: Float, cy: Float, col: Int) {
        glow(c, cx, cy, 240f, (0x77 shl 24) or (col and 0xFFFFFF))
        p.style = Paint.Style.STROKE; p.strokeCap = Paint.Cap.ROUND
        val rnd = Random((t * 12).toInt())
        for (b in 0 until 14) {
            var x = cx; var y = cy
            var ang = rnd.nextFloat() * 6.283f
            val path = Path().apply { moveTo(x, y) }
            for (s in 0 until 7) {
                ang += (rnd.nextFloat() - 0.5f) * 1.2f
                x += cos(ang) * 38f; y += sin(ang) * 38f
                path.lineTo(x, y)
            }
            p.color = Color.WHITE; p.strokeWidth = 3f; p.alpha = 255; c.drawPath(path, p)
            p.color = col; p.strokeWidth = 8f; p.alpha = 90; c.drawPath(path, p)
        }
        p.alpha = 255; p.style = Paint.Style.FILL
        p.color = Color.WHITE; c.drawCircle(cx, cy, 20f, p)
    }

    // One Piece: rolling sea waves + sun
    private fun waves(c: Canvas, w: Float, h: Float, sun: Int) {
        glow(c, w * 0.8f, h * 0.18f, 160f, (0xAA shl 24) or (sun and 0xFFFFFF))
        p.style = Paint.Style.FILL
        for (l in 0 until 4) {
            val path = Path().apply { moveTo(0f, h) }
            var x = 0f
            while (x <= w + 20) {
                path.lineTo(x, h * (0.68f + l * 0.07f) + sin(x / 90f + t * (1 + l * 0.4f)) * (16f + l * 6f))
                x += 20f
            }
            path.lineTo(w, h); path.close()
            p.color = Color.argb(150, 0, 60 + l * 25, 130 + l * 20); c.drawPath(path, p)
        }
    }

    // Jujutsu Kaisen: Domain Expansion rings + cursed energy
    private fun domain(c: Canvas, cx: Float, cy: Float, c1: Int, c2: Int) {
        p.style = Paint.Style.STROKE
        for (i in 0 until 6) {
            val r = ((t * 90 + i * 70) % 420f)
            p.color = if (i % 2 == 0) c1 else c2
            p.alpha = (255 - r / 420f * 255).toInt(); p.strokeWidth = 6f
            c.drawCircle(cx, cy, r, p)
        }
        p.alpha = 255; p.style = Paint.Style.FILL
        p.color = Color.BLACK; c.drawCircle(cx, cy, 36f, p)
    }

    // Dragon Ball: charging Kamehameha beam + aura
    private fun kamehameha(c: Canvas, w: Float, cy: Float, col: Int) {
        val cx = w * 0.25f
        val pulse = 90f + 20 * sin(t * 5)
        glow(c, cx, cy, pulse * 2f, (0x99 shl 24) or (col and 0xFFFFFF))
        p.style = Paint.Style.FILL; p.color = Color.WHITE; c.drawCircle(cx, cy, pulse * 0.45f, p)
        val len = w * (0.4f + 0.55f * ((sin(t * 1.5f) + 1) / 2))
        p.shader = LinearGradient(cx, cy, cx + len, cy, Color.WHITE, (col and 0x00FFFFFF),
            Shader.TileMode.CLAMP)
        c.drawRoundRect(cx, cy - 34f, cx + len, cy + 34f, 34f, 34f, p); p.shader = null
    }

    // Demon Slayer: Water Breathing swirls
    private fun waterBreathing(c: Canvas, w: Float, h: Float, c1: Int, c2: Int) {
        p.style = Paint.Style.STROKE; p.strokeCap = Paint.Cap.ROUND
        for (i in 0 until 5) {
            val path = Path()
            var x = -50f
            while (x < w + 50) {
                val y = h * (0.25f + i * 0.13f) + sin(x / 70f + t * 2 + i) * 40f
                if (x < 0) path.moveTo(x, y) else path.lineTo(x, y)
                x += 14f
            }
            p.color = c1; p.alpha = 180; p.strokeWidth = 7f; c.drawPath(path, p)
            p.color = c2; p.alpha = 60; p.strokeWidth = 16f; c.drawPath(path, p)
        }
        p.alpha = 255
    }

    // Attack on Titan: ODM gear slash lines + wings
    private fun odmSlash(c: Canvas, w: Float, h: Float, col: Int) {
        p.style = Paint.Style.STROKE; p.strokeCap = Paint.Cap.ROUND
        for (i in 0 until 3) {
            val phase = (t * 0.7f + i * 0.33f) % 1f
            val x1 = w * phase * 1.3f - w * 0.2f
            val y1 = h * (0.2f + i * 0.2f)
            p.color = Color.WHITE; p.alpha = (255 * (1 - phase)).toInt(); p.strokeWidth = 5f
            c.drawLine(x1, y1, x1 + 260f, y1 + 160f, p)
            p.color = col; p.alpha = (120 * (1 - phase)).toInt(); p.strokeWidth = 14f
            c.drawLine(x1, y1, x1 + 260f, y1 + 160f, p)
        }
        p.alpha = 255
    }

    // My Hero Academia: Plus Ultra star burst
    private fun plusUltra(c: Canvas, cx: Float, cy: Float, c1: Int, c2: Int) {
        glow(c, cx, cy, 250f, (0x66 shl 24) or (c2 and 0xFFFFFF))
        p.style = Paint.Style.STROKE; p.strokeCap = Paint.Cap.ROUND
        for (i in 0 until 12) {
            val a = i * 0.5236f + t * 0.5f
            val r1 = 60f + 20 * sin(t * 4 + i); val r2 = 200f + 40 * sin(t * 3 + i * 2)
            p.color = if (i % 2 == 0) c1 else c2; p.strokeWidth = 8f
            c.drawLine(cx + cos(a) * r1, cy + sin(a) * r1, cx + cos(a) * r2, cy + sin(a) * r2, p)
        }
    }

    // Chainsaw Man: spinning chainsaw blades
    private fun chainsaw(c: Canvas, cx: Float, cy: Float, c1: Int, c2: Int) {
        glow(c, cx, cy, 230f, (0x66 shl 24) or (c2 and 0xFFFFFF))
        p.style = Paint.Style.STROKE
        p.color = c1; p.strokeWidth = 10f; c.drawCircle(cx, cy, 130f, p)
        p.style = Paint.Style.FILL; p.color = Color.WHITE
        for (i in 0 until 24) {
            val a = i * 0.2618f + t * 2.2f
            val x = cx + cos(a) * 130f; val y = cy + sin(a) * 130f
            val path = Path().apply {
                moveTo(x + cos(a) * 26f, y + sin(a) * 26f)
                lineTo(x + cos(a + 1.9f) * 12f, y + sin(a + 1.9f) * 12f)
                lineTo(x + cos(a - 1.9f) * 12f, y + sin(a - 1.9f) * 12f); close()
            }
            c.drawPath(path, p)
        }
    }

    // Space: rotating galaxy spiral
    private fun galaxy(c: Canvas, cx: Float, cy: Float, col: Int) {
        glow(c, cx, cy, 280f, (0x55 shl 24) or (col and 0xFFFFFF))
        p.style = Paint.Style.FILL
        for (i in 0 until 260) {
            val a = i * 0.11f + t * 0.4f
            val r = 4f + i * 0.9f
            p.color = Color.WHITE; p.alpha = (40 + (i % 5) * 40).coerceAtMost(220)
            c.drawCircle(cx + cos(a) * r, cy + sin(a) * r * 0.6f, 2f + (i % 3), p)
        }
        p.alpha = 255
    }
}
