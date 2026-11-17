package br.com.meucheckin.domain.util

import java.util.Calendar

object AlarmFormatters {

    fun formatDiasCompacto(dias: Set<Int>): String {
        if (dias.size == 7) return "Todos os dias"
        if (dias == setOf(1, 2, 3, 4, 5)) return "Seg.-sex."
        if (dias == setOf(6, 7)) return "Fim de semana"
        val nomes = mapOf(1 to "Seg.", 2 to "Ter.", 3 to "Qua.", 4 to "Qui.", 5 to "Sex.", 6 to "Sáb.", 7 to "Dom.")
        return dias.sorted().joinToString(" ") { nomes[it] ?: "" }
    }

    fun calcularProximoTexto(dias: Set<Int>, calendar: Calendar = Calendar.getInstance()): String {
        val hoje = calendar.get(Calendar.DAY_OF_WEEK)
        // 1=Dom, 2=Seg ... 7=Sab -> converte para 1=Seg...7=Dom
        val hojeIso = when (hoje) {
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY -> 4
            Calendar.FRIDAY -> 5
            Calendar.SATURDAY -> 6
            Calendar.SUNDAY -> 7
            else -> 1
        }
        return if (dias.contains(hojeIso)) "Hoje" else "Próximo dia cadastrado"
    }
}

