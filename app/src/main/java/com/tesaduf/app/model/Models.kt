package com.tesaduf.app.model
import kotlinx.serialization.Serializable
@Serializable data class ProfileResponse(val profile:Profile?=null,val created:Boolean?=null,val error:String?=null)
@Serializable data class Profile(val id:String,val anonymous_id:String,val status:String)
@Serializable data class Match(val id:String,val user_a:String,val user_b:String?=null,val mode:String,val status:String,val started_at:String?=null,val expires_at:String?=null,val ended_at:String?=null,val decision_a:Boolean?=null,val decision_b:Boolean?=null)
@Serializable data class MatchResponse(val match:Match?=null,val partner_anonymous_id:String?=null,val matched:Boolean?=null,val reused:Boolean?=null,val error:String?=null)
@Serializable data class Message(val id:String,val match_id:String,val sender_id:String,val body:String,val created_at:String)
@Serializable data class MessagesResponse(val messages:List<Message> = emptyList(),val error:String?=null)
@Serializable data class SendMessageResponse(val message:Message?=null,val error:String?=null)
@Serializable data class DestinyResponse(val status:String,val error:String?=null)
