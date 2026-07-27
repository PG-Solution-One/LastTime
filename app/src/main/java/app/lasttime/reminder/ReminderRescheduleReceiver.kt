package app.lasttime.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import app.lasttime.LastTimeApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ReminderRescheduleReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        if (intent.action !in SUPPORTED_ACTIONS) return
        val pendingResult = goAsync()
        val app = context.applicationContext as LastTimeApplication
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                app.container.reminderScheduler.syncAll()
            } finally {
                pendingResult.finish()
            }
        }
    }

    private companion object {
        val SUPPORTED_ACTIONS =
            setOf(
                Intent.ACTION_BOOT_COMPLETED,
                Intent.ACTION_TIME_CHANGED,
                Intent.ACTION_TIMEZONE_CHANGED,
            )
    }
}
