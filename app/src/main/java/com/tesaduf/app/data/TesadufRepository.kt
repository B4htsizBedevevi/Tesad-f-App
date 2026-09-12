package com.tesaduf.app.data

import com.tesaduf.app.model.*
import io.github.jan.supabase.functions.functions
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class TesadufRepository {
    private val sb get() = Supabase.client

    suspend fun bootstrap(): ProfileResponse =
        sb.functions.invoke("bootstrap").body<ProfileResponse>()

    suspend fun match(mode: String="text", mood: String?=null): MatchResponse =
        sb.functions.invoke("matchmaker", buildJsonObject {
            put("mode", mode)
            mood?.let { put("mood", it) }
        }).body()

    suspend fun getMessages(matchId: String): List<Message> =
        sb.functions.invoke("messages?match_id=$matchId").body<MessagesResponse>().messages

    suspend fun sendMessage(matchId: String, body: String): SendMessageResponse =
        sb.functions.invoke("send-message", buildJsonObject {
            put("match_id", matchId)
            put("body", body)
        }).body()

    suspend fun destiny(matchId: String, keep: Boolean): DestinyResponse =
        sb.functions.invoke("destiny-decision", buildJsonObject {
            put("match_id", matchId)
            put("keep", keep)
        }).body()

    suspend fun end(matchId: String) =
        sb.functions.invoke("end-match", buildJsonObject { put("match_id", matchId) })
    
    suspend fun myChats(): ChatsResponse =
        sb.functions.invoke("my-chats").body()
}
