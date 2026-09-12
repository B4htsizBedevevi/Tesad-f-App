package com.tesaduf.app.data
import com.tesaduf.app.model.*
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
class TesadufRepository{private val sb get()=Supabase.client
 suspend fun bootstrap()=sb.functions.invoke("bootstrap").body<ProfileResponse>()
 suspend fun match()=sb.functions.invoke("matchmaker",buildJsonObject{put("mode","text")}).body<MatchResponse>()
 suspend fun status(id:String)=sb.functions.invoke("match-status?match_id=$id").body<MatchResponse>()
 suspend fun getMessages(id:String)=sb.functions.invoke("messages?match_id=$id").body<MessagesResponse>().messages
 suspend fun sendMessage(id:String,text:String)=sb.functions.invoke("send-message",buildJsonObject{put("match_id",id);put("body",text)}).body<SendMessageResponse>()
 suspend fun destiny(id:String,keep:Boolean)=sb.functions.invoke("destiny-decision",buildJsonObject{put("match_id",id);put("keep",keep)}).body<DestinyResponse>()
 suspend fun end(id:String)=sb.functions.invoke("end-match",buildJsonObject{put("match_id",id)})
}
