package br.com.meucheckin.data.repository

import br.com.meucheckin.data.ApiClient
import br.com.meucheckin.data.local.CheckinLogStore
import br.com.meucheckin.data.local.TrainingAlarmStore
import br.com.meucheckin.domain.model.AlunoPerfil
import br.com.meucheckin.domain.model.CheckinLog
import br.com.meucheckin.domain.model.TrainingAlarm
import br.com.meucheckin.domain.model.TreinoOpcao
import org.json.JSONObject

class CheckinRepositoryImpl(
    private val api: ApiClient,
    private val alarmStore: TrainingAlarmStore,
    private val logStore: CheckinLogStore
) : CheckinRepository {

    override fun hasSavedCredentials(): Boolean = api.hasSavedCredentials()

    override fun login(email: String, pass: String) {
        api.login(email.trim(), pass)
    }

    override fun logout() {
        api.logout()
    }

    override fun getAlunoPerfil(): AlunoPerfil? {
        return try {
            val grad = api.get("/api/graduacoes/me")
            val alunoObj = grad.optJSONObject("aluno")
            if (alunoObj != null) {
                val nome = alunoObj.optString("nome_completo")
                val faixa = alunoObj.optString("cor_faixa")
                val graus = alunoObj.optInt("qntd_graus")
                val prox = alunoObj.optJSONObject("graduacao_status")?.optString("proxima_faixa")
                AlunoPerfil(nome, faixa, graus, prox)
            } else {
                val me = api.get("/api/auth/me?photoMode=metadata")
                AlunoPerfil(
                    nome = me.optString("full_name"),
                    faixa = me.optString("cor_faixa"),
                    graus = me.optInt("qntd_graus"),
                    proximaFaixa = null
                )
            }
        } catch (e: Exception) {
            null
        }
    }

    override fun getTreinosDisponiveis(): List<TreinoOpcao> {
        return try {
            val painel = api.get("/api/checkins-treino")
            val lista = mutableListOf<TreinoOpcao>()

            fun extrair(obj: JSONObject?) {
                if (obj == null) return
                val sched = obj.optInt("schedule_id")
                val tipo = obj.optString("tipo_treino")
                val ini = obj.optString("horario_inicio")
                val fim = obj.optString("horario_fim")
                val dia = obj.optInt("dia_semana")
                if (ini.isNotBlank()) {
                    lista.add(TreinoOpcao(sched, tipo, ini, fim, dia))
                }
            }

            extrair(painel.optJSONObject("main")?.optJSONObject("slot"))
            val upcoming = painel.optJSONArray("upcoming")
            if (upcoming != null) {
                for (i in 0 until upcoming.length()) {
                    extrair(upcoming.getJSONObject(i))
                }
            }

            val parsed = lista.distinctBy { "${it.tipoTreino}_${it.horarioInicio}" }
                .sortedBy { it.horarioInicio }

            if (parsed.isNotEmpty()) parsed else fallbackTreinos()
        } catch (e: Exception) {
            fallbackTreinos()
        }
    }

    private fun fallbackTreinos(): List<TreinoOpcao> {
        return listOf(
            TreinoOpcao(26, "Gi", "06:00", "07:30", 2),
            TreinoOpcao(2, "Gi", "09:00", "10:30", 1),
            TreinoOpcao(11, "Treino Extra", "16:00", "17:30", 6),
            TreinoOpcao(4, "Gi", "17:00", "18:30", 2),
            TreinoOpcao(3, "Gi", "20:00", "21:30", 1),
            TreinoOpcao(1, "No-Gi", "20:00", "21:30", 3)
        )
    }

    override fun getAlarms(): List<TrainingAlarm> = alarmStore.getAlarms()

    override fun saveAlarm(alarm: TrainingAlarm) {
        alarmStore.saveAlarm(alarm)
    }

    override fun deleteAlarm(id: String) {
        alarmStore.deleteAlarm(id)
    }

    override fun toggleAlarm(id: String, enabled: Boolean) {
        alarmStore.toggleAlarm(id, enabled)
    }

    override fun getLogs(): List<CheckinLog> = logStore.getLogs()
}

