package br.com.meucheckin.ui.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.meucheckin.automation.CheckinManager
import br.com.meucheckin.data.ApiClient
import br.com.meucheckin.data.AuthException
import br.com.meucheckin.data.local.CheckinLogStore
import br.com.meucheckin.data.local.TrainingAlarmStore
import br.com.meucheckin.data.repository.CheckinRepository
import br.com.meucheckin.domain.model.AlunoPerfil
import br.com.meucheckin.domain.model.TrainingAlarm
import br.com.meucheckin.domain.model.TreinoOpcao
import br.com.meucheckin.ui.state.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(
    private val repository: CheckinRepository,
    private val api: ApiClient,
    private val alarmStore: TrainingAlarmStore,
    private val logStore: CheckinLogStore
) : ViewModel() {

    var state by mutableStateOf<UiState>(if (repository.hasSavedCredentials()) UiState.Loading else UiState.Login)
        private set
    var error by mutableStateOf<String?>(null)
        private set
    var alarms by mutableStateOf(repository.getAlarms())
        private set
    var perfilAluno by mutableStateOf<AlunoPerfil?>(null)
        private set
    var availableTreinos by mutableStateOf<List<TreinoOpcao>>(emptyList())
        private set
    var isExecutingAction by mutableStateOf(false)
        private set
    var actionFeedback by mutableStateOf<String?>(null)
        private set

    init {
        if (state is UiState.Loading) {
            verificarSessao()
        }
    }

    private fun verificarSessao() {
        state = UiState.Loading
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    perfilAluno = repository.getAlunoPerfil()
                    availableTreinos = repository.getTreinosDisponiveis()
                }
                alarms = repository.getAlarms()
                state = UiState.Alarms
            } catch (e: Exception) {
                error = if (e is AuthException) e.message else "Conectando ao servidor..."
                state = if (repository.hasSavedCredentials()) UiState.Alarms else UiState.Login
            }
        }
    }

    fun login(email: String, pass: String, context: Context) {
        error = null
        state = UiState.Loading
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    repository.login(email.trim(), pass)
                    perfilAluno = repository.getAlunoPerfil()
                    availableTreinos = repository.getTreinosDisponiveis()
                }
                alarms = repository.getAlarms()
                withContext(Dispatchers.IO) { CheckinManager.reagendarAlarmes(context) }
                state = UiState.Alarms
            } catch (e: Exception) {
                error = e.message ?: "Erro no login"
                state = UiState.Login
            }
        }
    }

    fun toggleAlarm(id: String, enabled: Boolean, context: Context) {
        repository.toggleAlarm(id, enabled)
        alarms = repository.getAlarms()
        viewModelScope.launch(Dispatchers.IO) {
            CheckinManager.reagendarAlarmes(context)
        }
    }

    fun saveAlarm(alarm: TrainingAlarm, context: Context) {
        repository.saveAlarm(alarm)
        alarms = repository.getAlarms()
        viewModelScope.launch(Dispatchers.IO) {
            CheckinManager.reagendarAlarmes(context)
        }
    }

    fun deleteAlarm(id: String, context: Context) {
        repository.deleteAlarm(id)
        alarms = repository.getAlarms()
        viewModelScope.launch(Dispatchers.IO) {
            CheckinManager.reagendarAlarmes(context)
        }
    }

    fun testarCheckinAgora(context: Context) {
        isExecutingAction = true
        actionFeedback = null
        viewModelScope.launch {
            val (ok, msg) = withContext(Dispatchers.IO) {
                CheckinManager.tentarCheckin(context, api, alarmStore, logStore, forcarSeHouverTreino = true)
            }
            isExecutingAction = false
            actionFeedback = if (ok) "✅ $msg" else "⚠️ $msg"
        }
    }

    fun logout() {
        repository.logout()
        state = UiState.Login
    }
}

