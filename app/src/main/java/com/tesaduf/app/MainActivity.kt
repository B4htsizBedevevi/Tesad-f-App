package com.tesaduf.app

import android.app.Activity
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.LinearGradient
import android.graphics.Shader
import android.graphics.Typeface
import android.view.View
import android.view.animation.OvershootInterpolator
import android.animation.ValueAnimator
import kotlin.math.min
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class MainActivity : Activity() {
    private val prefs by lazy { getSharedPreferences("tesaduf", Context.MODE_PRIVATE) }
    private val bg = Color.rgb(7, 9, 20)
    private val card = Color.rgb(17, 20, 38)
    private val white = Color.WHITE
    private val muted = Color.rgb(170, 174, 195)
    private val accent = Color.rgb(105, 91, 255)
    private val accent2 = Color.rgb(42, 190, 255)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = bg
        window.navigationBarColor = bg
        showSplash()
    }

    private fun showSplash() {
        val splash = TesadufSplashView(this)
        setContentView(splash)
        splash.postDelayed({
            splash.animateExit {
                if (prefs.getString("profile_ready", null) == "yes") showHome() else showProfileSetup()
            }
        }, 1550)
    }

    private inner class TesadufSplashView(context: Context) : View(context) {
        private val cyan = Color.rgb(42, 190, 255)
        private val violet = Color.rgb(110, 90, 255)
        private val pink = Color.rgb(238, 65, 205)
        private val gold = Color.rgb(255, 194, 92)
        private val pathPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeWidth = 7f
        }
        private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = white
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        private val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = muted
            textAlign = Paint.Align.CENTER
        }
        private val starPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = gold }
        private var progress = 0f
        private var pulse = 0f
        private var titleAlpha = 0f
        private var subtitleAlpha = 0f

        init {
            ValueAnimator.ofFloat(0f, 1f).apply {
                duration = 1100
                interpolator = OvershootInterpolator(0.7f)
                addUpdateListener {
                    progress = it.animatedValue as Float
                    invalidate()
                }
                start()
            }
            ValueAnimator.ofFloat(0f, 1f).apply {
                duration = 1400
                repeatCount = ValueAnimator.INFINITE
                addUpdateListener {
                    pulse = it.animatedValue as Float
                    invalidate()
                }
                start()
            }
            postDelayed({
                titleAlpha = 1f
                subtitleAlpha = 1f
                invalidate()
            }, 420)
        }

        override fun onDraw(canvas: Canvas) {
            canvas.drawColor(bg)
            val cx = width / 2f
            val cy = height * 0.40f
            val scale = min(width, height) / 430f
            val rx = 92f * scale
            val ry = 68f * scale

            pathPaint.strokeWidth = 6f * scale

            val glow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.argb((34 + pulse * 28).toInt(), 90, 100, 255)
            }
            canvas.drawCircle(cx, cy, (95f + pulse * 25f) * scale, glow)

            val left = Path().apply {
                moveTo(cx - rx * 1.30f, cy)
                cubicTo(
                    cx - rx * 0.75f, cy - ry,
                    cx + rx * 0.35f, cy + ry,
                    cx + rx * 1.30f, cy
                )
            }
            val right = Path().apply {
                moveTo(cx - rx * 1.30f, cy)
                cubicTo(
                    cx - rx * 0.35f, cy + ry,
                    cx + rx * 0.75f, cy - ry,
                    cx + rx * 1.30f, cy
                )
            }

            pathPaint.color = cyan
            pathPaint.alpha = (180 + progress * 75).toInt()
            drawProgress(canvas, left, progress)

            pathPaint.color = pink
            pathPaint.alpha = (160 + progress * 95).toInt()
            drawProgress(canvas, right, (progress - 0.12f).coerceAtLeast(0f))

            drawStar(canvas, cx, cy - 88f * scale, (18f + pulse * 3f) * scale)

            textPaint.textSize = 40f * scale
            textPaint.alpha = (255f * titleAlpha).toInt()
            canvas.drawText("TESADÜF", cx, height * 0.61f, textPaint)

            subPaint.textSize = 15f * scale
            subPaint.alpha = (255f * subtitleAlpha).toInt()
            canvas.drawText("Her sohbet yeni bir hikâye.", cx, height * 0.67f, subPaint)

            val sparkle = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
            val points = arrayOf(
                floatArrayOf(-0.25f, -0.04f, 3f),
                floatArrayOf(0.26f, -0.02f, 3f),
                floatArrayOf(-0.30f, 0.16f, 2f),
                floatArrayOf(0.31f, 0.17f, 2f)
            )
            points.forEachIndexed { i, s ->
                val alpha = 110 + (100 * (0.5f + 0.5f * kotlin.math.sin(pulse * 6f + i))).toInt()
                sparkle.alpha = (alpha * progress).toInt()
                canvas.drawCircle(cx + width * s[0], cy + height * s[1], s[2] * scale, sparkle)
            }
        }

        private fun drawProgress(canvas: Canvas, path: Path, fraction: Float) {
            if (fraction <= 0f) return
            val pm = android.graphics.PathMeasure(path, false)
            val segment = Path()
            pm.getSegment(0f, pm.length * fraction.coerceAtMost(1f), segment, true)
            canvas.drawPath(segment, pathPaint)
        }

        private fun drawStar(canvas: Canvas, x: Float, y: Float, r: Float) {
            val path = Path()
            for (i in 0 until 8) {
                val angle = Math.toRadians((-90 + i * 45).toDouble())
                val rr = if (i % 2 == 0) r else r * 0.34f
                val px = x + kotlin.math.cos(angle).toFloat() * rr
                val py = y + kotlin.math.sin(angle).toFloat() * rr
                if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
            }
            path.close()
            canvas.drawPath(path, starPaint)
        }

        fun animateExit(onEnd: () -> Unit) {
            animate().alpha(0f).setDuration(380).withEndAction(onEnd).start()
        }
    }

    private fun showProfileSetup() {
        val root = baseRoot()
        val scroll = ScrollView(this).apply { isFillViewport = true }
        val content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(24), dp(32), dp(24), dp(28)) }
        val introLogo = AnimatedLogoView(this)
        content.addView(introLogo, lp(-1, dp(170), 1, 0))
        content.addView(label("Önce seni tanıyalım", 28f, white, Typeface.BOLD))
        content.addView(label("Kayıt yok. İsim, telefon veya e-posta istemiyoruz. Sana sadece anonim bir kimlik veriyoruz.", 15f, muted), lp(-1, -2, 1, 12))
        val idCard = roundedCard()
        idCard.addView(label("ANONİM KİMLİĞİN", 11f, muted, Typeface.BOLD))
        val id = getOrCreateAnonymousId()
        idCard.addView(label(id, 30f, white, Typeface.BOLD), lp(-1, -2, 1, 6))
        idCard.addView(label("Bu kodla sohbetlerde görünürsün.", 13f, muted), lp(-1, -2, 1, 2))
        content.addView(idCard, lp(-1, -2, 1, 20))
        content.addView(label("Avatarını seç", 18f, white, Typeface.BOLD), lp(-1, -2, 1, 4))
        content.addView(label("İstersen daha sonra değiştirebilirsin.", 13f, muted), lp(-1, -2, 1, 10))

        val avatars = listOf("🌙", "⚡", "🎧", "🐺", "🦊", "🌌", "🎮", "🪐")
        val avatarRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER }
        var selected = prefs.getString("avatar", avatars[0]) ?: avatars[0]
        val avatarViews = mutableListOf<TextView>()
        avatars.forEach { avatar ->
            val v = avatarButton(avatar, avatar == selected)
            v.setOnClickListener { selected = avatar; avatarViews.forEach { updateAvatarState(it, it.tag == selected) } }
            v.tag = avatar
            avatarViews.add(v)
            avatarRow.addView(v, lp(0, dp(58), 1, 5))
        }
        content.addView(avatarRow, lp(-1, -2, 1, 18))
        val continueButton = primaryButton("TESADÜFE BAŞLA")
        continueButton.setOnClickListener {
            prefs.edit().putString("anonymous_id", id).putString("avatar", selected).putString("profile_ready", "yes").apply()
            showHome()
        }
        content.addView(continueButton, lp(-1, dp(54), 1, 16))
        content.addView(label("Kimlik bilgilerin cihazında anonim olarak saklanır.", 12f, muted).apply { gravity = Gravity.CENTER }, lp(-1, -2, 1, 12))
        scroll.addView(content)
        root.addView(scroll, lp(-1, -1, 1, 0))
        setContentView(root)
    }

    private fun showHome() {
        val root = baseRoot()
        val content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(22), dp(26), dp(22), dp(18)) }
        val logo = AnimatedLogoView(this)
        content.addView(logo, lp(-1, dp(120), 1, 0))
        val top = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL }
        top.addView(label(prefs.getString("avatar", "🌙") ?: "🌙", 28f, white), lp(dp(48), dp(48), 0, 0))
        val identity = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        identity.addView(label("TESADÜF", 20f, white, Typeface.BOLD))
        identity.addView(label(prefs.getString("anonymous_id", "#????????") ?: "#????????", 13f, muted), lp(-1, -2, 1, 2))
        top.addView(identity, lp(0, -2, 1, 12))
        content.addView(top)
        content.addView(label("Bugün kimin hikâyesine denk geleceksin?", 28f, white, Typeface.BOLD), lp(-1, -2, 1, 34))
        val hero = roundedCard()
        hero.gravity = Gravity.CENTER
        hero.addView(label("✦", 46f, accent2, Typeface.BOLD).apply { gravity = Gravity.CENTER })
        hero.addView(label("Bir tesadüf başlat.", 21f, white, Typeface.BOLD).apply { gravity = Gravity.CENTER }, lp(-1, -2, 1, 8))
        hero.addView(label("Rastgele biriyle anonim bir sohbet.\n15 dakika. Gerisini tesadüfe bırak.", 14f, muted).apply { gravity = Gravity.CENTER }, lp(-1, -2, 1, 4))
        val start = primaryButton("TESADÜFÜ BAŞLAT")
        start.setOnClickListener { startActivity(android.content.Intent(this, MatchActivity::class.java)) }
        hero.addView(start, lp(-1, dp(52), 1, 20))
        content.addView(hero, lp(-1, 0, 1, 20))
        val privacy = roundedCard()
        privacy.addView(label("ANONİMLİK ÖNCELİĞİ", 11f, accent2, Typeface.BOLD))
        privacy.addView(label("Gerçek ad yok • Profil fotoğrafı yok • Telefon yok", 14f, white), lp(-1, -2, 1, 7))
        privacy.addView(label("Sadece sohbet. Sadece o an.", 13f, muted), lp(-1, -2, 1, 2))
        content.addView(privacy, lp(-1, -2, 1, 0))
        val footer = label("Ayarlar   •   Engellenenler   •   Güvenlik", 12f, muted).apply { gravity = Gravity.CENTER }
        content.addView(footer, lp(-1, -2, 1, 18))
        root.addView(content, lp(-1, -1, 1, 0))
        setContentView(root)
    }


    private inner class AnimatedLogoView(context: Context) : View(context) {
        private val leftPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val rightPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
        }
        private val starPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(255, 194, 92) }
        private var phase = 0f

        init {
            ValueAnimator.ofFloat(0f, 1f).apply {
                duration = 1900
                repeatCount = ValueAnimator.INFINITE
                addUpdateListener {
                    phase = it.animatedValue as Float
                    invalidate()
                }
                start()
            }
        }

        override fun onDraw(canvas: Canvas) {
            val cx = width / 2f
            val cy = height * 0.48f
            val s = min(width, height) / 210f
            val bubble = 54f * s
            val drift = kotlin.math.sin(phase * Math.PI * 2.0).toFloat() * 5f * s

            canvas.drawCircle(cx, cy, 84f * s, Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.argb(28, 80, 100, 255)
            })

            leftPaint.shader = LinearGradient(cx-bubble, cy-bubble, cx+bubble, cy+bubble,
                intArrayOf(Color.rgb(30,126,255), Color.rgb(76,44,220)), null, Shader.TileMode.CLAMP)
            rightPaint.shader = LinearGradient(cx+bubble, cy-bubble, cx-bubble, cy+bubble,
                intArrayOf(Color.rgb(255,74,196), Color.rgb(172,30,217)), null, Shader.TileMode.CLAMP)

            canvas.drawOval(RectF(cx-bubble*1.55f, cy-bubble+drift, cx+bubble*0.25f, cy+bubble+drift), leftPaint)
            canvas.drawOval(RectF(cx-bubble*0.25f, cy-bubble-drift, cx+bubble*1.55f, cy+bubble-drift), rightPaint)

            ringPaint.strokeWidth = 7f * s
            ringPaint.shader = LinearGradient(cx-bubble*1.8f, cy, cx+bubble*1.8f, cy,
                intArrayOf(Color.rgb(30,215,255), Color.rgb(142,77,255), Color.rgb(255,84,199)),
                null, Shader.TileMode.CLAMP)

            val a = Path().apply {
                moveTo(cx-bubble*1.55f, cy+drift)
                cubicTo(cx-bubble, cy-bubble, cx+bubble, cy+bubble, cx+bubble*1.55f, cy-drift)
            }
            val b = Path().apply {
                moveTo(cx-bubble*1.55f, cy-drift)
                cubicTo(cx-bubble, cy+bubble, cx+bubble, cy-bubble, cx+bubble*1.55f, cy+drift)
            }
            canvas.drawPath(a, ringPaint)
            canvas.drawPath(b, ringPaint)
            drawStar(canvas, cx, cy-bubble*1.55f, 13f*s + phase*2f*s)
        }

        private fun drawStar(canvas: Canvas, x: Float, y: Float, r: Float) {
            val p = Path()
            for (i in 0 until 8) {
                val ang = Math.toRadians((-90 + i*45).toDouble())
                val rr = if (i % 2 == 0) r else r*0.34f
                val px = x + kotlin.math.cos(ang).toFloat()*rr
                val py = y + kotlin.math.sin(ang).toFloat()*rr
                if (i == 0) p.moveTo(px, py) else p.lineTo(px, py)
            }
            p.close()
            canvas.drawPath(p, starPaint)
        }
    }

    private fun getOrCreateAnonymousId(): String {
        prefs.getString("anonymous_id", null)?.let { return it }
        val installationId = java.util.UUID.randomUUID().toString().replace("-", "")
        val id = "#" + installationId.substring(0, 8).uppercase(java.util.Locale.ROOT)
        prefs.edit().putString("anonymous_id", id).apply()
        return id
    }

    private fun baseRoot() = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER; setBackgroundColor(bg) }
    private fun roundedCard() = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(18), dp(18), dp(18), dp(18)); background = GradientDrawable().apply { setColor(card); cornerRadius = dp(22).toFloat() } }
    private fun avatarButton(symbol: String, selected: Boolean) = TextView(this).apply { text = symbol; textSize = 25f; gravity = Gravity.CENTER; updateAvatarState(this, selected) }
    private fun updateAvatarState(view: TextView, selected: Boolean) { view.background = GradientDrawable().apply { setColor(if (selected) Color.rgb(42,52,92) else card); cornerRadius = dp(18).toFloat(); if (selected) setStroke(dp(2), accent) } }
    private fun primaryButton(text: String) = Button(this).apply { this.text = text; textSize = 14f; setTextColor(white); typeface = Typeface.DEFAULT_BOLD; isAllCaps = false; background = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, intArrayOf(accent2, accent)).apply { cornerRadius = dp(17).toFloat() } }
    private fun label(text: String, size: Float, color: Int, style: Int = Typeface.NORMAL) = TextView(this).apply { this.text = text; textSize = size; setTextColor(color); typeface = Typeface.create(Typeface.DEFAULT, style); setLineSpacing(0f, 1.08f) }
    private fun lp(width: Int, height: Int, weight: Int, margin: Int) = LinearLayout.LayoutParams(width, height, weight.toFloat()).apply { if (margin > 0) setMargins(0, dp(margin), 0, 0) }
    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
