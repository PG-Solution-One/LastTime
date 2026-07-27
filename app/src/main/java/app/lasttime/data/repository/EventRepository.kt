package app.lasttime.data.repository

import android.database.sqlite.SQLiteConstraintException
import androidx.room.withTransaction
import app.lasttime.data.local.CompletionEntity
import app.lasttime.data.local.EventEntity
import app.lasttime.data.local.EventWithCompletions
import app.lasttime.data.local.LastTimeDatabase
import app.lasttime.domain.Completion
import app.lasttime.domain.EventDraft
import app.lasttime.domain.RepeatInterval
import app.lasttime.domain.TrackedEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Clock
import java.time.Instant
import java.time.LocalDate

sealed interface CompletionResult {
    data object Success : CompletionResult

    data object DuplicateDate : CompletionResult

    data object LastCompletion : CompletionResult

    data object NotFound : CompletionResult

    data object Failed : CompletionResult
}

interface EventRepository {
    fun observeEvents(): Flow<List<TrackedEvent>>

    fun observeEvent(id: Long): Flow<TrackedEvent?>

    suspend fun getEvents(): List<TrackedEvent>

    suspend fun getEvent(id: Long): TrackedEvent?

    suspend fun create(draft: EventDraft): Long

    suspend fun update(
        id: Long,
        draft: EventDraft,
    )

    suspend fun delete(id: Long)

    suspend fun recordCompletion(
        eventId: Long,
        date: LocalDate,
    ): CompletionResult

    suspend fun updateCompletion(
        completionId: Long,
        date: LocalDate,
    ): CompletionResult

    suspend fun deleteCompletion(completionId: Long): CompletionResult
}

internal class RoomEventRepository(
    private val database: LastTimeDatabase,
    private val clock: Clock = Clock.systemDefaultZone(),
) : EventRepository {
    private val dao = database.eventDao()

    override fun observeEvents(): Flow<List<TrackedEvent>> =
        dao.observeAll().map { rows -> rows.mapNotNull(EventWithCompletions::toDomain) }

    override fun observeEvent(id: Long): Flow<TrackedEvent?> = dao.observeById(id).map { it?.toDomain() }

    override suspend fun getEvents(): List<TrackedEvent> = dao.getAll().mapNotNull(EventWithCompletions::toDomain)

    override suspend fun getEvent(id: Long): TrackedEvent? = dao.getById(id)?.toDomain()

    override suspend fun create(draft: EventDraft): Long =
        database.withTransaction {
            val now = Instant.now(clock)
            val eventId =
                dao.insertEvent(
                    EventEntity(
                        title = draft.title.trim(),
                        category = draft.category,
                        repeatAmount = draft.interval.amount,
                        repeatUnit = draft.interval.unit,
                        reminderDaysBefore = draft.reminderDaysBefore,
                        note = draft.note.trim(),
                        createdAt = now,
                        updatedAt = now,
                    ),
                )
            dao.insertCompletion(
                CompletionEntity(
                    eventId = eventId,
                    completedDate = draft.initialDate,
                    createdAt = now,
                ),
            )
            eventId
        }

    override suspend fun update(
        id: Long,
        draft: EventDraft,
    ) {
        database.withTransaction {
            val current = dao.getById(id) ?: return@withTransaction
            dao.updateEvent(
                current.event.copy(
                    title = draft.title.trim(),
                    category = draft.category,
                    repeatAmount = draft.interval.amount,
                    repeatUnit = draft.interval.unit,
                    reminderDaysBefore = draft.reminderDaysBefore,
                    note = draft.note.trim(),
                    updatedAt = Instant.now(clock),
                ),
            )
            val latest = current.completions.maxByOrNull { it.completedDate }
            if (latest != null && latest.completedDate != draft.initialDate) {
                dao.updateCompletion(latest.copy(completedDate = draft.initialDate))
            }
        }
    }

    override suspend fun delete(id: Long) {
        database.withTransaction {
            dao.getById(id)?.let { dao.deleteEvent(it.event) }
        }
    }

    override suspend fun recordCompletion(
        eventId: Long,
        date: LocalDate,
    ): CompletionResult {
        if (dao.getById(eventId) == null) return CompletionResult.NotFound
        return try {
            dao.insertCompletion(
                CompletionEntity(
                    eventId = eventId,
                    completedDate = date,
                    createdAt = Instant.now(clock),
                ),
            )
            CompletionResult.Success
        } catch (_: SQLiteConstraintException) {
            CompletionResult.DuplicateDate
        }
    }

    override suspend fun updateCompletion(
        completionId: Long,
        date: LocalDate,
    ): CompletionResult {
        val completion = dao.getCompletion(completionId) ?: return CompletionResult.NotFound
        return try {
            dao.updateCompletion(completion.copy(completedDate = date))
            CompletionResult.Success
        } catch (_: SQLiteConstraintException) {
            CompletionResult.DuplicateDate
        }
    }

    override suspend fun deleteCompletion(completionId: Long): CompletionResult {
        val completion = dao.getCompletion(completionId) ?: return CompletionResult.NotFound
        if (dao.completionCount(completion.eventId) <= 1) return CompletionResult.LastCompletion
        dao.deleteCompletion(completion)
        return CompletionResult.Success
    }
}

private fun EventWithCompletions.toDomain(): TrackedEvent? {
    if (completions.isEmpty()) return null
    return TrackedEvent(
        id = event.id,
        title = event.title,
        category = event.category,
        interval = RepeatInterval(event.repeatAmount, event.repeatUnit),
        reminderDaysBefore = event.reminderDaysBefore,
        note = event.note,
        completions =
            completions
                .sortedByDescending { it.completedDate }
                .map { Completion(id = it.id, date = it.completedDate) },
    )
}
