package com.tesaduf.app

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.graphics.drawable.GradientDrawable
import java.util.concurrent.Executors

class MatchActivity : Activity() {
    private val executor = Executors.newSingleThreadExecutor()
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var status: TextView
    private lateinit var detail: TextView
    private var matchId: String? = null
    private var running = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(7, 9, 20)
        window.navigationBarColor = Color.rgb(7, 9, 20)
        setContentView(screen())
        startMatch()
    }

    private fun screen(): LinearLayout {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(32, 24, 32, 24)
            setBackgroundColor(Color.rgb(7, 9, 20))
        }
        val mark = TextView(this).apply { text = "✦"; textSize = 52f; setTextColor(Color.rgb(42,190,255)); gravity = Gravity.CENTER }
        status = TextView(this).apply { text = "Tesadüf aranıyor…"; textSize = 27f; typeface = Typeface.DEFAULT_BOLD; setTextColor(Color.WHITE); gravity = Gravity.CENTER }
        detail = TextView(this).apply { text = "Sana uygun anonim bir sohbet arkadaşı buluyoruz."; textSize = 15f; setTextColor(Color.rgb(170,174,195)); gravity = Gravity.CENTER; setPadding(0, 14, 0, 0) }
        val cancel = Button(this).apply {
            text = "VAZGEÇ"
            textSize = 13f
            isAllCaps = false
            setTextColor(Color.WHITE)
            background = GradientDrawable().apply { setColor(Color.rgb(17,20,38)); cornerRadius = 18f }
            setOnClickListener { finish() }
        }
        root.addView(mark, LinearLayout.LayoutParams(-1, -2))
        root.addView(status, LinearLayout.LayoutParams(-1, -2))
        root.addView(detail, LinearLayout.LayoutParams(-1, -2))
        root.addView(cancel, LinearLayout.LayoutParams(-1, 54).apply { setMargins(0, 44, 0, 0) })
        return root
    }

    private fun startMatch() {
        executor.execute {
            try {
                val localPrefs = getSharedPreferences("tesaduf", Context.MODE_PRIVATE)
                val anonymousId = localPrefs.getString("anonymous_id", null)
                    ?: throw IllegalStateException("Anonim kimlik bulunamadı. Lütfen uygulamayı yeniden başlat.")
                val avatar = localPrefs.getString("avatar", null)
                val avatarKey = avatarKey(avatar)
                val api = SupabaseApi(this)

                handler.post { status.text = "Bağlanıyoruz…"; detail.text = "Anonim kimliğin güvenli şekilde hazırlanıyor." }
                val bootstrap = api.bootstrap(anonymousId, avatarKey)
                if (!bootstrap.ok) throw IllegalStateException(bootstrap.error ?: "Profil başlatılamadı")

                val profile = bootstrap.json.optJSONObject("profile")
                val serverId = profile?.optString("anonymous_id")?.takeIf { it.isNotBlank() }
                if (serverId != null && serverId != anonymousId) {
                    throw IllegalStateException("Anonim kimlik doğrulanamadı. Lütfen tekrar dene.")
                }

                handler.post { status.text = "Tesadüf aranıyor…"; detail.text = "Sana uygun anonim bir sohbet arkadaşı buluyoruz." }
                val result = api.findTextMatch()
                if (!result.ok) throw IllegalStateException(result.error ?: "Eşleştirme başlatılamadı")
                val match = result.json.optJSONObject("match") ?: throw IllegalStateException("Eşleşme bilgisi alınamadı")
                matchId = match.optString("id").takeIf { it.isNotBlank() } ?: throw IllegalStateException("Eşleşme kimliği alınamadı")
                val matched = result.json.optBoolean("matched", false) || match.optString("status") == "active"
                handler.post { if (matched) openChat(result.json) else { status.text = "Birini arıyoruz…"; detail.text = "Bekleyen bir tesadüf var. Birazdan eşleşebilirsiniz." } }
                if (!matched) poll(api)
            } catch (e: Exception) {
                handler.post {
                    status.text = "Bağlantı kurulamadı"
                    detail.text = e.message ?: "Lütfen internet bağlantını kontrol edip tekrar dene."
                }
            }
        }
    }

    private fun avatarKey(avatar: String?): String? = when (avatar) {
        "🌙" -> "avatar_01"
        "⚡" -> "avatar_02"
        "🎧" -> "avatar_03"
        "🐺" -> "avatar_04"
        "🦊" -> "avatar_05"
        "🌌" -> "avatar_06"
        "🎮" -> "avatar_07"
        "🪐" -> "avatar_08"
        else -> null
    }

    private fun poll(api: SupabaseApi) {
        val id = matchId ?: return
        var attempts = 0
        while (running && attempts < 150) {
            Thread.sleep(2000)
            if (!running) return
            val r = runCatching { api.matchStatus(id) }.getOrNull()
            if (r == null || !r.ok) { attempts++; continue }
            val match = r.json.optJSONObject("match") ?: return
            val state = match.optString("status")
            if (state == "active" || state == "destiny") {
                handler.post { openChat(r.json) }
                return
            }
            if (state == "expired" || state == "ended") {
                handler.post { status.text = "Bu tesadüf olmadı"; detail.text = "Hazırsan yeniden deneyebiliriz." }
                return
            }
            attempts++
        }
        handler.post { if (running) detail.text = "Şimdilik kimseyi bulamadık. İstersen tekrar deneyebilirsin." }
    }

    private fun openChat(json: org.json.JSONObject) {
        if (!running) return
        val id = json.optJSONObject("match")?.optString("id")?.takeIf { it.isNotBlank() } ?: matchId ?: return
        val intent = android.content.Intent(this, ChatActivity::class.java)
        intent.putExtra("match_id", id)
        intent.putExtra("partner_id", json.optString("partner_anonymous_id", "Tesadüf"))
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        running = false
        executor.shutdownNow()
        super.onDestroy()
    }
}
