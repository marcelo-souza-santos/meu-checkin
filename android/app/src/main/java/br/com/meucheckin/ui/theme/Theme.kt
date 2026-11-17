package br.com.meucheckin.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    background = ClockDarkBackground,
    surface = ClockCardOff,
    onBackground = ClockTextLight,
    onSurface = ClockTextLight,
    primary = ClockPillBlue
)

@Composable
fun MeuCheckinTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}

