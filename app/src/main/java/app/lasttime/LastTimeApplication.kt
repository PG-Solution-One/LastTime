package app.lasttime

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import app.lasttime.di.AppContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class LastTimeApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        createNotificationChannel()
        applicationScope.launch {
            container.reminderScheduler.syncAll()
        }
    }

    private fun createNotificationChannel() {
        val channel =
            NotificationChannel(
                REMINDERS_CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = getString(R.string.notification_channel_description)
            }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    companion object {
        const val REMINDERS_CHANNEL_ID = "event_reminders"
    }
}
