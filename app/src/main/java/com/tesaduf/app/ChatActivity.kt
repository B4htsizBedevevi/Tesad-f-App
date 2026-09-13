package com.tesaduf.app

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Button
import android.graphics.drawable.GradientDrawable
import java.util.concurrent.Executors

class ChatActivity : Activity() {
    private val executor = Executors.newSingleThreadExecutor()
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var messagesBox: LinearLayout
    private lateinit var input: EditText
    private var running = true
    private lateinit var matchId: String
    private lateinit var partner: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        matchId = intent.getStringExtra("match_id") ?: run { finish(); return }
        partner = intent.getStringExtra("partner_id") ?: "Tesadüf"
        window.statusBarColor = Color.rgb(7,9,20)
        window.navigationBarColor = Color.rgb(7,9,20)
        setContentView(screen())
        refreshMessages()
    }

    private fun screen(): LinearLayout {
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(Color.rgb(7,9,20)) }
        val header = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL; setPadding(20,18,20,14) }
        val name = TextView(this).apply { text = partner; textSize = 20f; typeface = Typeface.DEFAULT_BOLD; setTextColor(Color.WHITE) }
        val sub = TextView(this).apply { text = "15 dakikalık tesadüf"; textSize = 12f; setTextColor(Color.rgb(170,174,195)) }
        val titles = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; addView(name); addView(sub) }
        header.addView(titles, LinearLayout.LayoutParams(0,-2,1f))
        val end = Button(this).apply { text = "Bitir"; isAllCaps = false; setTextColor(Color.WHITE); background = GradientDrawable().apply { setColor(Color.rgb(17,20,38)); cornerRadius = 16f }; setOnClickListener { finish() } }
        header.addView(end, LinearLayout.LayoutParams(90,50))
        root.addView(header)

        val scroll = ScrollView(this).apply { fillViewport = true }
        messagesBox = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(18,10,18,18) }
        scroll.addView(messagesBox)
        root.addView(scroll, LinearLayout.LayoutParams(-1,0,1f))

        val composer = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL; setPadding(14,10,14,14) }
        input = EditText(this).apply {
            hint = "Bir şey yaz…"; textSize = 15f; setTextColor(Color.WHITE); setHintTextColor(Color.rgb(130,134,155)); maxLines = 4
            background = GradientDrawable().apply { setColor(Color.rgb(17,20,38)); cornerRadius = 18f; setStroke(1,Color.rgb(40,44,68)) }
            setPadding(16,0,16,0)
        }
        val send = Button(this).apply { text = "Gönder"; isAllCaps = false; setTextColor(Color.WHITE); background = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, intArrayOf(Color.rgb(42,190,255),Color.rgb(105,91,255))).apply { cornerRadius = 18f }; setOnClickListener { send() } }
        composer.addView(input, LinearLayout.LayoutParams(0,56,1f).apply { setMargins(0,0,8,0) })
        composer.addView(send, LinearLayout.LayoutParams(88,56))
        root.addView(composer)
        return root
    }

    private fun refreshMessages() {
        executor.execute {
            val r = runCatching { SupabaseApi(this).messages(matchId) }.getOrNull() ?: return@execute
            if (!r.ok) return@execute
            val arr = r.json.optJSONArray("messages") ?: return@execute
            handler.post {
                messagesBox.removeAllViews()
                for (i in 0 until arr.length()) {
                    val m = arr.optJSONObject(i) ?: continue
                    addMessage(m.optString("body"), m.optString("sender_id"))
                }
            }
        }
        handler.postDelayed({ if (running) refreshMessages() }, 2500)
    }

    private fun addMessage(text: String, sender: String) {
        val mine = sender.isNotBlank() && sender == "me"
        val v = TextView(this).apply { this.text = text; textSize = 15f; setTextColor(Color.WHITE); setPadding(14,10,14,10); background = GradientDrawable().apply { setColor(if (mine) Color.rgb(53,47,105) else Color.rgb(17,20,38)); cornerRadius = 18f } }
        val row = LinearLayout(this).apply { gravity = if (mine) Gravity.END else Gravity.START }
        row.addView(v, LinearLayout.LayoutParams(-2,-2).apply { setMargins(0,0,0,8) })
        messagesBox.addView(row)
    }

    private fun send() {
        val text = input.text.toString().trim()
        if (text.isEmpty()) return
        input.setText("")
        executor.execute {
            val r = runCatching { SupabaseApi(this).sendMessage(matchId, text) }.getOrNull()
            handler.post {
                if (r?.ok == true) refreshMessages()
                else { input.setText(text) }
            }
        }
    }

    override fun onDestroy() { running = false; executor.shutdownNow(); super.onDestroy() }
}
