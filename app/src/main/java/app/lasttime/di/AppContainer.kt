package app.lasttime.di

import android.content.Context
import app.lasttime.data.local.LastTimeDatabase
import app.lasttime.data.preferences.DataStoreUserPreferencesRepository
import app.lasttime.data.preferences.UserPreferencesRepository
import app.lasttime.data.repository.EventRepository
import app.lasttime.data.repository.RoomEventRepository
import app.lasttime.reminder.ReminderScheduler
import app.lasttime.reminder.WorkReminderScheduler

class AppContainer(
    context: Context,
) {
    private val database = LastTimeDatabase.create(context)
    val repository: EventRepository = RoomEventRepository(database)
    val userPreferencesRepository: UserPreferencesRepository =
        DataStoreUserPreferencesRepository(context)
    val reminderScheduler: ReminderScheduler =
        WorkReminderScheduler(context, repository)
}
