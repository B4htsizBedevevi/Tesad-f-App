package com.tesaduf.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun TesadufApp(vm: TesadufViewModel) {
    val s by vm.state.collectAsState()
    when {
        s.loading -> CenterText("TESADÜF")
        s.error != null && s.anonymousId == null -> ErrorScreen(s.error!!)
        s.matchId != null && !s.ended -> ChatScreen(s, vm)
        else -> HomeScreen(s, vm)
    }
}

@Composable fun HomeScreen(s: TesadufState, vm: TesadufViewModel) {
    val slogans=listOf(
        "Bazen en iyi sohbetler planlanmaz.",
        "İyi sohbetler tesadüfen başlar.",
        "Karşına kim çıkacak?",
        "Sadece yaz. Gerisini tesadüfe bırak.",
        "Bugün kimin hikâyesine denk geleceksin?"
    )
    var slogan by remember { mutableStateOf(slogans.random()) }
    LaunchedEffect(Unit) {
        while(true){ delay(6500); slogan=slogans.random() }
    }
    Scaffold { pad ->
        Column(
            Modifier.fillMaxSize().padding(pad).padding(24.dp).verticalScroll(rememberScrollState()),
            horizontalAlignment=Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(50.dp))
            Text("✦", style=MaterialTheme.typography.displayLarge, color=MaterialTheme.colorScheme.primary)
            Text("TESADÜF", style=MaterialTheme.typography.displayMedium)
            Spacer(Modifier.height(16.dp))
            Text(slogan, style=MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(56.dp))
            Text("Sen", style=MaterialTheme.typography.labelLarge)
            Text(s.anonymousId ?: "#-----", style=MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(32.dp))
            Button(
                onClick=vm::findText,
                enabled=!s.searching,
                modifier=Modifier.fillMaxWidth().height(58.dp)
            ) { Text(if(s.searching) "Bir tesadüf aranıyor..." else "🎲 TESADÜFÜ BAŞLAT") }
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick={},enabled=false,modifier=Modifier.fillMaxWidth().height(54.dp)){
                Text("🎙️ Sesli Sohbet  •  Yakında")
            }
            if(s.searching) {
                Spacer(Modifier.height(20.dp))
                CircularProgressIndicator()
                Spacer(Modifier.height(8.dp))
                Text("Bakalım bugün kime denk geleceksin…")
            }
        }
    }
}

@Composable fun ChatScreen(s: TesadufState, vm: TesadufViewModel) {
    var text by remember { mutableStateOf("") }
    val expiry=s.matchId?.let { s.messages.size } ?: 0
    var showDecision by remember { mutableStateOf(false) }
    LaunchedEffect(s.matchId) {
        while(!showDecision){ delay(5000); s.matchId?.let(vm::loadMessages) }
    }
    Scaffold(
        topBar={TopAppBar(title={Text(s.partnerId?.let{"✦ $it"}?:"Tesadüf")},actions={TextButton(vm::leave){Text("Bitir")}})},
        bottomBar={
            Row(Modifier.fillMaxWidth().padding(12.dp),verticalAlignment=Alignment.CenterVertically){
                OutlinedTextField(value=text,onValueChange={if(it.length<=2000)text=it},modifier=Modifier.weight(1f),placeholder={Text("Bir şey söyle…")},maxLines=4)
                Spacer(Modifier.width(8.dp))
                Button(onClick={val t=text; text=""; vm.send(t)}){Text("Gönder")}
            }
        }
    ){ pad ->
        Column(Modifier.fillMaxSize().padding(pad).padding(horizontal=12.dp)){
            Text("⏱️ Tesadüfünüz devam ediyor",style=MaterialTheme.typography.labelLarge,modifier=Modifier.padding(8.dp))
            LazyColumn(Modifier.weight(1f),reverseLayout=false,contentPadding=PaddingValues(vertical=8.dp)){
                items(s.messages){m->
                    Card(Modifier.fillMaxWidth().padding(vertical=4.dp)){
                        Text(m.body,Modifier.padding(14.dp))
                    }
                }
            }
            if(s.messages.isNotEmpty() && s.messages.size % 7 == 0 && !showDecision){
                LaunchedEffect(s.messages.size){ /* subtle reminder */ }
            }
        }
    }
}

@Composable fun CenterText(t:String){
    Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center){Text(t,style=MaterialTheme.typography.headlineLarge)}
}
@Composable fun ErrorScreen(message:String){
    Box(Modifier.fillMaxSize().padding(24.dp),contentAlignment=Alignment.Center){Text(message)}
}
