package app.lasttime.reminder

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import app.lasttime.data.repository.EventRepository
import java.time.Clock
import java.time.Duration
import java.time.LocalTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit

class WorkReminderScheduler(
    context: Context,
    private val repository: EventRepository,
    private val clock: Clock = Clock.systemDefaultZone(),
) : ReminderScheduler {
    private val workManager = WorkManager.getInstance(context.applicationContext)

    override suspend fun schedule(eventId: Long) {
        val event = repository.getEvent(eventId)
        val offset = event?.reminderDaysBefore
        if (event == null || offset == null) {
            cancel(eventId)
            return
        }

        val zone = ZoneId.systemDefault()
        val triggerAt =
            event.nextDate
                .minusDays(offset.toLong())
                .atTime(LocalTime.of(9, 0))
                .atZone(zone)
                .toInstant()
        val now = clock.instant()
        if (!triggerAt.isAfter(now)) {
            cancel(eventId)
            return
        }

        val delay = Duration.between(now, triggerAt)
        val request =
            OneTimeWorkRequestBuilder<ReminderWorker>()
                .setInitialDelay(delay.toMillis(), TimeUnit.MILLISECONDS)
                .setInputData(
                    workDataOf(
                        ReminderWorker.EVENT_ID_KEY to eventId,
                        ReminderWorker.EXPECTED_DUE_EPOCH_DAY_KEY to event.nextDate.toEpochDay(),
                    ),
                ).build()
        workManager.enqueueUniqueWork(
            workName(eventId),
            ExistingWorkPolicy.REPLACE,
            request,
        )
    }

    override fun cancel(eventId: Long) {
        workManager.cancelUniqueWork(workName(eventId))
    }

    override suspend fun syncAll() {
        repository.getEvents().forEach { schedule(it.id) }
    }

    private fun workName(eventId: Long) = "event_reminder_$eventId"
}
