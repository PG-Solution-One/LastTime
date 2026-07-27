package app.lasttime.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class ModelsTest {
    @Test
    fun `month interval uses last available day`() {
        val interval = RepeatInterval(1, RepeatUnit.MONTHS)

        assertEquals(
            LocalDate.of(2025, 2, 28),
            interval.addTo(LocalDate.of(2025, 1, 31)),
        )
    }

    @Test
    fun `year interval handles leap day`() {
        val interval = RepeatInterval(1, RepeatUnit.YEARS)

        assertEquals(
            LocalDate.of(2025, 2, 28),
            interval.addTo(LocalDate.of(2024, 2, 29)),
        )
    }

    @Test
    fun `status follows urgency boundaries`() {
        val today = LocalDate.of(2026, 7, 27)

        assertEquals(EventStatus.OVERDUE, eventDueOn(today.minusDays(1)).status(today))
        assertEquals(EventStatus.TODAY, eventDueOn(today).status(today))
        assertEquals(EventStatus.SOON, eventDueOn(today.plusDays(7)).status(today))
        assertEquals(EventStatus.UPCOMING, eventDueOn(today.plusDays(8)).status(today))
    }

    @Test
    fun `valid draft has no validation error`() {
        val today = LocalDate.of(2026, 7, 27)
        val draft =
            EventDraft(
                title = "Замена масла",
                category = EventCategory.CAR,
                initialDate = today.minusMonths(2),
                interval = RepeatInterval(6, RepeatUnit.MONTHS),
                reminderDaysBefore = 7,
                note = "",
            )

        assertNull(validateDraft(draft, today))
    }

    @Test
    fun `future completion is rejected`() {
        val today = LocalDate.of(2026, 7, 27)
        val draft =
            EventDraft(
                title = "Стоматолог",
                category = EventCategory.HEALTH,
                initialDate = today.plusDays(1),
                interval = RepeatInterval(1, RepeatUnit.YEARS),
                reminderDaysBefore = 14,
                note = "",
            )

        assertNotNull(validateDraft(draft, today))
    }

    @Test
    fun `reminder cannot be longer than interval`() {
        val today = LocalDate.of(2026, 7, 27)
        val draft =
            EventDraft(
                title = "Полив",
                category = EventCategory.PLANTS,
                initialDate = today,
                interval = RepeatInterval(3, RepeatUnit.DAYS),
                reminderDaysBefore = 3,
                note = "",
            )

        assertNotNull(validateDraft(draft, today))
    }

    private fun eventDueOn(dueDate: LocalDate): TrackedEvent =
        TrackedEvent(
            id = 1,
            title = "Тест",
            category = EventCategory.OTHER,
            interval = RepeatInterval(10, RepeatUnit.DAYS),
            reminderDaysBefore = null,
            note = "",
            completions = listOf(Completion(1, dueDate.minusDays(10))),
        )
}
