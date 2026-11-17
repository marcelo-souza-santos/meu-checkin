package br.com.meucheckin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.meucheckin.ui.theme.*

@Composable
fun LoginScreen(error: String?, onLogin: (String, String) -> Unit) {
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }

    Box(
        Modifier
            .fillMaxSize()
            .background(ClockDarkBackground)
            .padding(28.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Alarmes",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = ClockTextLight
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Check-in automático Jiu-Jitsu",
                style = MaterialTheme.typography.bodyMedium,
                color = ClockTextDim
            )

            Spacer(Modifier.height(32.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("E-mail") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ClockPillBlue,
                    unfocusedBorderColor = ClockTextDim.copy(alpha = 0.4f),
                    focusedLabelColor = ClockPillBlue,
                    unfocusedLabelColor = ClockTextDim
                )
            )
            Spacer(Modifier.height(14.dp))
            OutlinedTextField(
                value = pass,
                onValueChange = { pass = it },
                label = { Text("Senha") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ClockPillBlue,
                    unfocusedBorderColor = ClockTextDim.copy(alpha = 0.4f),
                    focusedLabelColor = ClockPillBlue,
                    unfocusedLabelColor = ClockTextDim
                )
            )

            if (error != null) {
                Spacer(Modifier.height(10.dp))
                Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(26.dp))

            Button(
                onClick = { onLogin(email, pass) },
                enabled = email.isNotBlank() && pass.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ClockPillBlue, contentColor = ClockPillOnBlue)
            ) {
                Text("Entrar", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(18.dp))
            Text(
                "Credenciais salvas de forma segura e criptografada no celular.",
                style = MaterialTheme.typography.bodySmall,
                color = ClockTextDim
            )
        }
    }
}

