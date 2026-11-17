package br.com.meucheckin.data.repository

import br.com.meucheckin.domain.model.AlunoPerfil
import br.com.meucheckin.domain.model.CheckinLog
import br.com.meucheckin.domain.model.TrainingAlarm
import br.com.meucheckin.domain.model.TreinoOpcao

interface CheckinRepository {
    fun hasSavedCredentials(): Boolean
    fun login(email: String, pass: String)
    fun logout()
    fun getAlunoPerfil(): AlunoPerfil?
    fun getTreinosDisponiveis(): List<TreinoOpcao>
    fun getAlarms(): List<TrainingAlarm>
    fun saveAlarm(alarm: TrainingAlarm)
    fun deleteAlarm(id: String)
    fun toggleAlarm(id: String, enabled: Boolean)
    fun getLogs(): List<CheckinLog>
}

