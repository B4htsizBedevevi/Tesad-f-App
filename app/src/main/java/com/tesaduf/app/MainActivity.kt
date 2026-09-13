package com.tesaduf.app

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import java.util.Locale
import kotlin.random.Random

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
        val root = baseRoot()
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
        }
        val title = label("TESADÜF", 38f, white, Typeface.BOLD)
        val subtitle = label("İyi sohbetler tesadüfen başlar.", 15f, muted, Typeface.NORMAL)
        box.addView(title)
        box.addView(subtitle, lp(0, 44, 1, 0))
        root.addView(box, lp(-1, -2, 1, 0))
        setContentView(root)
        root.postDelayed({
            if (prefs.getString("anonymous_id", null) == null) showProfileSetup() else showHome()
        }, 700)
    }

    private fun showProfileSetup() {
        val root = baseRoot()
        val scroll = ScrollView(this).apply { isFillViewport = true }
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(24), dp(32), dp(24), dp(28))
        }

        content.addView(label("Önce seni tanıyalım", 28f, white, Typeface.BOLD))
        content.addView(label("Kayıt yok. İsim, telefon veya e-posta istemiyoruz. Sana sadece anonim bir kimlik veriyoruz.", 15f, muted), lp(-1, -2, 1, 12))

        val idCard = roundedCard()
        idCard.addView(label("ANONİM KİMLİĞİN", 11f, muted, Typeface.BOLD))
        val id = generateAnonymousId()
        idCard.addView(label(id, 30f, white, Typeface.BOLD), lp(-1, -2, 1, 6))
        idCard.addView(label("Bu kodla sohbetlerde görünürsün.", 13f, muted), lp(-1, -2, 1, 2))
        content.addView(idCard, lp(-1, -2, 1, 20))

        content.addView(label("Avatarını seç", 18f, white, Typeface.BOLD), lp(-1, -2, 1, 4))
        content.addView(label("İstersen daha sonra değiştirebilirsin.", 13f, muted), lp(-1, -2, 1, 10))

        val avatars = listOf("🌙", "⚡", "🎧", "🐺", "🦊", "🌌", "🎮", "🪐")
        val avatarRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        var selected = prefs.getString("avatar", avatars[0]) ?: avatars[0]
        val avatarViews = mutableListOf<TextView>()
        avatars.forEach { avatar ->
            val v = avatarButton(avatar, avatar == selected)
            v.setOnClickListener {
                selected = avatar
                avatarViews.forEach { updateAvatarState(it, it.tag == selected) }
            }
            v.tag = avatar
            avatarViews.add(v)
            avatarRow.addView(v, lp(0, dp(58), 1, 5))
        }
        content.addView(avatarRow, lp(-1, -2, 1, 18))

        val continueButton = primaryButton("TESADÜFE BAŞLA")
        continueButton.setOnClickListener {
            prefs.edit().putString("anonymous_id", id).putString("avatar", selected).apply()
            showHome()
        }
        content.addView(continueButton, lp(-1, dp(54), 1, 16))
        content.addView(label("Kimlik bilgilerin cihazında anonim olarak saklanır.", 12f, muted, Typeface.NORMAL).apply { gravity = Gravity.CENTER }, lp(-1, -2, 1, 12))

        scroll.addView(content)
        root.addView(scroll, lp(-1, -1, 1, 0))
        setContentView(root)
    }

    private fun showHome() {
        val root = baseRoot()
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(26), dp(22), dp(18))
        }

        val top = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL }
        val avatar = label(prefs.getString("avatar", "🌙") ?: "🌙", 28f, white)
        top.addView(avatar, lp(dp(48), dp(48), 0, 0))
        val identity = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        identity.addView(label("TESADÜF", 20f, white, Typeface.BOLD))
        identity.addView(label(prefs.getString("anonymous_id", "#?????") ?: "#?????", 13f, muted, Typeface.NORMAL), lp(-1, -2, 1, 2))
        top.addView(identity, lp(0, -2, 1, 12))
        content.addView(top)

        content.addView(label("Bugün kimin hikâyesine denk geleceksin?", 28f, white, Typeface.BOLD), lp(-1, -2, 1, 34))

        val hero = roundedCard()
        hero.gravity = Gravity.CENTER
        hero.addView(label("✦", 46f, accent2, Typeface.BOLD).apply { gravity = Gravity.CENTER })
        hero.addView(label("Bir tesadüf başlat.", 21f, white, Typeface.BOLD).apply { gravity = Gravity.CENTER }, lp(-1, -2, 1, 8))
        hero.addView(label("Rastgele biriyle anonim bir sohbet.\n15 dakika. Gerisini tesadüfe bırak.", 14f, muted).apply { gravity = Gravity.CENTER }, lp(-1, -2, 1, 4))
        val start = primaryButton("TESADÜFÜ BAŞLAT")
        start.setOnClickListener {
            Toast.makeText(this, "Eşleştirme altyapısını şimdi bağlıyoruz. Bu ekran hazır. 🚀", Toast.LENGTH_SHORT).show()
        }
        hero.addView(start, lp(-1, dp(52), 1, 20))
        content.addView(hero, lp(-1, 0, 1, 20))

        val privacy = roundedCard()
        privacy.addView(label("ANONİMLİK ÖNCELİĞİ", 11f, accent2, Typeface.BOLD))
        privacy.addView(label("Gerçek ad yok • Profil fotoğrafı yok • Telefon yok", 14f, white), lp(-1, -2, 1, 7))
        privacy.addView(label("Sadece sohbet. Sadece o an.", 13f, muted), lp(-1, -2, 1, 2))
        content.addView(privacy, lp(-1, -2, 1, 0))

        val footer = label("Ayarlar   •   Engellenenler   •   Güvenlik", 12f, muted)
        footer.gravity = Gravity.CENTER
        content.addView(footer, lp(-1, -2, 1, 18))

        root.addView(content, lp(-1, -1, 1, 0))
        setContentView(root)
    }

    private fun generateAnonymousId(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        return buildString {
            append('#')
            repeat(5) { append(chars[Random.nextInt(chars.length)]) }
        }.uppercase(Locale.ROOT)
    }

    private fun baseRoot() = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER
        setBackgroundColor(bg)
    }

    private fun roundedCard(): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(18), dp(18), dp(18), dp(18))
        background = GradientDrawable().apply {
            setColor(card)
            cornerRadius = dp(22).toFloat()
        }
    }

    private fun avatarButton(symbol: String, selected: Boolean): TextView = TextView(this).apply {
        text = symbol
        textSize = 25f
        gravity = Gravity.CENTER
        updateAvatarState(this, selected)
    }

    private fun updateAvatarState(view: TextView, selected: Boolean) {
        view.background = GradientDrawable().apply {
            setColor(if (selected) Color.rgb(42, 52, 92) else card)
            cornerRadius = dp(18).toFloat()
            if (selected) setStroke(dp(2), accent)
        }
    }

    private fun primaryButton(text: String) = Button(this).apply {
        this.text = text
        textSize = 14f
        setTextColor(white)
        typeface = Typeface.DEFAULT_BOLD
        isAllCaps = false
        background = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, intArrayOf(accent2, accent)).apply {
            cornerRadius = dp(17).toFloat()
        }
    }

    private fun label(text: String, size: Float, color: Int, style: Int = Typeface.NORMAL) = TextView(this).apply {
        this.text = text
        textSize = size
        setTextColor(color)
        typeface = Typeface.create(Typeface.DEFAULT, style)
        setLineSpacing(0f, 1.08f)
    }

    private fun lp(width: Int, height: Int, weight: Int, margin: Int): LinearLayout.LayoutParams =
        LinearLayout.LayoutParams(width, height, weight.toFloat()).apply {
            if (margin > 0) setMargins(0, dp(margin), 0, 0)
        }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
