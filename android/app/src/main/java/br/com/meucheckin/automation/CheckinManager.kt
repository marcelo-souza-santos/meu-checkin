package br.com.meucheckin.automation

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import br.com.meucheckin.alarm.CheckinAlarmReceiver
import br.com.meucheckin.data.ApiClient
import br.com.meucheckin.data.local.CheckinLogStore
import br.com.meucheckin.data.local.TrainingAlarmStore
import br.com.meucheckin.domain.model.TrainingAlarm
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

sealed interface CheckinElegibilidade {
    data class Cancelado(val motivo: String) : CheckinElegibilidade
    data class JaRealizado(val mensagem: String) : CheckinElegibilidade
    data class SemAlarme(val motivo: String) : CheckinElegibilidade
    data class JanelaFechada(val motivo: String) : CheckinElegibilidade
    data class AguardandoHorario(val minutosRestantes: Long) : CheckinElegibilidade
    data class ProntoParaCheckin(val scheduleId: Int, val dataTreino: String, val desc: String) : CheckinElegibilidade
}

object CheckinManager {

    /**
     * Extrai todos os slots disponíveis no painel da API (tanto em main.slot quanto em upcoming).
     */
    fun extrairTodosSlots(painel: JSONObject): List<JSONObject> {
        val lista = mutableListOf<JSONObject>()
        painel.optJSONObject("main")?.optJSONObject("slot")?.let { lista.add(it) }

        val upcoming = painel.optJSONArray("upcoming")
        if (upcoming != null) {
            for (i in 0 until upcoming.length()) {
                val s = upcoming.optJSONObject(i)
                if (s != null) {
                    val id = s.optInt("schedule_id")
                    val dt = s.optString("data_treino")
                    if (lista.none { it.optInt("schedule_id") == id && it.optString("data_treino") == dt }) {
                        lista.add(s)
                    }
                }
            }
        }
        return lista
    }

    /**
     * Avalia a elegibilidade de um slot de treino contra os alarmes do usuário (lógica pura, testável).
     */
    fun avaliarElegibilidade(
        slot: JSONObject,
        alarmesAtivos: List<TrainingAlarm>,
        forcarSeHouverTreino: Boolean = false,
        agoraMillis: Long = System.currentTimeMillis()
    ): CheckinElegibilidade {
        val scheduleId = slot.optInt("schedule_id")
        val dataTreino = slot.optString("data_treino")
        val diaSemana = slot.optInt("dia_semana") // 1=Segunda, 7=Domingo
        val horarioInicio = slot.optString("horario_inicio") // ex: "09:00"
        val tipoTreino = slot.optString("tipo_treino")
        val canCheckin = slot.optBoolean("can_checkin")
        val statusSlot = slot.optString("status")
        val jaFeito = !slot.isNull("checkin") || statusSlot == "realizado"
        val cancelado = slot.optBoolean("cancelled", false)

        val desc = "$tipoTreino $horarioInicio ($dataTreino)"

        if (cancelado) {
            return CheckinElegibilidade.Cancelado("O treino $desc foi cancelado pela academia.")
        }

        if (jaFeito) {
            return CheckinElegibilidade.JaRealizado("Check-in já foi realizado para $desc!")
        }

        val alarmeCorrespondente = alarmesAtivos.find { alarme ->
            alarme.diasSemana.contains(diaSemana) && (alarme.horarioTreino == horarioInicio || forcarSeHouverTreino)
        }

        if (alarmeCorrespondente == null && !forcarSeHouverTreino) {
            return CheckinElegibilidade.SemAlarme("Nenhum alarme ativo configurado para hoje às $horarioInicio.")
        }

        val offsetMin = alarmeCorrespondente?.offsetMinutos ?: 10

        if (!forcarSeHouverTreino) {
            val horarioAlvo = parseHorarioAlvo(dataTreino, horarioInicio, offsetMin)
            if (horarioAlvo != null && agoraMillis < horarioAlvo - (60000)) {
                val faltando = (horarioAlvo - agoraMillis) / 60000
                return CheckinElegibilidade.AguardandoHorario(faltando)
            }
        }

        if (!canCheckin) {
            return CheckinElegibilidade.JanelaFechada("A janela de check-in não está aberta na API no momento.")
        }

        return CheckinElegibilidade.ProntoParaCheckin(scheduleId, dataTreino, desc)
    }

