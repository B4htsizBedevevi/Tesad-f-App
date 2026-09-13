package com.tesaduf.app

import android.content.Context
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class SupabaseApi(context: Context) {
    private val prefs = context.getSharedPreferences("tesaduf_session", Context.MODE_PRIVATE)
    private val key = BuildConfig.API_KEY
    private val base = "https://wihagxhijxccxnaktnel.supabase.co"

    data class Result(val ok: Boolean, val json: JSONObject, val error: String? = null)

    fun bootstrap(): Result { ensureSession(); return call("bootstrap", JSONObject()) }
    fun findTextMatch(): Result { ensureSession(); return call("matchmaker", JSONObject().put("mode", "text").put("mood", "random")) }
    fun matchStatus(matchId: String): Result { ensureSession(); return get("/functions/v1/match-status?match_id=$matchId", prefs.getString("access", null)) }

    private fun ensureSession() {
        if (key.isBlank()) error("Backend anahtarı yapılandırılmamış")
        if (!prefs.getString("access", null).isNullOrBlank()) return
        val r = post("/auth/v1/signup", JSONObject(), null)
        if (!r.ok) error(r.error ?: "Anonim oturum açılamadı")
        save(r.json)
    }

    private fun call(name: String, body: JSONObject): Result = post("/functions/v1/$name", body, prefs.getString("access", null))

    private fun post(path: String, body: JSONObject, token: String?): Result {
        var r = raw(path, "POST", body, token)
        if (!r.ok && token != null && r.json.optInt("code") == 401 && refresh()) r = raw(path, "POST", body, prefs.getString("access", null))
        if (r.ok) save(r.json)
        return r
    }

    private fun get(path: String, token: String?): Result {
        var r = raw(path, "GET", JSONObject(), token)
        if (!r.ok && token != null && r.json.optInt("code") == 401 && refresh()) r = raw(path, "GET", JSONObject(), prefs.getString("access", null))
        return r
    }

    private fun raw(path: String, method: String, body: JSONObject, token: String?): Result {
        val c = (URL(base + path).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 12000
            readTimeout = 15000
            doOutput = method == "POST"
            setRequestProperty("apikey", key)
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Accept", "application/json")
            if (!token.isNullOrBlank()) setRequestProperty("Authorization", "Bearer $token")
        }
        return try {
            if (method == "POST") c.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
            val code = c.responseCode
            val stream = if (code in 200..299) c.inputStream else c.errorStream
            val text = stream?.let { BufferedReader(InputStreamReader(it)).use(BufferedReader::readText) } ?: "{}"
            val json = runCatching { JSONObject(text) }.getOrElse { JSONObject().put("message", text) }
            Result(code in 200..299, json, if (code in 200..299) null else json.optString("message", "HTTP $code"))
        } finally { c.disconnect() }
    }

    private fun refresh(): Boolean {
        val refresh = prefs.getString("refresh", null) ?: return false
        val r = raw("/auth/v1/token?grant_type=refresh_token", "POST", JSONObject().put("refresh_token", refresh), null)
        if (!r.ok) return false
        save(r.json)
        return true
    }

    private fun save(json: JSONObject) {
        val e = prefs.edit()
        json.optString("access_token").takeIf { it.isNotBlank() }?.let { e.putString("access", it) }
        json.optString("refresh_token").takeIf { it.isNotBlank() }?.let { e.putString("refresh", it) }
        e.apply()
    }
}
