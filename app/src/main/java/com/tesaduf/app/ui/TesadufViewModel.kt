package com.tesaduf.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tesaduf.app.data.Supabase
import com.tesaduf.app.data.TesadufRepository
import com.tesaduf.app.model.Message
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class TesadufState(
    val anonymousId: String? = null,
    val matchId: String? = null,
    val matchStatus: String? = null,
    val partnerId: String? = null,
    val expiresAt: String? = null,
    val messages: List<Message> = emptyList(),
    val loading: Boolean = true,
    val searching: Boolean = false,
    val error: String? = null,
    val ended: Boolean = false,
    val decisionVisible: Boolean = false,
    val destiny: Boolean = false
)

class TesadufViewModel : ViewModel() {
    private val repo = TesadufRepository()
    private val _state = MutableStateFlow(TesadufState())
    val state: StateFlow<TesadufState> = _state

    private var waitingJob: Job? = null
    private var messageJob: Job? = null

    init {
        viewModelScope.launch {
            runCatching {
                if (Supabase.client.auth.currentSessionOrNull() == null) {
                    Supabase.client.auth.signInAnonymously()
                }
                repo.bootstrap()
            }.onSuccess { response ->
                _state.value = _state.value.copy(
                    anonymousId = response.profile?.anonymous_id,
                    loading = false,
                    error = response.error
                )
            }.onFailure { error ->
                _state.value = _state.value.copy(
                    loading = false,
                    error = error.message ?: "Bağlantı hatası"
                )
            }
        }
    }

    fun find() {
        waitingJob?.cancel()
        messageJob?.cancel()
        viewModelScope.launch {
            _state.value = _state.value.copy(
                searching = true,
                ended = false,
                decisionVisible = false,
                destiny = false,
                error = null,
                matchId = null,
                matchStatus = null,
                partnerId = null,
                expiresAt = null,
                messages = emptyList()
            )

            runCatching { repo.match() }
                .onSuccess { response ->
                    val match = response.match
                    val active = match?.status == "active" || match?.status == "destiny"

                    _state.value = _state.value.copy(
                        searching = !active,
                        matchId = if (active) match?.id else null,
                        matchStatus = match?.status,
                        partnerId = response.partner_anonymous_id,
                        expiresAt = match?.expires_at,
                        destiny = match?.status == "destiny"
                    )

                    if (active && match != null) {
                        load(match.id)
                        startMessagePolling(match.id)
                    } else if (match?.id != null) {
                        watchWaiting(match.id)
                    }
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        searching = false,
                        error = error.message ?: "Eşleşme başlatılamadı"
                    )
                }
        }
    }

    private fun watchWaiting(id: String) {
        waitingJob?.cancel()
        waitingJob = viewModelScope.launch {
            repeat(150) {
                delay(2000)
                if (!_state.value.searching) return@launch

                runCatching { repo.status(id) }
                    .onSuccess { response ->
                        val match = response.match ?: return@onSuccess

                        when (match.status) {
                            "active", "destiny" -> {
                                _state.value = _state.value.copy(
                                    searching = false,
                                    matchId = match.id,
                                    matchStatus = match.status,
                                    partnerId = response.partner_anonymous_id,
                                    expiresAt = match.expires_at,
                                    destiny = match.status == "destiny"
                                )
                                load(match.id)
                                startMessagePolling(match.id)
                                return@launch
                            }
                            "expired", "ended" -> {
                                _state.value = _state.value.copy(
                                    searching = false,
                                    ended = true,
                                    matchId = null,
                                    matchStatus = match.status
                                )
                                return@launch
                            }
                        }
                    }
                    .onFailure { error ->
                        _state.value = _state.value.copy(error = error.message)
                    }
            }

            _state.value = _state.value.copy(searching = false)
        }
    }

    private fun startMessagePolling(id: String) {
        messageJob?.cancel()
        messageJob = viewModelScope.launch {
            while (_state.value.matchId == id && !_state.value.ended) {
                loadNow(id)
                delay(3500)
            }
        }
    }

    private suspend fun loadNow(id: String) {
        runCatching { repo.getMessages(id) }
            .onSuccess { messages ->
                _state.value = _state.value.copy(messages = messages, error = null)
            }
            .onFailure { error ->
                _state.value = _state.value.copy(error = error.message)
            }
    }

    fun load(id: String) {
        viewModelScope.launch { loadNow(id) }
    }

    fun send(text: String) {
        val id = _state.value.matchId ?: return
        val clean = text.trim()
        if (clean.isEmpty() || clean.length > 2000) return

        viewModelScope.launch {
            runCatching { repo.sendMessage(id, clean) }
                .onSuccess { loadNow(id) }
                .onFailure { error -> _state.value = _state.value.copy(error = error.message) }
        }
    }

    fun decide(keep: Boolean) {
        val id = _state.value.matchId ?: return
        viewModelScope.launch {
            runCatching { repo.destiny(id, keep) }
                .onSuccess { response ->
                    when (response.status) {
                        "destiny" -> _state.value = _state.value.copy(
                            matchStatus = "destiny",
                            destiny = true,
                            decisionVisible = false,
                            expiresAt = null,
                            error = null
                        )
                        "ended", "expired" -> {
                            messageJob?.cancel()
                            _state.value = _state.value.copy(
                                ended = true,
                                decisionVisible = false,
                                matchId = null,
                                matchStatus = response.status
                            )
                        }
                    }
                }
                .onFailure { error -> _state.value = _state.value.copy(error = error.message) }
        }
    }

    fun showDecision() {
        if (!_state.value.destiny && !_state.value.decisionVisible) {
            _state.value = _state.value.copy(decisionVisible = true)
        }
    }

    fun leave() {
        waitingJob?.cancel()
        messageJob?.cancel()
        val id = _state.value.matchId
        viewModelScope.launch {
            if (id != null) runCatching { repo.end(id) }
            _state.value = _state.value.copy(
                ended = true,
                searching = false,
                matchId = null,
                matchStatus = "ended",
                decisionVisible = false
            )
        }
    }
}
