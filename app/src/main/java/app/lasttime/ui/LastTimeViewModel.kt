package app.lasttime.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import app.lasttime.data.preferences.UserPreferencesRepository
import app.lasttime.data.repository.CompletionResult
import app.lasttime.data.repository.EventRepository
import app.lasttime.domain.EventDraft
import app.lasttime.domain.ThemeMode
import app.lasttime.domain.TrackedEvent
import app.lasttime.reminder.ReminderScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate

class LastTimeViewModel(
    private val repository: EventRepository,
    private val reminderScheduler: ReminderScheduler,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val clock: Clock = Clock.systemDefaultZone(),
) : ViewModel() {
    val events: StateFlow<List<TrackedEvent>> =
        repository
            .observeEvents()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val themeMode: StateFlow<ThemeMode> =
        userPreferencesRepository.themeMode
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ThemeMode.SYSTEM)

    private val _today = MutableStateFlow(LocalDate.now(clock))
    val today: StateFlow<LocalDate> = _today.asStateFlow()

    fun refreshDate() {
        _today.value = LocalDate.now(clock)
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            userPreferencesRepository.setThemeMode(mode)
        }
    }

    fun observeEvent(id: Long): Flow<TrackedEvent?> = repository.observeEvent(id)

    fun create(
        draft: EventDraft,
        onComplete: (Long) -> Unit,
        onError: () -> Unit,
    ) {
        viewModelScope.launch {
            runCatching { repository.create(draft) }
                .onSuccess { id ->
                    rescheduleSafely(id)
                    onComplete(id)
                }.onFailure { onError() }
        }
    }

    fun update(
        id: Long,
        draft: EventDraft,
        onComplete: () -> Unit,
        onError: () -> Unit,
    ) {
        viewModelScope.launch {
            runCatching { repository.update(id, draft) }
                .onSuccess {
                    rescheduleSafely(id)
                    onComplete()
                }.onFailure { onError() }
        }
    }

    fun delete(
        id: Long,
        onComplete: () -> Unit,
        onError: () -> Unit,
    ) {
        viewModelScope.launch {
            runCatching { repository.delete(id) }
                .onSuccess {
                    reminderScheduler.cancel(id)
                    onComplete()
                }.onFailure { onError() }
        }
    }

    fun recordCompletion(
        eventId: Long,
        date: LocalDate,
        onResult: (CompletionResult) -> Unit,
    ) {
        viewModelScope.launch {
            val result =
                runCatching { repository.recordCompletion(eventId, date) }
                    .getOrDefault(CompletionResult.Failed)
            if (result == CompletionResult.Success) rescheduleSafely(eventId)
            onResult(result)
        }
    }

    fun updateCompletion(
        eventId: Long,
        completionId: Long,
        date: LocalDate,
        onResult: (CompletionResult) -> Unit,
    ) {
        viewModelScope.launch {
            val result =
                runCatching { repository.updateCompletion(completionId, date) }
                    .getOrDefault(CompletionResult.Failed)
            if (result == CompletionResult.Success) rescheduleSafely(eventId)
            onResult(result)
        }
    }

    fun deleteCompletion(
        eventId: Long,
        completionId: Long,
        onResult: (CompletionResult) -> Unit,
    ) {
        viewModelScope.launch {
            val result =
                runCatching { repository.deleteCompletion(completionId) }
                    .getOrDefault(CompletionResult.Failed)
            if (result == CompletionResult.Success) rescheduleSafely(eventId)
            onResult(result)
        }
    }

    private suspend fun rescheduleSafely(eventId: Long) {
        runCatching { reminderScheduler.schedule(eventId) }
    }

    class Factory(
        private val repository: EventRepository,
        private val reminderScheduler: ReminderScheduler,
        private val userPreferencesRepository: UserPreferencesRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            LastTimeViewModel(
                repository,
                reminderScheduler,
                userPreferencesRepository,
            ) as T
    }
}
