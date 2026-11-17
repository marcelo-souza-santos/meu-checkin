package br.com.meucheckin

import br.com.meucheckin.domain.util.AlarmFormatters
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar

class AlarmFormattersTest {

    @Test
    fun formatDiasCompacto_allDays() {
        val dias = setOf(1, 2, 3, 4, 5, 6, 7)
        assertEquals("Todos os dias", AlarmFormatters.formatDiasCompacto(dias))
    }

    @Test
    fun formatDiasCompacto_weekdays() {
        val dias = setOf(1, 2, 3, 4, 5)
        assertEquals("Seg.-sex.", AlarmFormatters.formatDiasCompacto(dias))
    }

    @Test
    fun formatDiasCompacto_weekend() {
        val dias = setOf(6, 7)
        assertEquals("Fim de semana", AlarmFormatters.formatDiasCompacto(dias))
    }

    @Test
    fun formatDiasCompacto_specificDays() {
        val dias = setOf(1, 3, 5)
        assertEquals("Seg. Qua. Sex.", AlarmFormatters.formatDiasCompacto(dias))
    }

    @Test
    fun calcularProximoTexto_todayMatches() {
        val cal = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        }
        val dias = setOf(1, 3, 5) // 1=Segunda
        assertEquals("Hoje", AlarmFormatters.calcularProximoTexto(dias, cal))
    }

    @Test
    fun calcularProximoTexto_differentDay() {
        val cal = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        }
        val dias = setOf(1, 3, 5) // domingo é 7
        assertEquals("Próximo dia cadastrado", AlarmFormatters.calcularProximoTexto(dias, cal))
    }
}

