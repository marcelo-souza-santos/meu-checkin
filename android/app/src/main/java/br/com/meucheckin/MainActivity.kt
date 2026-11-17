package br.com.meucheckin

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.meucheckin.automation.CheckinManager
import br.com.meucheckin.automation.CheckinScheduler
import br.com.meucheckin.data.ApiClient
import br.com.meucheckin.data.SecureSessionStore
import br.com.meucheckin.data.local.CheckinLogStore
import br.com.meucheckin.data.local.TrainingAlarmStore
import br.com.meucheckin.data.repository.CheckinRepositoryImpl
import br.com.meucheckin.ui.screens.ClockAlarmsScreen
import br.com.meucheckin.ui.screens.LoadingScreen
import br.com.meucheckin.ui.screens.LoginScreen
import br.com.meucheckin.ui.state.UiState
import br.com.meucheckin.ui.theme.ClockDarkBackground
import br.com.meucheckin.ui.theme.MeuCheckinTheme
import br.com.meucheckin.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val secureStore = SecureSessionStore(applicationContext)
        val api = ApiClient(secureStore)
        val alarmStore = TrainingAlarmStore(applicationContext)
        val logStore = CheckinLogStore(applicationContext)
        val repository = CheckinRepositoryImpl(api, alarmStore, logStore)

        CheckinScheduler.iniciarAgendamentos(applicationContext)
        CheckinManager.reagendarAlarmes(applicationContext)

        setContent {
            val context = LocalContext.current
            var notificationPermissionGranted by remember {
                mutableStateOf(
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) == PackageManager.PERMISSION_GRANTED
                    } else true
                )
            }

            val permissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { isGranted -> notificationPermissionGranted = isGranted }

            LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !notificationPermissionGranted) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }

            MeuCheckinTheme {
                Surface(Modifier.fillMaxSize(), color = ClockDarkBackground) {
                    val vm: MainViewModel = viewModel(factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>) =
                            MainViewModel(repository, api, alarmStore, logStore) as T
                    })

                    when (vm.state) {
                        UiState.Login -> LoginScreen(vm.error) { email, pass ->
                            vm.login(email, pass, context)
                        }
                        UiState.Loading -> LoadingScreen()
                        UiState.Alarms -> ClockAlarmsScreen(
                            alarms = vm.alarms,
                            perfil = vm.perfilAluno,
                            availableTreinos = vm.availableTreinos,
                            isExecuting = vm.isExecutingAction,
                            feedback = vm.actionFeedback,
                            onToggleAlarm = { id, en -> vm.toggleAlarm(id, en, context) },
                            onSaveAlarm = { alarm -> vm.saveAlarm(alarm, context) },
                            onDeleteAlarm = { id -> vm.deleteAlarm(id, context) },
                            onTestCheckin = { vm.testarCheckinAgora(context) },
                            onLogout = vm::logout
                        )
                    }
                }
            }
        }
    }
}
