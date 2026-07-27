package app.lasttime.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.lasttime.data.local.LastTimeDatabase
import app.lasttime.domain.EventCategory
import app.lasttime.domain.EventDraft
import app.lasttime.domain.RepeatInterval
import app.lasttime.domain.RepeatUnit
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@RunWith(AndroidJUnit4::class)
class EventRepositoryTest {
    private lateinit var database: LastTimeDatabase
    private lateinit var repository: EventRepository
    private val today = LocalDate.of(2026, 7, 27)

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database =
            Room
                .inMemoryDatabaseBuilder(context, LastTimeDatabase::class.java)
                .allowMainThreadQueries()
                .build()
        repository =
            RoomEventRepository(
                database,
                Clock.fixed(Instant.parse("2026-07-27T10:00:00Z"), ZoneOffset.UTC),
            )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun createsEventWithInitialHistoryEntry() =
        runTest {
            val id = repository.create(draft())

            val event = repository.getEvent(id)!!
            assertEquals("Замена масла", event.title)
            assertEquals(today.minusMonths(5), event.lastDate)
            assertEquals(today.plusMonths(1), event.nextDate)
            assertEquals(1, event.completions.size)
        }

    @Test
    fun rejectsDuplicateDateAndProtectsLastCompletion() =
        runTest {
            val id = repository.create(draft())
            val event = repository.getEvent(id)!!

            assertEquals(
                CompletionResult.DuplicateDate,
                repository.recordCompletion(id, event.lastDate),
            )
            assertEquals(
                CompletionResult.LastCompletion,
                repository.deleteCompletion(event.completions.single().id),
            )
        }

    @Test
    fun latestCompletionRecalculatesNextDate() =
        runTest {
            val id = repository.create(draft())

            assertEquals(CompletionResult.Success, repository.recordCompletion(id, today))

            val event = repository.getEvent(id)!!
            assertEquals(today, event.lastDate)
            assertEquals(today.plusMonths(6), event.nextDate)
            assertEquals(2, event.completions.size)
        }

    @Test
    fun deletingEventCascadesAndRemovesItFromFlow() =
        runTest {
            val id = repository.create(draft())

            repository.delete(id)

            assertNull(repository.getEvent(id))
            assertEquals(emptyList<Any>(), repository.getEvents())
        }

    private fun draft() =
        EventDraft(
            title = "Замена масла",
            category = EventCategory.CAR,
            initialDate = today.minusMonths(5),
            interval = RepeatInterval(6, RepeatUnit.MONTHS),
            reminderDaysBefore = 7,
            note = "Проверить фильтр",
        )
}