    /**
     * Tenta efetuar o check-in do dia comparando os treinos disponíveis na API
     * (verificando tanto main.slot quanto upcoming) com os alarmes configurados pelo usuário.
     */
    fun tentarCheckin(
        context: Context,
        api: ApiClient,
        alarmStore: TrainingAlarmStore,
        logStore: CheckinLogStore,
        forcarSeHouverTreino: Boolean = false
    ): Pair<Boolean, String> {
        val painel = try {
            api.get("/api/checkins-treino")
        } catch (e: Exception) {
            return false to "Erro de conexão com a API: ${e.message}"
        }

        val todosSlots = extrairTodosSlots(painel)
        if (todosSlots.isEmpty()) {
            return false to "Nenhum treino disponível no momento na plataforma."
        }

        val alarmesAtivos = alarmStore.getAlarms().filter { it.enabled }
        if (alarmesAtivos.isEmpty() && !forcarSeHouverTreino) {
            return false to "Nenhum alarme de treino está ativado no aplicativo."
        }

        // Procura entre os slots disponíveis aquele que casa com os alarmes ativos
        var melhorResultado: CheckinElegibilidade? = null
        for (slot in todosSlots) {
            val res = avaliarElegibilidade(slot, alarmesAtivos, forcarSeHouverTreino)
            if (res is CheckinElegibilidade.ProntoParaCheckin) {
                melhorResultado = res
                break
            }
            if (melhorResultado == null || res is CheckinElegibilidade.JaRealizado) {
                melhorResultado = res
            }
        }

        when (val resultado = melhorResultado ?: CheckinElegibilidade.SemAlarme("Nenhum treino compatível encontrado")) {
            is CheckinElegibilidade.Cancelado -> return false to resultado.motivo
            is CheckinElegibilidade.JaRealizado -> return true to resultado.mensagem
            is CheckinElegibilidade.SemAlarme -> return false to resultado.motivo
            is CheckinElegibilidade.JanelaFechada -> return false to resultado.motivo
            is CheckinElegibilidade.AguardandoHorario ->
                return false to "Aguardando horário estimado de check-in (~${resultado.minutosRestantes} min restantes)."
            is CheckinElegibilidade.ProntoParaCheckin -> {
                val body = JSONObject().apply {
                    put("schedule_id", resultado.scheduleId)
                    put("data_treino", resultado.dataTreino)
                }

                val postResult = try {
                    api.postAuthenticated("/api/checkins-treino", body)
                } catch (e: Exception) {
                    return false to "Falha ao enviar check-in: ${e.message}"
                }

                val confirmou = (postResult.first in 200..299) || verificarConfirmacao(postResult.second, resultado.scheduleId)

                if (confirmou) {
                    logStore.addLog(resultado.scheduleId, resultado.desc, true, "Check-in automático realizado com sucesso!")
                    Notifier.notify(context, "Check-in Confirmado! 🥋", resultado.desc, true)
                    return true to "Check-in realizado com sucesso para ${resultado.desc}!"
                } else {
                    val erroMsg = "API recusou com HTTP ${postResult.first}: ${postResult.second}"
                    logStore.addLog(resultado.scheduleId, resultado.desc, false, erroMsg)
                    Notifier.notify(context, "Falha no Check-in ⚠️", erroMsg, false)
                    return false to erroMsg
                }
            }
        }
    }

    fun verificarConfirmacao(respJsonStr: String, scheduleId: Int): Boolean {
        val json = runCatching { JSONObject(respJsonStr) }.getOrNull() ?: return false
        // 1. Checa no history
        val history = json.optJSONArray("history")
        if (history != null) {
            for (i in 0 until history.length()) {
                val h = history.optJSONObject(i)
                if (h?.optInt("schedule_id") == scheduleId && h.optString("status") == "confirmado") {
                    return true
                }
            }
        }
        // 2. Checa no upcoming
        val upcoming = json.optJSONArray("upcoming")
        if (upcoming != null) {
            for (i in 0 until upcoming.length()) {
                val u = upcoming.optJSONObject(i)
                if (u?.optInt("schedule_id") == scheduleId && (!u.isNull("checkin") || u.optString("status") == "realizado")) {
                    return true
                }
            }
        }
        // 3. Checa no main.slot
        val mainSlot = json.optJSONObject("main")?.optJSONObject("slot")
        if (mainSlot?.optInt("schedule_id") == scheduleId && !mainSlot.isNull("checkin")) {
            return true
        }
        return false
    }

