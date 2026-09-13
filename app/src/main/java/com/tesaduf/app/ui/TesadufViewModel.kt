package com.tesaduf.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tesaduf.app.data.Supabase
import com.tesaduf.app.data.TesadufRepository
import com.tesaduf.app.model.ChatItem
import com.tesaduf.app.model.Message
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class TesadufState(
    val anonymousId:String?=null,
    val matchId:String?=null,
    val waitingMatchId:String?=null,
    val matchStatus:String?=null,
    val partnerId:String?=null,
    val partnerUserId:String?=null,
    val expiresAt:String?=null,
    val messages:List<Message> = emptyList(),
    val chats:List<ChatItem> = emptyList(),
    val loading:Boolean=true,
    val searching:Boolean=false,
    val chatsLoading:Boolean=false,
    val error:String?=null,
    val ended:Boolean=false,
    val decisionVisible:Boolean=false,
    val destiny:Boolean=false,
    val currentTab:String="home",
    val selectedMood:String?=null,
    val decisionSent:Boolean=false,
    val waitingForOther:Boolean=false
)

class TesadufViewModel:ViewModel(){
    private val repo=TesadufRepository()
    private val _state=MutableStateFlow(TesadufState())
    val state:StateFlow<TesadufState> = _state

    private var waitingJob:Job?=null
    private var messageJob:Job?=null
    private var statusJob:Job?=null

    init{
        viewModelScope.launch{
            runCatching{
                if(Supabase.client.auth.currentSessionOrNull()==null){
                    Supabase.client.auth.signInAnonymously()
                }
                repo.bootstrap()
            }.onSuccess{response->
                _state.value=_state.value.copy(
                    anonymousId=response.profile?.anonymous_id,
                    loading=false,
                    error=response.error
                )
            }.onFailure{e->
                _state.value=_state.value.copy(loading=false,error=e.message?:"Bağlantı hatası")
            }
        }
    }

    fun tab(tab:String){
        _state.value=_state.value.copy(currentTab=tab,error=null)
        if(tab=="chats") loadChats()
    }

    fun loadChats(){
        viewModelScope.launch{
            _state.value=_state.value.copy(chatsLoading=true,error=null)
            runCatching{repo.myChats()}
                .onSuccess{r->_state.value=_state.value.copy(chats=r.chats,chatsLoading=false)}
                .onFailure{e->_state.value=_state.value.copy(chatsLoading=false,error=e.message)}
        }
    }

    fun openChat(chat:ChatItem){
        waitingJob?.cancel();messageJob?.cancel();statusJob?.cancel()
        viewModelScope.launch{
            runCatching{repo.status(chat.id)}
                .onSuccess{r->
                    val m=r.match?:return@onSuccess
                    val me=Supabase.client.auth.currentSessionOrNull()?.user?.id
                    val partner=if(m.user_a==me)m.user_b else m.user_a
                    if(m.status in listOf("active","destiny")){
                        _state.value=_state.value.copy(
                            currentTab="home",matchId=m.id,waitingMatchId=null,matchStatus=m.status,
                            partnerId=r.partner_anonymous_id,partnerUserId=partner,
                            expiresAt=m.expires_at,destiny=m.status=="destiny",
                            ended=false,decisionVisible=false,decisionSent=false,waitingForOther=false
                        )
                        load(m.id);startMessagePolling(m.id);startStatusPolling(m.id)
                    }else{
                        _state.value=_state.value.copy(error="Bu sohbet artık aktif değil.")
                    }
                }
                .onFailure{e->_state.value=_state.value.copy(error=e.message)}
        }
    }

    fun find(mood:String?=_state.value.selectedMood){
        waitingJob?.cancel();messageJob?.cancel();statusJob?.cancel()
        viewModelScope.launch{
            _state.value=_state.value.copy(
                searching=true,ended=false,decisionVisible=false,destiny=false,
                error=null,matchId=null,waitingMatchId=null,matchStatus=null,partnerId=null,partnerUserId=null,
                expiresAt=null,messages=emptyList(),currentTab="home",selectedMood=mood,
                decisionSent=false,waitingForOther=false
            )
            runCatching{repo.match(mood)}
                .onSuccess{response->
                    val m=response.match
                    val active=m?.status=="active"||m?.status=="destiny"
                    val me=Supabase.client.auth.currentSessionOrNull()?.user?.id
                    val partner=if(m!=null&&m.user_a==me)m.user_b else m?.user_a
                    _state.value=_state.value.copy(
                        searching=!active,
                        matchId=if(active)m?.id else null,
                        waitingMatchId=if(active)null else m?.id,
                        matchStatus=m?.status,
                        partnerId=response.partner_anonymous_id,
                        partnerUserId=partner,
                        expiresAt=m?.expires_at,
                        destiny=m?.status=="destiny"
                    )
                    if(active&&m!=null){load(m.id);startMessagePolling(m.id);startStatusPolling(m.id)}
                    else if(m?.id!=null)watchWaiting(m.id)
                }
                .onFailure{e->_state.value=_state.value.copy(searching=false,error=e.message?:"Eşleşme başlatılamadı")}
        }
    }

