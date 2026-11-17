package br.com.meucheckin

import br.com.meucheckin.domain.model.TrainingAlarm
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test

class TrainingAlarmTest {

    @Test
    fun defaultValues_areValid() {
        val alarm = TrainingAlarm()
        assertEquals("Treino", alarm.titulo)
        assertEquals("09:00", alarm.horarioTreino)
        assertEquals(setOf(1, 3, 5), alarm.diasSemana)
        assertEquals(10, alarm.offsetMinutos)
        assertTrue(alarm.variacaoRandomica)
        assertTrue(alarm.enabled)
    }

    @Test
    fun toJson_and_fromJson_preservesAllProperties() {
        val original = TrainingAlarm(
            id = "alarm-123",
            titulo = "No-Gi Noturno",
            horarioTreino = "20:00",
            diasSemana = setOf(2, 4),
            offsetMinutos = 15,
            variacaoRandomica = false,
            enabled = false
        )

        val json = original.toJson()
        val restored = TrainingAlarm.fromJson(json)

        assertEquals(original.id, restored.id)
        assertEquals(original.titulo, restored.titulo)
        assertEquals(original.horarioTreino, restored.horarioTreino)
        assertEquals(original.diasSemana, restored.diasSemana)
        assertEquals(original.offsetMinutos, restored.offsetMinutos)
        assertEquals(original.variacaoRandomica, restored.variacaoRandomica)
        assertEquals(original.enabled, restored.enabled)
    }

    @Test
    fun fromJson_emptyDays_fallsBackToDefaults() {
        val json = JSONObject("""{"titulo":"Test","diasSemana":[]}""")
        val alarm = TrainingAlarm.fromJson(json)
        assertEquals(setOf(1, 3, 5), alarm.diasSemana)
    }
}