    fun parseHorarioAlvo(dataTreino: String, horarioInicio: String, offsetMinutos: Int): Long? {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val date = sdf.parse("$dataTreino $horarioInicio") ?: return null
            val cal = Calendar.getInstance()
            cal.time = date
            cal.add(Calendar.MINUTE, offsetMinutos)
            cal.timeInMillis
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Agenda o próximo alarme exato do Android baseado nas configurações de alarmes do usuário.
     * Se o alarme estiver desativado no switch (enabled == false), cancela o agendamento no sistema.
     */
    fun reagendarAlarmes(context: Context) {
        val alarmStore = TrainingAlarmStore(context)
        val todosAlarmes = alarmStore.getAlarms()
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        // 1. Cancela explicitamente qualquer alarme desativado ou removido no Android
        for (alarme in todosAlarmes) {
            if (!alarme.enabled) {
                for (dia in 1..7) {
                    val requestCode = (alarme.id.hashCode() * 31 + dia).let { if (it < 0) -it else it }
                    val intent = Intent(context, CheckinAlarmReceiver::class.java).apply {
                        action = "br.com.meucheckin.ACTION_EXECUTE_CHECKIN"
                        putExtra("alarm_id", alarme.id)
                    }
                    val pendingIntent = PendingIntent.getBroadcast(
                        context,
                        requestCode,
                        intent,
                        PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
                    )
                    if (pendingIntent != null) {
                        alarmManager.cancel(pendingIntent)
                        pendingIntent.cancel()
                    }
                }
            }
        }

        // 2. Filtra SOMENTE os que estão ATIVOS (switch ligado)
        val alarmesAtivos = todosAlarmes.filter { it.enabled }
        if (alarmesAtivos.isEmpty()) {
            return
        }

        val agora = Calendar.getInstance()

        // Para cada dia da semana e alarme ativo, encontra a próxima ocorrência
        for (alarme in alarmesAtivos) {
            val partes = alarme.horarioTreino.split(":")
            val hora = partes.getOrNull(0)?.toIntOrNull() ?: 9
            val minuto = partes.getOrNull(1)?.toIntOrNull() ?: 0

            for (dia in alarme.diasSemana) {
                val jitterMinutos = if (alarme.variacaoRandomica) {
                    kotlin.random.Random.nextInt(-2, 5)
                } else 0
                val jitterSegundos = if (alarme.variacaoRandomica) {
                    kotlin.random.Random.nextInt(5, 55)
                } else 0

                val offsetFinal = (alarme.offsetMinutos + jitterMinutos).coerceAtLeast(1)

                val cal = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hora)
                    set(Calendar.MINUTE, minuto + offsetFinal)
                    set(Calendar.SECOND, jitterSegundos)
                    set(Calendar.MILLISECOND, 0)
                    val calDay = when (dia) {
                        1 -> Calendar.MONDAY
                        2 -> Calendar.TUESDAY
                        3 -> Calendar.WEDNESDAY
                        4 -> Calendar.THURSDAY
                        5 -> Calendar.FRIDAY
                        6 -> Calendar.SATURDAY
                        7 -> Calendar.SUNDAY
                        else -> Calendar.MONDAY
                    }
                    set(Calendar.DAY_OF_WEEK, calDay)
                }

                if (cal.before(agora)) {
                    cal.add(Calendar.WEEK_OF_YEAR, 1)
                }

                val requestCode = (alarme.id.hashCode() * 31 + dia).let { if (it < 0) -it else it }
                val intent = Intent(context, CheckinAlarmReceiver::class.java).apply {
                    action = "br.com.meucheckin.ACTION_EXECUTE_CHECKIN"
                    putExtra("alarm_id", alarme.id)
                }
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            cal.timeInMillis,
                            pendingIntent
                        )
                    } else {
                        alarmManager.setExact(
                            AlarmManager.RTC_WAKEUP,
                            cal.timeInMillis,
                            pendingIntent
                        )
                    }
                } catch (e: SecurityException) {
                    // Sem permissão de alarme exato ainda, o WorkManager cobrirá
                }
            }
        }
    }
}
