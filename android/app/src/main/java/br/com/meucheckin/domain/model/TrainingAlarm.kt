package br.com.meucheckin.domain.model

import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/**
 * Modelo estilo Alarme de Celular:
 * - Horário do treino (ex: "09:00", "20:00")
 * - Dias da semana em que repete (ex: Seg, Qua, Sex)
 * - Minutos após o início para efetuar o check-in (ex: 10 min)
 * - Switch de ativado/desativado
 * - Variação randômica humana (jitter)
 */
data class TrainingAlarm(
    val id: String = UUID.randomUUID().toString(),
    val titulo: String = "Treino",
    val horarioTreino: String = "09:00",
    val diasSemana: Set<Int> = setOf(1, 3, 5), // 1=Segunda, ..., 7=Domingo
    val offsetMinutos: Int = 10,
    val variacaoRandomica: Boolean = true,
    val enabled: Boolean = true
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("titulo", titulo)
        put("horarioTreino", horarioTreino)
        put("diasSemana", JSONArray(diasSemana))
        put("offsetMinutos", offsetMinutos)
        put("variacaoRandomica", variacaoRandomica)
        put("enabled", enabled)
    }

    companion object {
        fun fromJson(json: JSONObject): TrainingAlarm {
            val dias = mutableSetOf<Int>()
            val arr = json.optJSONArray("diasSemana")
            if (arr != null) {
                for (i in 0 until arr.length()) {
                    dias.add(arr.getInt(i))
                }
            }
            return TrainingAlarm(
                id = json.optString("id", UUID.randomUUID().toString()),
                titulo = json.optString("titulo", "Treino"),
                horarioTreino = json.optString("horarioTreino", "09:00"),
                diasSemana = if (dias.isEmpty()) setOf(1, 3, 5) else dias,
                offsetMinutos = json.optInt("offsetMinutos", 10),
                variacaoRandomica = json.optBoolean("variacaoRandomica", true),
                enabled = json.optBoolean("enabled", true)
            )
        }
    }
}

