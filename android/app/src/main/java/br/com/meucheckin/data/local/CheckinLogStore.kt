package br.com.meucheckin.data.local

import android.content.Context
import android.content.SharedPreferences
import br.com.meucheckin.domain.model.CheckinLog
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CheckinLogStore(private val prefs: SharedPreferences) {

    constructor(context: Context) : this(
        context.getSharedPreferences("checkin_logs", Context.MODE_PRIVATE)
    )

    fun getLogs(): List<CheckinLog> {
        val raw = prefs.getString("logs", null) ?: return emptyList()
        val list = mutableListOf<CheckinLog>()
        val arr = JSONArray(raw)
        for (i in 0 until arr.length()) {
            list.add(CheckinLog.fromJson(arr.getJSONObject(i)))
        }
        return list
    }

    fun addLog(scheduleId: Int, treinoDesc: String, sucesso: Boolean, mensagem: String) {
        val now = System.currentTimeMillis()
        val df = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val newLog = CheckinLog(
            timestamp = now,
            dataHoraStr = df.format(Date(now)),
            scheduleId = scheduleId,
            treinoDesc = treinoDesc,
            sucesso = sucesso,
            mensagem = mensagem
        )
        val logs = getLogs().toMutableList()
        logs.add(0, newLog)
        val trimmed = logs.take(50)
        val arr = JSONArray()
        trimmed.forEach { arr.put(it.toJson()) }
        prefs.edit().putString("logs", arr.toString()).apply()
    }
}
