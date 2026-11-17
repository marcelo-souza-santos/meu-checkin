package br.com.meucheckin

import br.com.meucheckin.automation.CheckinElegibilidade
import br.com.meucheckin.automation.CheckinManager
import br.com.meucheckin.domain.model.TrainingAlarm
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test

class CheckinManagerLogicTest {

    private fun mockSlot(
        scheduleId: Int = 2,
        dataTreino: String = "2026-11-09",
        diaSemana: Int = 1, // Segunda
        horarioInicio: String = "09:00",
        tipoTreino: String = "Gi",
        canCheckin: Boolean = true,
        checkin: String? = null,
        cancelled: Boolean = false,
        status: String = "disponivel"
    ): JSONObject {
        return JSONObject().apply {
            put("schedule_id", scheduleId)
            put("data_treino", dataTreino)
            put("dia_semana", diaSemana)
            put("horario_inicio", horarioInicio)
            put("tipo_treino", tipoTreino)
            put("can_checkin", canCheckin)
            if (checkin != null) put("checkin", JSONObject("""{"id":"chk-1"}""")) else put("checkin", JSONObject.NULL)
            put("cancelled", cancelled)
            put("status", status)
        }
    }

    @Test
    fun slotCancelled_returnsCancelledResult() {
        val slot = mockSlot(cancelled = true)
        val alarmes = listOf(TrainingAlarm(horarioTreino = "09:00", diasSemana = setOf(1)))

        val result = CheckinManager.avaliarElegibilidade(slot, alarmes)
        assertTrue(result is CheckinElegibilidade.Cancelado)
    }

    @Test
    fun slotAlreadyCheckedIn_returnsJaRealizado() {
        val slot = mockSlot(checkin = "chk-1")
        val alarmes = listOf(TrainingAlarm(horarioTreino = "09:00", diasSemana = setOf(1)))

        val result = CheckinManager.avaliarElegibilidade(slot, alarmes)
        assertTrue(result is CheckinElegibilidade.JaRealizado)
    }

    @Test
    fun slotStatusRealizado_returnsJaRealizado() {
        val slot = mockSlot(status = "realizado")
        val alarmes = listOf(TrainingAlarm(horarioTreino = "09:00", diasSemana = setOf(1)))

        val result = CheckinManager.avaliarElegibilidade(slot, alarmes)
        assertTrue(result is CheckinElegibilidade.JaRealizado)
    }

    @Test
    fun noMatchingAlarmForDay_returnsSemAlarme() {
        val slot = mockSlot(diaSemana = 2, horarioInicio = "09:00") // Terça
        val alarmes = listOf(TrainingAlarm(horarioTreino = "09:00", diasSemana = setOf(1, 3, 5))) // Seg, Qua, Sex

        val result = CheckinManager.avaliarElegibilidade(slot, alarmes)
        assertTrue(result is CheckinElegibilidade.SemAlarme)
    }

    @Test
    fun noMatchingAlarmForTime_returnsSemAlarme() {
        val slot = mockSlot(diaSemana = 1, horarioInicio = "20:00") // 20:00
        val alarmes = listOf(TrainingAlarm(horarioTreino = "09:00", diasSemana = setOf(1))) // 09:00

        val result = CheckinManager.avaliarElegibilidade(slot, alarmes)
        assertTrue(result is CheckinElegibilidade.SemAlarme)
    }

    @Test
    fun canCheckinFalse_returnsJanelaFechada() {
        val slot = mockSlot(canCheckin = false)
        val alarmes = listOf(TrainingAlarm(horarioTreino = "09:00", diasSemana = setOf(1)))

        val result = CheckinManager.avaliarElegibilidade(slot, alarmes, forcarSeHouverTreino = true)
        assertTrue(result is CheckinElegibilidade.JanelaFechada)
    }

    @Test
    fun eligibleAndForced_returnsProntoParaCheckin() {
        val slot = mockSlot(canCheckin = true)
        val alarmes = listOf(TrainingAlarm(horarioTreino = "09:00", diasSemana = setOf(1)))

        val result = CheckinManager.avaliarElegibilidade(slot, alarmes, forcarSeHouverTreino = true)
        assertTrue(result is CheckinElegibilidade.ProntoParaCheckin)
        val pronto = result as CheckinElegibilidade.ProntoParaCheckin
        assertEquals(2, pronto.scheduleId)
        assertEquals("2026-11-09", pronto.dataTreino)
    }

    @Test
    fun extrairTodosSlots_combinesMainAndUpcomingWithoutDuplicates() {
        val painel = JSONObject("""
        {
            "main": {
                "slot": { "schedule_id": 26, "data_treino": "2026-11-10", "horario_inicio": "06:00" }
            },
            "upcoming": [
                { "schedule_id": 3, "data_treino": "2026-11-09", "horario_inicio": "20:00" },
                { "schedule_id": 26, "data_treino": "2026-11-10", "horario_inicio": "06:00" },
                { "schedule_id": 4, "data_treino": "2026-11-10", "horario_inicio": "17:00" }
            ]
        }
        """)

        val slots = CheckinManager.extrairTodosSlots(painel)
        assertEquals(3, slots.size)
        assertTrue(slots.any { it.optInt("schedule_id") == 3 })
        assertTrue(slots.any { it.optInt("schedule_id") == 26 })
        assertTrue(slots.any { it.optInt("schedule_id") == 4 })
    }

    @Test
    fun verificarConfirmacao_recognizesSuccessInHistoryAndUpcoming() {
        val responseJson = """
        {
            "history": [
                { "schedule_id": 3, "data_treino": "2026-11-09", "status": "confirmado" }
            ],
            "upcoming": [
                { "schedule_id": 3, "status": "realizado", "checkin": { "id": "123" } }
            ]
        }
        """

        assertTrue(CheckinManager.verificarConfirmacao(responseJson, 3))
        assertFalse(CheckinManager.verificarConfirmacao(responseJson, 99))
    }
}
