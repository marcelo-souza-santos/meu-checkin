package br.com.meucheckin.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.meucheckin.domain.model.TrainingAlarm
import br.com.meucheckin.domain.util.AlarmFormatters
import br.com.meucheckin.ui.theme.*

@Composable
fun NativeClockCard(
    alarm: TrainingAlarm,
    isExpanded: Boolean,
    onCardClick: () -> Unit,
    onOpenTreinoSelect: () -> Unit,
    onToggle: (Boolean) -> Unit,
    onSave: (TrainingAlarm) -> Unit,
    onDelete: () -> Unit
) {
    var horarioState by remember(alarm) { mutableStateOf(alarm.horarioTreino) }
    var tituloState by remember(alarm) { mutableStateOf(alarm.titulo) }
    var offsetState by remember(alarm) { mutableStateOf(alarm.offsetMinutos) }
    var randomJitterState by remember(alarm) { mutableStateOf(alarm.variacaoRandomica) }
    val diasState = remember(alarm) {
        mutableStateMapOf<Int, Boolean>().apply {
            (1..7).forEach { put(it, alarm.diasSemana.contains(it)) }
        }
    }

    val cardColor = if (alarm.enabled) ClockCardBlue else ClockCardOff

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onCardClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Linha do cabeçalho do card: Descrição dos dias / Switch
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${AlarmFormatters.formatDiasCompacto(alarm.diasSemana)} • ${alarm.titulo}",
                    fontSize = 14.sp,
                    color = ClockTextDim,
                    fontWeight = FontWeight.Medium
                )
                Switch(
                    checked = alarm.enabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = ClockPillBlue,
                        checkedTrackColor = Color(0xFF1D556F),
                        uncheckedThumbColor = ClockTextDim,
                        uncheckedTrackColor = Color(0xFF2C2F36)
                    )
                )
            }

            // Horário grande
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = horarioState,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (alarm.enabled) ClockTextLight else ClockTextDim.copy(alpha = 0.6f)
                )

                if (isExpanded) {
                    FilledTonalButton(
                        onClick = onOpenTreinoSelect,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = ClockChipBg,
                            contentColor = ClockTextLight
                        ),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text("Trocar Treino", fontSize = 13.sp)
                    }
                }
            }

            // Área expandida
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    // Barra seletora de dias da semana circular: D S T Q Q S S
                    val diasOrdem = listOf(
                        7 to "D",
                        1 to "S",
                        2 to "T",
                        3 to "Q",
                        4 to "Q",
                        5 to "S",
                        6 to "S"
                    )

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        diasOrdem.forEach { (diaNum, sigla) ->
                            val isSel = diasState[diaNum] == true
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(if (isSel) ClockPillBlue else ClockChipBg)
                                    .clickable { diasState[diaNum] = !isSel },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    sigla,
                                    color = if (isSel) ClockPillOnBlue else ClockTextLight,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(18.dp))

                    // Linha com Próximo Alarme informativo
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Próximo check-in", fontSize = 12.sp, color = ClockTextDim)
                            Text(AlarmFormatters.calcularProximoTexto(alarm.diasSemana), fontSize = 14.sp, color = ClockTextLight)
                        }
                        Text(
                            "+$offsetState min após início",
                            fontSize = 13.sp,
                            color = ClockPillBlue,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    // Item: Offset de minutos de disparo
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(ClockChipBg)
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Disparar após início", color = ClockTextLight, fontSize = 14.sp)
                            Text("Tempo base estimado", color = ClockTextDim, fontSize = 11.sp)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(5, 10, 15, 20).forEach { min ->
                                val sel = offsetState == min
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (sel) ClockPillBlue else Color.Transparent)
                                        .clickable { offsetState = min }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        "~$min min",
                                        fontSize = 12.sp,
                                        color = if (sel) ClockPillOnBlue else ClockTextDim,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    // Switch: Variação humana aleatória
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(ClockChipBg)
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("Simular horário humano", color = ClockTextLight, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Text(
                                "Varia alguns minutos e segundos aleatoriamente a cada dia.",
                                color = ClockTextDim,
                                fontSize = 11.sp
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Switch(
                            checked = randomJitterState,
                            onCheckedChange = { randomJitterState = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = ClockPillBlue,
                                checkedTrackColor = Color(0xFF1D556F),
                                uncheckedThumbColor = ClockTextDim,
                                uncheckedTrackColor = Color(0xFF2C2F36)
                            )
                        )
                    }

                    Spacer(Modifier.height(20.dp))

                    // Barra inferior de botões: Excluir / Salvar
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = onDelete,
                            colors = ButtonDefaults.textButtonColors(contentColor = ClockDeleteRed)
                        ) {
                            Text("Excluir", fontSize = 15.sp)
                        }

                        Button(
                            onClick = {
                                val selectedDays = diasState.filter { it.value }.keys.toSet()
                                val updated = alarm.copy(
                                    titulo = tituloState.ifBlank { "Treino" },
                                    horarioTreino = horarioState,
                                    diasSemana = if (selectedDays.isEmpty()) setOf(1, 3, 5) else selectedDays,
                                    offsetMinutos = offsetState,
                                    variacaoRandomica = randomJitterState,
                                    enabled = true
                                )
                                onSave(updated)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ClockPillBlue,
                                contentColor = ClockPillOnBlue
                            ),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text("Salvar", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

