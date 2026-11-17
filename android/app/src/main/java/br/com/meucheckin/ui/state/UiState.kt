package br.com.meucheckin.ui.state

sealed interface UiState {
    object Login : UiState
    object Loading : UiState
    object Alarms : UiState
}