    private fun watchWaiting(id:String){
        waitingJob?.cancel()
        waitingJob=viewModelScope.launch{
            repeat(150){
                delay(2000)
                if(!_state.value.searching)return@launch
                runCatching{repo.status(id)}
                    .onSuccess{response->
                        val m=response.match?:return@onSuccess
                        val me=Supabase.client.auth.currentSessionOrNull()?.user?.id
                        val partner=if(m.user_a==me)m.user_b else m.user_a
                        when(m.status){
                            "active","destiny"->{
                                _state.value=_state.value.copy(
                                    searching=false,matchId=m.id,waitingMatchId=null,matchStatus=m.status,
                                    partnerId=response.partner_anonymous_id,partnerUserId=partner,
                                    expiresAt=m.expires_at,destiny=m.status=="destiny",decisionSent=false,waitingForOther=false
                                )
                                load(m.id);startMessagePolling(m.id);startStatusPolling(m.id);return@launch
                            }
                            "expired","ended"->{
                                _state.value=_state.value.copy(searching=false,ended=true,matchId=null,waitingMatchId=null,matchStatus=m.status)
                                return@launch
                            }
                        }
                    }
                    .onFailure{e->_state.value=_state.value.copy(error=e.message)}
            }
            _state.value=_state.value.copy(searching=false,waitingMatchId=null)
        }
    }

    private fun startStatusPolling(id:String){
        statusJob?.cancel()
        statusJob=viewModelScope.launch{
            while(_state.value.matchId==id&&!_state.value.ended){
                delay(3500)
                runCatching{repo.status(id)}.onSuccess{r->
                    val m=r.match?:return@onSuccess
                    when(m.status){
                        "destiny"->_state.value=_state.value.copy(matchStatus="destiny",destiny=true,expiresAt=null,decisionVisible=false,waitingForOther=false)
                        "expired","ended"->_state.value=_state.value.copy(ended=true,matchId=null,matchStatus=m.status,decisionVisible=false)
                        "active"->_state.value=_state.value.copy(matchStatus="active",partnerId=r.partner_anonymous_id)
                    }
                }
            }
        }
    }

    private fun startMessagePolling(id:String){
        messageJob?.cancel()
        messageJob=viewModelScope.launch{
            while(_state.value.matchId==id&&!_state.value.ended){
                loadNow(id)
                delay(3500)
            }
        }
    }

    private suspend fun loadNow(id:String){
        runCatching{repo.getMessages(id)}
            .onSuccess{messages->_state.value=_state.value.copy(messages=messages,error=null)}
            .onFailure{e->_state.value=_state.value.copy(error=e.message)}
    }

    fun load(id:String){viewModelScope.launch{loadNow(id)}}

    fun send(text:String){
        val id=_state.value.matchId?:return
        val clean=text.trim()
        if(clean.isEmpty()||clean.length>2000)return
        viewModelScope.launch{
            runCatching{repo.sendMessage(id,clean)}
                .onSuccess{loadNow(id)}
                .onFailure{e->_state.value=_state.value.copy(error=e.message)}
        }
    }

    fun decide(keep:Boolean){
        val id=_state.value.matchId?:return
        viewModelScope.launch{
            runCatching{repo.destiny(id,keep)}
                .onSuccess{r->
                    when(r.status){
                        "active"->_state.value=_state.value.copy(decisionVisible=false,decisionSent=true,waitingForOther=true,error=null)
                        "destiny"->_state.value=_state.value.copy(matchStatus="destiny",destiny=true,decisionVisible=false,expiresAt=null,error=null,waitingForOther=false)
                        "ended","expired"->{messageJob?.cancel();statusJob?.cancel();_state.value=_state.value.copy(ended=true,decisionVisible=false,matchId=null,matchStatus=r.status,decisionSent=false,waitingForOther=false)}
                    }
                }
                .onFailure{e->_state.value=_state.value.copy(error=e.message)}
        }
    }

    fun showDecision(){if(!_state.value.destiny&&!_state.value.decisionVisible&&!_state.value.decisionSent)_state.value=_state.value.copy(decisionVisible=true)}

    fun blockCurrent(){
        val id=_state.value.partnerUserId?:return
        viewModelScope.launch{
            runCatching{repo.block(id)}
                .onSuccess{leave()}
                .onFailure{e->_state.value=_state.value.copy(error=e.message)}
        }
    }

    fun reportCurrent(reason:String){
        val id=_state.value.partnerUserId?:return
        val match=_state.value.matchId
        viewModelScope.launch{
            runCatching{repo.report(id,match,reason)}
                .onFailure{e->_state.value=_state.value.copy(error=e.message)}
        }
    }

    fun leave(){
        waitingJob?.cancel();messageJob?.cancel();statusJob?.cancel()
        val activeId=_state.value.matchId
        val waitingId=_state.value.waitingMatchId
        viewModelScope.launch{
            if(activeId!=null)runCatching{repo.end(activeId)}
            else if(waitingId!=null)runCatching{repo.end(waitingId)}
            _state.value=_state.value.copy(
                ended=true,searching=false,matchId=null,waitingMatchId=null,matchStatus="ended",
                decisionVisible=false,decisionSent=false,waitingForOther=false,currentTab="home"
            )
        }
    }

    override fun onCleared(){
        waitingJob?.cancel();messageJob?.cancel();statusJob?.cancel()
        super.onCleared()
    }
}
