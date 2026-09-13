package com.tesaduf.app.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tesaduf.app.R
import kotlinx.coroutines.delay

private val slogans = listOf(
    "Bazen en iyi sohbetler planlanmaz.",
    "Bir yabancı. Bir sohbet.",
    "Yollarınız kesişmek üzere…",
    "Bugün kimin hikâyesine denk geleceksin?",
    "Sadece yaz. Gerisini tesadüfe bırak."
)

@Composable
fun TesadufRoot() {
    var splash by remember { mutableStateOf(true) }
    Crossfade(targetState = splash, animationSpec = tween(400), label = "root") {
        if (it) TesadufSplash { splash = false } else TesadufHome()
    }
}

@Composable
private fun TesadufSplash(onFinished: () -> Unit) {
    var index by remember { mutableIntStateOf(0) }
    val t = rememberInfiniteTransition(label = "splash")
    val pulse by t.animateFloat(0.95f, 1.05f, infiniteRepeatable(tween(1100), RepeatMode.Reverse), label = "pulse")
    val rotation by t.animateFloat(0f, 360f, infiniteRepeatable(tween(6000)), label = "rotation")

    LaunchedEffect(Unit) {
        repeat(4) {
            delay(850)
            index = (index + 1) % slogans.size
        }
        delay(250)
        onFinished()
    }

    Box(
        Modifier.fillMaxSize().background(
            Brush.radialGradient(listOf(Color(0xFF1A1A42), Color(0xFF0C1022), Color(0xFF070914)))
        ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val c = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height * .38f)
            val r = size.minDimension * .28f
            drawCircle(
                Brush.sweepGradient(listOf(Color(0xFF31B7FF), Color(0xFF7550FF), Color(0xFFFF3FB8), Color(0xFF31B7FF))),
                r, c, style = Stroke(7f)
            )
            repeat(12) { i ->
                val a = Math.toRadians(i * 30.0 + rotation)
                val rr = r + 22f
                drawCircle(
                    Color.White.copy(alpha = .5f), 3f,
                    androidx.compose.ui.geometry.Offset(c.x + kotlin.math.cos(a).toFloat() * rr, c.y + kotlin.math.sin(a).toFloat() * rr)
                )
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(painterResource(R.drawable.tesaduf_logo), "Tesadüf", Modifier.size(190.dp).scale(pulse))
            Spacer(Modifier.height(16.dp))
            Text("TESADÜF", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            AnimatedContent(targetState = index, label = "slogan") {
                Text(slogans[it], style = MaterialTheme.typography.bodyLarge, color = Color.White.copy(alpha = .88f))
            }
        }
    }
}

@Composable
private fun TesadufHome() {
    var mood by remember { mutableStateOf("🎲 Fark etmez") }
    val moods = listOf("😄 Eğlenceli", "🧠 Derin", "🌙 Gece", "🎵 Müzik", "🎮 Oyun", "🎲 Fark etmez")

    Column(
        Modifier.fillMaxSize().background(Color(0xFF070914)).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(painterResource(R.drawable.tesaduf_logo), null, Modifier.size(68.dp))
            Spacer(Modifier.width(12.dp))
            Column {
                Text("TESADÜF", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text("Her sohbet yeni bir hikâye.", style = MaterialTheme.typography.bodyMedium)
            }
        }
        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp)) {
            Column(Modifier.padding(18.dp)) {
                Text("Anonim kimliğin", style = MaterialTheme.typography.labelLarge)
                Text("Hazırlanıyor…", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("Gerçek kimliğin görünmez.", style = MaterialTheme.typography.bodyMedium)
            }
        }
        Text("Bugün nasıl bir sohbet?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        moods.chunked(2).forEach { pair ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                pair.forEach { item ->
                    FilterChip(selected = mood == item, onClick = { mood = item }, label = { Text(item) }, modifier = Modifier.weight(1f))
                }
            }
        }
        Button(
            onClick = { },
            modifier = Modifier.fillMaxWidth().height(58.dp),
            shape = RoundedCornerShape(18.dp)
        ) { Text("🎲 TESADÜFÜ BAŞLAT", fontWeight = FontWeight.Bold) }
    }
}
