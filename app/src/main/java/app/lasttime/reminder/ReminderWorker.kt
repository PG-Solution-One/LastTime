package app.lasttime.reminder

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import app.lasttime.LastTimeApplication
import app.lasttime.MainActivity
import app.lasttime.R
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class ReminderWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val eventId = inputData.getLong(EVENT_ID_KEY, -1)
        val expectedDueDate = inputData.getLong(EXPECTED_DUE_EPOCH_DAY_KEY, Long.MIN_VALUE)
        if (eventId <= 0 || expectedDueDate == Long.MIN_VALUE) return Result.failure()

        val app = applicationContext as LastTimeApplication
        val event = app.container.repository.getEvent(eventId) ?: return Result.success()
        if (event.nextDate.toEpochDay() != expectedDueDate || event.reminderDaysBefore == null) {
            return Result.success()
        }

        if (
            ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return Result.success()
        }

        val intent =
            Intent(applicationContext, MainActivity::class.java)
                .putExtra(MainActivity.EXTRA_EVENT_ID, eventId)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val pendingIntent =
            PendingIntent.getActivity(
                applicationContext,
                eventId.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        val notification =
            NotificationCompat
                .Builder(
                    applicationContext,
                    LastTimeApplication.REMINDERS_CHANNEL_ID,
                ).setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(event.title)
                .setContentText(
                    applicationContext.getString(
                        R.string.notification_due,
                        event.nextDate.format(
                            DateTimeFormatter
                                .ofLocalizedDate(FormatStyle.LONG)
                                .withLocale(applicationContext.resources.configuration.locales[0]),
                        ),
                    ),
                ).setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()

        applicationContext
            .getSystemService(NotificationManager::class.java)
            .notify(eventId.hashCode(), notification)
        return Result.success()
    }

    companion object {
        const val EVENT_ID_KEY = "event_id"
        const val EXPECTED_DUE_EPOCH_DAY_KEY = "expected_due_epoch_day"
    }
}
