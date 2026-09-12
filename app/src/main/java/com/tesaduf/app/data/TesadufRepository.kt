package com.tesaduf.app.data

import com.tesaduf.app.BuildConfig
import com.tesaduf.app.model.DestinyResponse
import com.tesaduf.app.model.MatchResponse
import com.tesaduf.app.model.Message
import com.tesaduf.app.model.MessagesResponse
import com.tesaduf.app.model.ProfileResponse
import com.tesaduf.app.model.SendMessageResponse
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class TesadufRepository {
    private val http = HttpClient(Android)
    private val json = Json { ignoreUnknownKeys = true }

    private suspend inline fun <reified T> call(
        function: String,
        method: HttpMethod = HttpMethod.Post,
        body: JsonObject? = null,
        params: Map<String, String> = emptyMap()
    ): T {
        val session = Supabase.client.auth.currentSessionOrNull()
            ?: error("Oturum bulunamadı. Lütfen tekrar deneyin.")
        val response = http.request("${BuildConfig.SUPABASE_URL}/functions/v1/$function") {
            this.method = method
            header("apikey", BuildConfig.SUPABASE_KEY)
            bearerAuth(session.accessToken)
            params.forEach { (key, value) -> parameter(key, value) }
            if (body != null) {
                contentType(ContentType.Application.Json)
                setBody(body.toString())
            }
        }
        val text = response.bodyAsText()
        if (!response.status.isSuccess()) {
            error("Sunucu hatası ${response.status.value}: $text")
        }
        return json.decodeFromString(text)
    }

    suspend fun bootstrap(): ProfileResponse = call("bootstrap")

    suspend fun match(): MatchResponse = call(
        function = "matchmaker",
        body = buildJsonObject { put("mode", "text") }
    )

    suspend fun status(id: String): MatchResponse = call(
        function = "match-status",
        method = HttpMethod.Get,
        params = mapOf("match_id" to id)
    )

    suspend fun getMessages(id: String): List<Message> = call<MessagesResponse>(
        function = "messages",
        method = HttpMethod.Get,
        params = mapOf("match_id" to id)
    ).messages

    suspend fun sendMessage(id: String, text: String): SendMessageResponse = call(
        function = "send-message",
        body = buildJsonObject {
            put("match_id", id)
            put("body", text)
        }
    )

    suspend fun destiny(id: String, keep: Boolean): DestinyResponse = call(
        function = "destiny-decision",
        body = buildJsonObject {
            put("match_id", id)
            put("keep", keep)
        }
    )

    suspend fun end(id: String) {
        call<JsonObject>(
            function = "end-match",
            body = buildJsonObject { put("match_id", id) }
        )
    }
}
