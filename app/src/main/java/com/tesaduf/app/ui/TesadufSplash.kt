package com.tesaduf.app.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

private val SplashSlogans=listOf(
    "Bazen en iyi sohbetler planlanmaz.",
    "Bir yerde biri de seni bekliyor.",
    "Yollarınız kesişmek üzere…",
    "Belki de bugün birine denk gelirsin.",
    "Bugünün tesadüfü hazırlanıyor…",
    "İşte o an…"
)

@Composable
fun TesadufSplash(onFinished:()->Unit){
    var sloganIndex by remember { mutableIntStateOf(0) }
    val infinite=rememberInfiniteTransition(label="splash")
    val rotation by infinite.animateFloat(
        initialValue=0f,targetValue=360f,
        animationSpec=infiniteRepeatable(tween(5200, easing=LinearEasing)),
        label="ring"
    )
    val pulse by infinite.animateFloat(
        initialValue=0.93f,targetValue=1.05f,
        animationSpec=infiniteRepeatable(tween(1200),RepeatMode.Reverse),
        label="pulse"
    )
    val alpha by infinite.animateFloat(
        initialValue=0.35f,targetValue=0.95f,
        animationSpec=infiniteRepeatable(tween(900),RepeatMode.Reverse),
        label="glow"
    )

    LaunchedEffect(Unit){
        repeat(5){
            delay(520)
            sloganIndex=(sloganIndex+1)%SplashSlogans.size
        }
        onFinished()
    }

    Box(
        modifier=Modifier.fillMaxSize()
            .drawBehind{
                drawRect(
                    Brush.radialGradient(
                        colors=listOf(
                            Color(0xFF1A1738),
                            Color(0xFF0D1022),
                            Color(0xFF07080D)
                        ),
                        center=Offset(size.width/2,size.height*0.42f),
                        radius=size.maxDimension*0.8f
                    )
                )
            },
        contentAlignment=Alignment.Center
    ){
        Canvas(
            Modifier.fillMaxSize().rotate(rotation)
        ){
            val center=Offset(size.width/2,size.height*0.39f)
            val radius=size.minDimension*0.27f
            drawCircle(
                brush=Brush.sweepGradient(
                    0f to Color(0xFF32A6FF),
                    0.33f to Color(0xFF6B56FF),
                    0.66f to Color(0xFFFF38B8),
                    1f to Color(0xFF32A6FF)
                ),
                radius=radius,
                center=center,
                style=Stroke(width=7f)
            )
            for(i in 0 until 16){
                val a=(Math.PI*2*i/16.0)
                val rr=radius+18f
                val p=Offset(
                    center.x+(kotlin.math.cos(a)*rr).toFloat(),
                    center.y+(kotlin.math.sin(a)*rr).toFloat()
                )
                drawCircle(Color(0xFFFFFFFF).copy(alpha=alpha*0.55f),3.5f,p)
            }
        }

        Column(horizontalAlignment=Alignment.CenterHorizontally){
            Box(
                Modifier.size(190.dp).scale(pulse),
                contentAlignment=Alignment.Center
            ){
                Canvas(Modifier.fillMaxSize()){
                    val c=Offset(size.width/2,size.height/2)
                    val r=size.minDimension*0.42f
                    drawCircle(
                        brush=Brush.radialGradient(
                            colors=listOf(Color(0xFF284BFF).copy(alpha=0.22f),Color.Transparent)
                        ),
                        radius=r*1.45f,
                        center=c
                    )
                }
                Text("✦",style=MaterialTheme.typography.displayLarge,color=Color(0xFFFFC86E))
                Row(horizontalArrangement=Arrangement.spacedBy((-18).dp)){
                    Box(Modifier.size(76.dp).background(Color(0xFF2478FF),CircleShape))
                    Box(Modifier.size(76.dp).background(Color(0xFFE83BB0),CircleShape))
                }
            }

            Spacer(Modifier.height(12.dp))
            Text("Tesadüf",style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            Text(
                SplashSlogans[sloganIndex],
                style=MaterialTheme.typography.bodyLarge,
                color=Color.White.copy(alpha=0.86f)
            )

            Spacer(Modifier.height(28.dp))
            Canvas(Modifier.width(170.dp).height(4.dp)){
                drawRoundRect(
                    brush=Brush.horizontalGradient(
                        listOf(Color(0xFF2FA7FF),Color(0xFF8D45FF),Color(0xFFFF42B8))
                    ),
                    cornerRadius=androidx.compose.ui.geometry.CornerRadius(4f,4f)
                )
            }
        }
    }
}
