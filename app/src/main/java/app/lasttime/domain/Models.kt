package app.lasttime.domain

import java.time.LocalDate

enum class RepeatUnit {
    DAYS,
    WEEKS,
    MONTHS,
    YEARS,
}

enum class EventCategory {
    CAR,
    HOME,
    HEALTH,
    SHOPPING,
    PLANTS,
    TRAVEL,
    SOCIAL,
    OTHER,
}

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
}

enum class EventStatus {
    OVERDUE,
    TODAY,
    SOON,
    UPCOMING,
}

data class RepeatInterval(
    val amount: Int,
    val unit: RepeatUnit,
) {
    init {
        require(amount in 1..999)
    }

    fun addTo(date: LocalDate): LocalDate =
        when (unit) {
            RepeatUnit.DAYS -> date.plusDays(amount.toLong())
            RepeatUnit.WEEKS -> date.plusWeeks(amount.toLong())
            RepeatUnit.MONTHS -> date.plusMonths(amount.toLong())
            RepeatUnit.YEARS -> date.plusYears(amount.toLong())
        }

    fun minimumDays(): Long =
        when (unit) {
            RepeatUnit.DAYS -> amount.toLong()
            RepeatUnit.WEEKS -> amount * 7L
            RepeatUnit.MONTHS -> amount * 28L
            RepeatUnit.YEARS -> amount * 365L
        }
}

data class Completion(
    val id: Long,
    val date: LocalDate,
)

data class TrackedEvent(
    val id: Long,
    val title: String,
    val category: EventCategory,
    val interval: RepeatInterval,
    val reminderDaysBefore: Int?,
    val note: String,
    val completions: List<Completion>,
) {
    val lastDate: LocalDate
        get() = completions.maxOf { it.date }

    val nextDate: LocalDate
        get() = interval.addTo(lastDate)

    fun status(today: LocalDate): EventStatus =
        when {
            nextDate.isBefore(today) -> EventStatus.OVERDUE
            nextDate == today -> EventStatus.TODAY
            !nextDate.isAfter(today.plusDays(7)) -> EventStatus.SOON
            else -> EventStatus.UPCOMING
        }
}

data class EventDraft(
    val title: String,
    val category: EventCategory,
    val initialDate: LocalDate,
    val interval: RepeatInterval,
    val reminderDaysBefore: Int?,
    val note: String,
)

enum class DraftValidationError {
    TITLE_REQUIRED,
    TITLE_TOO_LONG,
    NOTE_TOO_LONG,
    FUTURE_DATE,
    REMINDER_OUT_OF_RANGE,
    REMINDER_AFTER_INTERVAL,
}

fun validateDraft(
    draft: EventDraft,
    today: LocalDate,
): DraftValidationError? =
    when {
        draft.title.isBlank() -> DraftValidationError.TITLE_REQUIRED
        draft.title.trim().length > 80 -> DraftValidationError.TITLE_TOO_LONG
        draft.note.length > 500 -> DraftValidationError.NOTE_TOO_LONG
        draft.initialDate.isAfter(today) -> DraftValidationError.FUTURE_DATE
        draft.reminderDaysBefore != null && draft.reminderDaysBefore !in 0..365 ->
            DraftValidationError.REMINDER_OUT_OF_RANGE
        draft.reminderDaysBefore != null &&
            draft.reminderDaysBefore >= draft.interval.minimumDays() ->
            DraftValidationError.REMINDER_AFTER_INTERVAL
        else -> null
    }
