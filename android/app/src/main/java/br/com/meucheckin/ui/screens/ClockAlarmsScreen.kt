package br.com.meucheckin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.meucheckin.domain.model.AlunoPerfil
import br.com.meucheckin.domain.model.TrainingAlarm
import br.com.meucheckin.domain.model.TreinoOpcao
import br.com.meucheckin.ui.components.NativeClockCard
import br.com.meucheckin.ui.components.SelectTreinoDialog
import br.com.meucheckin.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClockAlarmsScreen(
    alarms: List<TrainingAlarm>,
    perfil: AlunoPerfil?,
    availableTreinos: List<TreinoOpcao>,
    isExecuting: Boolean,
    feedback: String?,
    onToggleAlarm: (String, Boolean) -> Unit,
    onSaveAlarm: (TrainingAlarm) -> Unit,
    onDeleteAlarm: (String) -> Unit,
    onTestCheckin: () -> Unit,
    onLogout: () -> Unit
) {
    var expandedAlarmId by remember { mutableStateOf<String?>(null) }
    var menuOpen by remember { mutableStateOf(false) }

    // Estado para quando estiver criando um novo alarme pelo FAB (+)
    var draftNewAlarm by remember { mutableStateOf<TrainingAlarm?>(null) }
    var selectTreinoDialogOpen by remember { mutableStateOf(false) }
    var pickingForExistingAlarmId by remember { mutableStateOf<String?>(null) }

    Scaffold(
        containerColor = ClockDarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Check-in Automático",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = ClockTextLight
                        )
                        if (perfil != null) {
                            Text(
                                "${perfil.nome} • Faixa ${perfil.faixa} (${perfil.graus}º grau)" +
                                        (if (perfil.proximaFaixa != null) " → ${perfil.proximaFaixa}" else ""),
                                fontSize = 12.sp,
                                color = ClockPillBlue,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ClockDarkBackground),
                actions = {
                    Box {
                        IconButton(onClick = { menuOpen = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Mais opções", tint = ClockTextLight)
                        }
                        DropdownMenu(
                            expanded = menuOpen,
                            onDismissRequest = { menuOpen = false },
                            modifier = Modifier.background(ClockCardOff)
                        ) {
                            DropdownMenuItem(
                                text = { Text(if (isExecuting) "Verificando..." else "Testar check-in agora", color = ClockTextLight) },
                                onClick = {
                                    menuOpen = false
                                    onTestCheckin()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Sair da conta (Logout)", color = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    menuOpen = false
                                    onLogout()
                                }
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    pickingForExistingAlarmId = null
                    selectTreinoDialogOpen = true
                },
                containerColor = ClockPillBlue,
                contentColor = ClockPillOnBlue,
                shape = CircleShape,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar Alarme", modifier = Modifier.size(28.dp))
            }
        }
    ) { padding ->
        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (feedback != null) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = ClockCardBlue),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            feedback,
                            color = ClockTextLight,
                            modifier = Modifier.padding(14.dp),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            // Se estiver criando um novo alarme rascunho
            if (draftNewAlarm != null) {
                item {
                    Text(
                        "Novo Agendamento (Rascunho):",
                        color = ClockPillBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    NativeClockCard(
                        alarm = draftNewAlarm!!,
                        isExpanded = true,
                        onCardClick = {},
                        onOpenTreinoSelect = {
                            pickingForExistingAlarmId = "DRAFT"
                            selectTreinoDialogOpen = true
                        },
                        onToggle = { draftNewAlarm = draftNewAlarm!!.copy(enabled = it) },
                        onSave = { finalizedAlarm ->
                            onSaveAlarm(finalizedAlarm)
                            draftNewAlarm = null
                            expandedAlarmId = null
                        },
                        onDelete = {
                            draftNewAlarm = null
                        }
                    )
                }
            }

            if (alarms.isEmpty() && draftNewAlarm == null) {
                item {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Nenhum alarme de check-in configurado.\nToque em + para adicionar os treinos que você frequenta.",
                            color = ClockTextDim,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }

            items(alarms, key = { it.id }) { alarm ->
                val isExpanded = expandedAlarmId == alarm.id
                NativeClockCard(
                    alarm = alarm,
                    isExpanded = isExpanded,
                    onCardClick = {
                        expandedAlarmId = if (isExpanded) null else alarm.id
                    },
                    onOpenTreinoSelect = {
                        pickingForExistingAlarmId = alarm.id
                        selectTreinoDialogOpen = true
                    },
                    onToggle = { onToggleAlarm(alarm.id, it) },
                    onSave = { updated ->
                        onSaveAlarm(updated)
                        expandedAlarmId = null
                    },
                    onDelete = {
                        onDeleteAlarm(alarm.id)
                        if (expandedAlarmId == alarm.id) expandedAlarmId = null
                    }
                )
            }

            item {
                Spacer(Modifier.height(80.dp))
            }
        }
    }

    if (selectTreinoDialogOpen) {
        SelectTreinoDialog(
            treinos = availableTreinos,
            onDismiss = { selectTreinoDialogOpen = false },
            onSelect = { opcao ->
                selectTreinoDialogOpen = false
                if (pickingForExistingAlarmId == "DRAFT") {
                    draftNewAlarm = draftNewAlarm?.copy(
                        titulo = opcao.tipoTreino,
                        horarioTreino = opcao.horarioInicio
                    )
                } else if (pickingForExistingAlarmId != null) {
                    val existing = alarms.find { it.id == pickingForExistingAlarmId }
                    if (existing != null) {
                        val updated = existing.copy(
                            titulo = opcao.tipoTreino,
                            horarioTreino = opcao.horarioInicio
                        )
                        onSaveAlarm(updated)
                    }
                } else {
                    draftNewAlarm = TrainingAlarm(
                        titulo = opcao.tipoTreino,
                        horarioTreino = opcao.horarioInicio,
                        diasSemana = setOf(opcao.diaSemana),
                        offsetMinutos = 10,
                        variacaoRandomica = true,
                        enabled = true
                    )
                }
            }
        )
    }
}

