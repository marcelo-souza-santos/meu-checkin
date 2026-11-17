package br.com.meucheckin.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.meucheckin.domain.model.TreinoOpcao
import br.com.meucheckin.ui.theme.ClockCardOff
import br.com.meucheckin.ui.theme.ClockChipBg
import br.com.meucheckin.ui.theme.ClockPillBlue
import br.com.meucheckin.ui.theme.ClockTextDim
import br.com.meucheckin.ui.theme.ClockTextLight

@Composable
fun SelectTreinoDialog(
    treinos: List<TreinoOpcao>,
    onDismiss: () -> Unit,
    onSelect: (TreinoOpcao) -> Unit
) {
    val diasNomes = mapOf(
        1 to "Segunda", 2 to "Terça", 3 to "Quarta", 4 to "Quinta",
        5 to "Sexta", 6 to "Sábado", 7 to "Domingo"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ClockCardOff,
        title = {
            Text(
                "Escolha o Treino da Academia",
                color = ClockTextLight,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 350.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(treinos) { t ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(t) },
                        colors = CardDefaults.cardColors(containerColor = ClockChipBg),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "${t.tipoTreino} • ${t.horarioInicio}",
                                    color = ClockTextLight,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    "Término às ${t.horarioFim} • ${diasNomes[t.diaSemana] ?: ""}",
                                    color = ClockTextDim,
                                    fontSize = 12.sp
                                )
                            }
                            Text(
                                "Selecionar",
                                color = ClockPillBlue,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = ClockTextDim)
            }
        }
    )
}

