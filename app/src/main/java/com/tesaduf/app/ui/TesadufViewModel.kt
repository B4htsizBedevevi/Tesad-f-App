package com.tesaduf.app.ui
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tesaduf.app.data.Supabase
import com.tesaduf.app.data.TesadufRepository
import com.tesaduf.app.model.Message
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
data class TesadufState(
 val anonymousId:String?=null,val matchId:String?=null,val matchStatus:String?=null,val partnerId:String?=null,
 val expiresAt:String?=null,val messages:List<Message> = emptyList(),val loading:Boolean=true,val searching:Boolean=false,
 val error:String?=null,val ended:Boolean=false,val decisionVisible:Boolean=false,val destiny:Boolean=false
)
class TesadufViewModel:ViewModel(){
 private val repo=TesadufRepository();private val _state=MutableStateFlow(TesadufState());val state:StateFlow<TesadufState> = _state
 init{viewModelScope.launch{runCatching{if(Supabase.client.auth.currentSessionOrNull()==null)Supabase.client.auth.signInAnonymously();val p=repo.bootstrap();_state.value=_state.value.copy(anonymousId=p.profile?.anonymous_id,loading=false)}.onFailure{_state.value=_state.value.copy(loading=false,error=it.message?:"Bağlantı hatası")}}}
 fun find(){viewModelScope.launch{_state.value=_state.value.copy(searching=true,error=null,ended=false,matchId=null,matchStatus=null,partnerId=null);runCatching{repo.match()}.onSuccess{r->
   val m=r.match
   _state.value=_state.value.copy(searching=r.matched!=true&&m?.status=="waiting",matchId=if(m?.status in listOf("active","destiny"))m.id else null,matchStatus=m?.status,partnerId=r.partner_anonymous_id,expiresAt=m?.expires_at)
   if(m?.status=="active"||m?.status=="destiny"){load(m.id);observe(m.id)}
   else if(m?.id!=null)watchWaiting(m.id)
 }.onFailure{_state.value=_state.value.copy(searching=false,error=it.message)}}}
 private fun watchWaiting(id:String){viewModelScope.launch{repeat(150){delay(2000);if(!_state.value.searching)return@launch;runCatching{repo.status(id)}.onSuccess{r->val m=r.match?:return@onSuccess;if(m.status=="active"||m.status=="destiny"){_state.value=_state.value.copy(searching=false,matchId=m.id,matchStatus=m.status,partnerId=r.partner_anonymous_id,expiresAt=m.expires_at);load(m.id);observe(m.id);return@launch}if(m.status=="expired"||m.status=="ended"){_state.value=_state.value.copy(searching=false,ended=true,matchId=null,matchStatus=m.status);return@launch}}}}}
 fun observe(id:String){/* V1 uses resilient state polling; Realtime publication is enabled on the backend and can replace this with push-only updates after device build verification. */}
 fun load(id:String){viewModelScope.launch{runCatching{repo.getMessages(id)}.onSuccess{_state.value=_state.value.copy(messages=it)}.onFailure{_state.value=_state.value.copy(error=it.message)}}}
 fun send(t:String){val id=_state.value.matchId?:return;if(t.isBlank())return;viewModelScope.launch{runCatching{repo.sendMessage(id,t)}.onSuccess{load(id)}.onFailure{_state.value=_state.value.copy(error=it.message)}}}
 fun decide(keep:Boolean){val id=_state.value.matchId?:return;viewModelScope.launch{runCatching{repo.destiny(id,keep)}.onSuccess{r->when(r.status){"destiny"->_state.value=_state.value.copy(matchStatus="destiny",destiny=true,decisionVisible=false,expiresAt=null);"ended","expired"->_state.value=_state.value.copy(ended=true,decisionVisible=false)}}.onFailure{_state.value=_state.value.copy(error=it.message)}}}
 fun showDecision(){_state.value=_state.value.copy(decisionVisible=true)}
 fun leave(){val id=_state.value.matchId?:return;viewModelScope.launch{runCatching{repo.end(id)};_state.value=_state.value.copy(ended=true,matchId=null,matchStatus="ended")}}
}
