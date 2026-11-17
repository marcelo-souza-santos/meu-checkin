package br.com.meucheckin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.meucheckin.ui.theme.ClockDarkBackground
import br.com.meucheckin.ui.theme.ClockPillBlue
import br.com.meucheckin.ui.theme.ClockTextDim
import br.com.meucheckin.ui.theme.ClockTextLight

@Composable
fun LoadingScreen() {
    Box(
        Modifier
            .fillMaxSize()
            .background(ClockDarkBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(
                modifier = Modifier.size(52.dp),
                color = ClockPillBlue,
                strokeWidth = 4.dp
            )
            Spacer(Modifier.height(18.dp))
            Text(
                "Carregando...",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = ClockTextLight
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Conectando à plataforma de treinos",
                style = MaterialTheme.typography.bodySmall,
                color = ClockTextDim
            )
        }
    }
}

