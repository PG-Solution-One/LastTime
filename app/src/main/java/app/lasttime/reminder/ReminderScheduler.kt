package app.lasttime.reminder

interface ReminderScheduler {
    suspend fun schedule(eventId: Long)

    fun cancel(eventId: Long)

    suspend fun syncAll()
}
