package com.tesaduf.app.data

import com.tesaduf.app.BuildConfig
import com.tesaduf.app.model.*
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
    private val http=HttpClient(Android)
    private val json=Json{ignoreUnknownKeys=true}

    private suspend inline fun <reified T> call(function:String,method:HttpMethod=HttpMethod.Post,body:JsonObject?=null,params:Map<String,String> = emptyMap()):T{
        val session=Supabase.client.auth.currentSessionOrNull() ?: error("Oturum bulunamadı.")
        val response=http.request("${BuildConfig.SUPABASE_URL}/functions/v1/${function}"){
            this.method=method
            header("apikey",BuildConfig.SUPABASE_KEY)
            bearerAuth(session.accessToken)
            params.forEach{(k,v)->parameter(k,v)}
            if(body!=null){contentType(ContentType.Application.Json);setBody(body.toString())}
        }
        val text=response.bodyAsText()
        if(!response.status.isSuccess()) error("Sunucu hatası ${response.status.value}: $text")
        return json.decodeFromString(text)
    }

    private suspend fun callNoContent(function:String,body:JsonObject){
        val session=Supabase.client.auth.currentSessionOrNull() ?: error("Oturum bulunamadı.")
        val response=http.request("${BuildConfig.SUPABASE_URL}/functions/v1/${function}"){
            method=HttpMethod.Post
            header("apikey",BuildConfig.SUPABASE_KEY)
            bearerAuth(session.accessToken)
            contentType(ContentType.Application.Json)
            setBody(body.toString())
        }
        if(!response.status.isSuccess()) error("Sunucu hatası ${response.status.value}: ${response.bodyAsText()}")
    }

    suspend fun bootstrap():ProfileResponse=call("bootstrap")
    suspend fun match(mood:String?):MatchResponse=call("matchmaker",body=buildJsonObject{put("mode","text");mood?.let{put("mood",it)}})
    suspend fun status(id:String):MatchResponse=call("match-status",HttpMethod.Get,params=mapOf("match_id" to id))
    suspend fun getMessages(id:String):List<Message>=call<MessagesResponse>("messages",HttpMethod.Get,params=mapOf("match_id" to id)).messages
    suspend fun sendMessage(id:String,text:String):SendMessageResponse=call("send-message",body=buildJsonObject{put("match_id",id);put("body",text)})
    suspend fun destiny(id:String,keep:Boolean):DestinyResponse=call("destiny-decision",body=buildJsonObject{put("match_id",id);put("keep",keep)})
    suspend fun end(id:String)=callNoContent("end-match",buildJsonObject{put("match_id",id)})
    suspend fun myChats():ChatsResponse=call("my-chats",HttpMethod.Get)
    suspend fun block(id:String):ActionResponse=call("block-user",body=buildJsonObject{put("blocked_id",id)})
    suspend fun report(id:String,matchId:String?,reason:String):ActionResponse=call("report-user",body=buildJsonObject{put("reported_id",id);matchId?.let{put("match_id",it)};put("reason",reason)})
}
