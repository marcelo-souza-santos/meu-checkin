package br.com.meucheckin.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import br.com.meucheckin.automation.CheckinManager
import br.com.meucheckin.automation.CheckinScheduler
import br.com.meucheckin.data.ApiClient
import br.com.meucheckin.data.SecureSessionStore
import br.com.meucheckin.data.local.CheckinLogStore
import br.com.meucheckin.data.local.TrainingAlarmStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CheckinAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val action = intent.action
                val secureStore = SecureSessionStore(context)
                val api = ApiClient(secureStore)
                val alarmStore = TrainingAlarmStore(context)
                val logStore = CheckinLogStore(context)

                if (Intent.ACTION_BOOT_COMPLETED == action) {
                    CheckinScheduler.iniciarAgendamentos(context)
                    CheckinManager.reagendarAlarmes(context)
                } else if ("br.com.meucheckin.ACTION_EXECUTE_CHECKIN" == action) {
                    if (api.hasSavedCredentials()) {
                        CheckinManager.tentarCheckin(context, api, alarmStore, logStore, forcarSeHouverTreino = false)
                    }
                    CheckinManager.reagendarAlarmes(context)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
