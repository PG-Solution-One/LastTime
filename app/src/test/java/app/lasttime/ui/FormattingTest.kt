package app.lasttime.ui

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class FormattingTest {
    private val today = LocalDate.of(2026, 7, 27)

    @Test
    fun `recognizes today and yesterday`() {
        assertEquals(ElapsedPeriod(0, ElapsedUnit.TODAY), elapsedPeriod(today, today))
        assertEquals(
            ElapsedPeriod(1, ElapsedUnit.YESTERDAY),
            elapsedPeriod(today.minusDays(1), today),
        )
    }

    @Test
    fun `selects largest useful elapsed unit`() {
        assertEquals(
            ElapsedPeriod(5, ElapsedUnit.DAYS),
            elapsedPeriod(today.minusDays(5), today),
        )
        assertEquals(
            ElapsedPeriod(2, ElapsedUnit.WEEKS),
            elapsedPeriod(today.minusWeeks(2), today),
        )
        assertEquals(
            ElapsedPeriod(2, ElapsedUnit.MONTHS),
            elapsedPeriod(today.minusMonths(2), today),
        )
        assertEquals(
            ElapsedPeriod(2, ElapsedUnit.YEARS),
            elapsedPeriod(today.minusYears(2), today),
        )
    }
}
