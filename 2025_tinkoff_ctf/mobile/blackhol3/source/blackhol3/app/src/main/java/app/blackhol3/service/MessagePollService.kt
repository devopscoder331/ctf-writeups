package app.blackhol3.service

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import app.blackhol3.data.local.dao.ConfigDao
import app.blackhol3.data.remote.dao.RemoteMessageDao
import app.blackhol3.repository.MessagesRepository
import app.blackhol3.repository.PrivateKeyRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.concurrent.TimeUnit

class MessagePollService : KoinComponent {
    private val configDao: ConfigDao by inject()
    private val remoteMessageDao: RemoteMessageDao by inject()
    private val messagesRepository: MessagesRepository by inject()
    private val privateKeyRepository: PrivateKeyRepository by inject()

    companion object {
        private const val LAST_UPDATE_TIMESTAMP_KEY = "last_message_update_timestamp"
        private const val WORK_NAME = "message_update_worker"

        fun scheduleUpdates(
            context: Context,
            intervalMinutes: Long = 1,
        ) {
            val constraints =
                Constraints
                    .Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()

            val updateWorkRequest =
                PeriodicWorkRequestBuilder<MessageUpdateWorker>(
                    intervalMinutes,
                    TimeUnit.MINUTES,
                ).setConstraints(constraints)
                    .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                updateWorkRequest,
            )
        }
    }

    suspend fun fetchAndProcessUpdates(): Int =
        withContext(Dispatchers.IO) {
            var totalNewMessages = 0
            val lastUpdateTimestamp = configDao.getLong(LAST_UPDATE_TIMESTAMP_KEY, 0L)
            val currentTimestamp = System.currentTimeMillis()

            try {
                val privateKeys = privateKeyRepository.privateKeys().first()

                for (privateKey in privateKeys) {
                    try {
                        val newMessages =
                            remoteMessageDao.fetchNewMessages(
                                privateKey = privateKey,
                                since = lastUpdateTimestamp,
                            )

                        for ((publicKey, message) in newMessages) {
                            messagesRepository.incomingMessage(
                                privateKey = privateKey,
                                senderPublicKey = publicKey,
                                message = message,
                            )
                            totalNewMessages++
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                configDao.putLong(LAST_UPDATE_TIMESTAMP_KEY, currentTimestamp)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return@withContext totalNewMessages
        }
}

class MessageUpdateWorker(
    context: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams),
    KoinComponent {
    private val updateService: MessagePollService by inject()

    override suspend fun doWork(): Result =
        try {
            updateService.fetchAndProcessUpdates()
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
}

class MessageUpdateManager(
    private val context: Context,
) : KoinComponent {
    fun initialize(
        enableBackground: Boolean = true,
        intervalMinutes: Long = 15,
    ) {
        if (enableBackground) {
            MessagePollService.scheduleUpdates(context, intervalMinutes)
        }
    }
}
