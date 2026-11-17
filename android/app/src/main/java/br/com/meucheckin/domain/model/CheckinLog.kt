package br.com.meucheckin.domain.model

import org.json.JSONObject

/** Registro de histórico de tentativa de check-in. */
data class CheckinLog(
    val timestamp: Long,
    val dataHoraStr: String,
    val scheduleId: Int,
    val treinoDesc: String,
    val sucesso: Boolean,
    val mensagem: String
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("timestamp", timestamp)
        put("dataHoraStr", dataHoraStr)
        put("scheduleId", scheduleId)
        put("treinoDesc", treinoDesc)
        put("sucesso", sucesso)
        put("mensagem", mensagem)
    }

    companion object {
        fun fromJson(json: JSONObject): CheckinLog = CheckinLog(
            timestamp = json.optLong("timestamp"),
            dataHoraStr = json.optString("dataHoraStr"),
            scheduleId = json.optInt("scheduleId"),
            treinoDesc = json.optString("treinoDesc"),
            sucesso = json.optBoolean("sucesso"),
            mensagem = json.optString("mensagem")
        )
    }
}

