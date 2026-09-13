package com.tesaduf.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Colors = darkColorScheme(
    primary = Color(0xFF66B8FF),
    onPrimary = Color(0xFF07111F),
    secondary = Color(0xFFFF5CC7),
    onSecondary = Color(0xFF25051B),
    tertiary = Color(0xFFFFD27D),
    background = Color(0xFF070914),
    surface = Color(0xFF101322),
    surfaceVariant = Color(0xFF171B2C)
)

@Composable
fun TesadufTheme(content: @Composable () -> Unit) =
    MaterialTheme(colorScheme = Colors, content = content)
