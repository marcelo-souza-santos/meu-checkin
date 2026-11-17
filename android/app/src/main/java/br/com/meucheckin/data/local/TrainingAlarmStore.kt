package br.com.meucheckin.data.local

import android.content.Context
import android.content.SharedPreferences
import br.com.meucheckin.domain.model.TrainingAlarm
import org.json.JSONArray

class TrainingAlarmStore(private val prefs: SharedPreferences) {

    constructor(context: Context) : this(
        context.getSharedPreferences("training_alarms", Context.MODE_PRIVATE)
    )

    fun getAlarms(): List<TrainingAlarm> {
        val raw = prefs.getString("alarm_list", null) ?: return emptyList()
        val list = mutableListOf<TrainingAlarm>()
        val arr = JSONArray(raw)
        for (i in 0 until arr.length()) {
            list.add(TrainingAlarm.fromJson(arr.getJSONObject(i)))
        }

        // Se ainda for o alarme legado "Treino Manhã" pré-cadastrado de versão anterior, limpa
        if (list.size == 1 && list[0].titulo == "Treino Manhã" && !prefs.getBoolean("user_customized_alarms", false)) {
            prefs.edit().remove("alarm_list").apply()
            return emptyList()
        }

        return list.sortedBy { it.horarioTreino }
    }

    fun saveAlarms(alarms: List<TrainingAlarm>) {
        val arr = JSONArray()
        alarms.forEach { arr.put(it.toJson()) }
        prefs.edit()
            .putString("alarm_list", arr.toString())
            .putBoolean("user_customized_alarms", true)
            .apply()
    }

    fun saveAlarm(alarm: TrainingAlarm) {
        val current = getAlarms().toMutableList()
        val idx = current.indexOfFirst { it.id == alarm.id }
        if (idx >= 0) {
            current[idx] = alarm
        } else {
            current.add(alarm)
        }
        saveAlarms(current)
    }

    fun deleteAlarm(id: String) {
        val current = getAlarms().filter { it.id != id }
        saveAlarms(current)
    }

    fun toggleAlarm(id: String, enabled: Boolean) {
        val current = getAlarms().toMutableList()
        val idx = current.indexOfFirst { it.id == id }
        if (idx >= 0) {
            current[idx] = current[idx].copy(enabled = enabled)
            saveAlarms(current)
        }
    }
}
