package com.tesaduf.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.Instant

@Composable
fun TesadufApp(vm: TesadufViewModel) {
    val s by vm.state.collectAsState()
    when {
        s.loading -> Center("TESADÜF")
        s.error != null && s.anonymousId == null -> ErrorCenter(s.error!!)
        s.matchStatus == "waiting" || s.searching -> Searching(vm)
        s.matchId != null && !s.ended -> Chat(s, vm)
        else -> Home(s, vm)
    }
}

@Composable
fun Home(s: TesadufState, vm: TesadufViewModel) {
    val slogans = listOf(
        "Bazen en iyi sohbetler planlanmaz.",
        "İyi sohbetler tesadüfen başlar.",
        "Karşına kim çıkacak?",
        "Sadece yaz. Gerisini tesadüfe bırak.",
        "Bugün kimin hikâyesine denk geleceksin?"
    )
    var slogan by remember { mutableStateOf(slogans.random()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(6500)
            slogan = slogans.random()
        }
    }
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(72.dp))
        Text("✦", style = MaterialTheme.typography.displayLarge, color = MaterialTheme.colorScheme.primary)
        Text("TESADÜF", style = MaterialTheme.typography.displaySmall)
        Spacer(Modifier.height(14.dp))
        Text(slogan, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(36.dp))
        Text("Sen", style = MaterialTheme.typography.labelLarge)
        Text(s.anonymousId ?: "#-----", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(36.dp))
        Button(
            onClick = { vm.find() },
            enabled = !s.searching,
            modifier = Modifier.fillMaxWidth().height(58.dp)
        ) {
            Text(if (s.searching) "Bir tesadüf aranıyor…" else "🎲 TESADÜFÜ BAŞLAT")
        }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(
            onClick = {},
            enabled = false,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("🎙️ Sesli Sohbet • Yakında")
        }
        s.error?.let {
            Spacer(Modifier.height(20.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
fun Searching(vm: TesadufViewModel) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(22.dp))
        Text("Bir tesadüf aranıyor…", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        Text("Bakalım bugün kime denk geleceksin…")
        Spacer(Modifier.height(28.dp))
        OutlinedButton(onClick = { vm.leave() }) {
            Text("Aramayı iptal et")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Chat(s: TesadufState, vm: TesadufViewModel) {
    var text by remember { mutableStateOf("") }
    val expires = s.expiresAt
    var remaining by remember(expires, s.destiny) {
        mutableLongStateOf(
            expires?.let {
                runCatching { Duration.between(Instant.now(), Instant.parse(it)).seconds }.getOrDefault(0L)
            } ?: Long.MAX_VALUE
        )
    }

    LaunchedEffect(s.matchId, expires, s.destiny) {
        while (true) {
            if (s.destiny) {
                delay(1000)
                remaining = Long.MAX_VALUE
            } else {
                remaining = expires?.let {
                    runCatching { Duration.between(Instant.now(), Instant.parse(it)).seconds }.getOrDefault(0L)
                } ?: 0L
                if (remaining <= 0) {
                    vm.showDecision()
                    break
                }
                delay(1000)
            }
        }
    }

    if (s.decisionVisible) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Bu tesadüf hoşuna gitti mi? ✨") },
            text = { Text("İkiniz de devam etmeyi seçerse sohbetiniz kadere dönüşür.") },
            confirmButton = {
                Button(onClick = { vm.decide(true) }) { Text("💚 Tesadüfü Sürdür") }
            },
            dismissButton = {
                TextButton(onClick = { vm.decide(false) }) { Text("Burada Bitsin") }
            }
        )
    }

    Scaffold(
        topBar = TopAppBar(
            title = { Text(s.partnerId?.let { "✦ $it" } ?: "Tesadüf") },
            actions = {
                TextButton(onClick = { vm.leave() }) { Text("Bitir") }
            }
        ),
        bottomBar = {
            Row(
                Modifier.fillMaxWidth().padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { if (it.length <= 2000) text = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Bir şey söyle…") },
                    maxLines = 4
                )
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = {
                        val t = text
                        text = ""
                        vm.send(t)
                    },
                    enabled = text.isNotBlank()
                ) { Text("Gönder") }
            }
        }
    ) { p ->
        Column(Modifier.fillMaxSize().padding(p)) {
            if (!s.destiny) {
                val mins = remaining.coerceAtLeast(0) / 60
                val secs = remaining.coerceAtLeast(0) % 60
                Text(
                    "⏱️ Tesadüfün kalan süresi %02d:%02d".format(mins, secs),
                    Modifier.padding(12.dp),
                    style = MaterialTheme.typography.labelLarge
                )
            } else {
                Text(
                    "✨ KADER — artık süreniz yok",
                    Modifier.padding(12.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            LazyColumn(
                Modifier.weight(1f).padding(horizontal = 10.dp),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                items(s.messages, key = { it.id }) { m ->
                    Card(Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
                        Text(m.body, Modifier.padding(14.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun Center(t: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(t, style = MaterialTheme.typography.headlineMedium)
    }
}

@Composable
fun ErrorCenter(t: String) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Bir şeyler ters gitti", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))
        Text(t)
    }
}
