package com.tesaduf.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tesaduf.app.data.Supabase
import com.tesaduf.app.data.TesadufRepository
import com.tesaduf.app.model.Message
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class TesadufState(
    val anonymousId: String? = null,
    val matchId: String? = null,
    val partnerId: String? = null,
    val messages: List<Message> = emptyList(),
    val loading: Boolean = true,
    val searching: Boolean = false,
    val error: String? = null,
    val ended: Boolean = false,
)

class TesadufViewModel: ViewModel() {
    private val repo=TesadufRepository()
    private val _state=MutableStateFlow(TesadufState())
    val state: StateFlow<TesadufState> = _state

    init {
        viewModelScope.launch {
            try {
                val session=Supabase.client.auth.currentSessionOrNull()
                if(session==null) Supabase.client.auth.signInAnonymously()
                val p=repo.bootstrap()
                _state.value=_state.value.copy(anonymousId=p.profile?.anonymous_id,loading=false)
            } catch(e: Exception) {
                _state.value=_state.value.copy(loading=false,error=e.message ?: "Bağlantı hatası")
            }
        }
    }

    fun findText() {
        viewModelScope.launch {
            _state.value=_state.value.copy(searching=true,error=null,ended=false)
            try {
                val r=repo.match()
                _state.value=_state.value.copy(searching=!r.matched!!,matchId=r.match?.id,partnerId=r.partner_anonymous_id)
                if(r.matched==true && r.match?.id!=null) loadMessages(r.match.id)
            } catch(e: Exception) {
                _state.value=_state.value.copy(searching=false,error=e.message ?: "Eşleşme alınamadı")
            }
        }
    }

    fun loadMessages(id: String) {
        viewModelScope.launch {
            try { _state.value=_state.value.copy(messages=repo.getMessages(id),searching=false) }
            catch(e: Exception) { _state.value=_state.value.copy(searching=false,error=e.message) }
        }
    }

    fun send(text:String) {
        val id=_state.value.matchId ?: return
        if(text.isBlank())return
        viewModelScope.launch {
            try {
                repo.sendMessage(id,text)
                loadMessages(id)
            } catch(e:Exception) { _state.value=_state.value.copy(error=e.message) }
        }
    }

    fun decide(keep:Boolean) {
        val id=_state.value.matchId ?: return
        viewModelScope.launch {
            try {
                val r=repo.destiny(id,keep)
                if(r.status=="ended"||r.status=="expired") _state.value=_state.value.copy(ended=true)
            } catch(e:Exception) { _state.value=_state.value.copy(error=e.message) }
        }
    }

    fun leave() {
        val id=_state.value.matchId ?: return
        viewModelScope.launch { runCatching { repo.end(id) }; _state.value=_state.value.copy(ended=true) }
    }
}
