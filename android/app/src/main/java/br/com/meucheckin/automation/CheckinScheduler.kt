package br.com.meucheckin.automation

import android.content.Context
import androidx.work.*
import br.com.meucheckin.data.ApiClient
import br.com.meucheckin.data.SecureSessionStore
import br.com.meucheckin.data.local.CheckinLogStore
import br.com.meucheckin.data.local.TrainingAlarmStore
import java.util.concurrent.TimeUnit

class CheckinWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val secureStore = SecureSessionStore(context)
        val api = ApiClient(secureStore)
        val alarmStore = TrainingAlarmStore(context)
        val logStore = CheckinLogStore(context)

        if (!api.hasSavedCredentials()) {
            return Result.success()
        }

        // 1. Reagenda alarmes exatos
        CheckinManager.reagendarAlarmes(context)

        // 2. Tenta check-in se estiver no dia/horário de algum alarme configurado
        CheckinManager.tentarCheckin(context, api, alarmStore, logStore, forcarSeHouverTreino = false)

        return Result.success()
    }
}

object CheckinScheduler {
    private const val WORK_NAME = "PeriodicCheckinSafetyWorker"

    fun iniciarAgendamentos(context: Context) {
        val request = PeriodicWorkRequestBuilder<CheckinWorker>(15, TimeUnit.MINUTES)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}
