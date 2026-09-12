package com.tesaduf.app.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors=darkColorScheme(
    primary=Color(0xFFA6FF4D),
    onPrimary=Color(0xFF182000),
    background=Color(0xFF080909),
    surface=Color(0xFF111313),
    secondary=Color(0xFFB7FF7C)
)

@Composable
fun TesadufTheme(content:@Composable()->Unit){
    MaterialTheme(colorScheme=DarkColors,content=content)
}
