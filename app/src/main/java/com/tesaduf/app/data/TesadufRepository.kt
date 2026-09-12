package com.tesaduf.app.data

import com.tesaduf.app.model.DestinyResponse
import com.tesaduf.app.model.MatchResponse
import com.tesaduf.app.model.MessagesResponse
import com.tesaduf.app.model.ProfileResponse
import com.tesaduf.app.model.SendMessageResponse
import io.github.jan.supabase.functions.functions
import io.github.jan.supabase.functions.invoke
import io.ktor.client.call.body
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class TesadufRepository {
    private val sb get() = Supabase.client

    suspend fun bootstrap(): ProfileResponse =
        sb.functions.invoke("bootstrap").body()

    suspend fun match(): MatchResponse =
        sb.functions.invoke(
            function = "matchmaker",
            body = buildJsonObject { put("mode", "text") }
        ).body()

    suspend fun status(id: String): MatchResponse =
        sb.functions.invoke("match-status") {
            parameter("match_id", id)
        }.body()

    suspend fun getMessages(id: String): List<com.tesaduf.app.model.Message> =
        sb.functions.invoke("messages") {
            parameter("match_id", id)
        }.body<MessagesResponse>().messages

    suspend fun sendMessage(id: String, text: String): SendMessageResponse =
        sb.functions.invoke(
            function = "send-message",
            body = buildJsonObject {
                put("match_id", id)
                put("body", text)
            }
        ).body()

    suspend fun destiny(id: String, keep: Boolean): DestinyResponse =
        sb.functions.invoke(
            function = "destiny-decision",
            body = buildJsonObject {
                put("match_id", id)
                put("keep", keep)
            }
        ).body()

    suspend fun end(id: String) {
        sb.functions.invoke(
            function = "end-match",
            body = buildJsonObject { put("match_id", id) }
        )
    }
}
