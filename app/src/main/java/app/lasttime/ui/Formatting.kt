package app.lasttime.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.intl.Locale
import app.lasttime.R
import app.lasttime.domain.EventStatus
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit
import java.util.Locale as JavaLocale

internal enum class ElapsedUnit {
    TODAY,
    YESTERDAY,
    DAYS,
    WEEKS,
    MONTHS,
    YEARS,
}

internal data class ElapsedPeriod(
    val value: Int,
    val unit: ElapsedUnit,
)

internal fun elapsedPeriod(
    from: LocalDate,
    to: LocalDate,
): ElapsedPeriod {
    val days = ChronoUnit.DAYS.between(from, to).coerceAtLeast(0)
    if (days == 0L) return ElapsedPeriod(0, ElapsedUnit.TODAY)
    if (days == 1L) return ElapsedPeriod(1, ElapsedUnit.YESTERDAY)

    val years = ChronoUnit.YEARS.between(from, to)
    if (years > 0) return ElapsedPeriod(years.toInt(), ElapsedUnit.YEARS)

    val months = ChronoUnit.MONTHS.between(from, to)
    if (months > 0) return ElapsedPeriod(months.toInt(), ElapsedUnit.MONTHS)

    val weeks = days / 7
    if (weeks > 0) return ElapsedPeriod(weeks.toInt(), ElapsedUnit.WEEKS)

    return ElapsedPeriod(days.toInt(), ElapsedUnit.DAYS)
}

@Composable
fun formatDate(date: LocalDate): String {
    val locale = JavaLocale.forLanguageTag(Locale.current.toLanguageTag())
    return date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(locale))
}

@Composable
fun formatElapsed(
    from: LocalDate,
    to: LocalDate,
): String {
    val period = elapsedPeriod(from, to)
    return when (period.unit) {
        ElapsedUnit.TODAY -> stringResource(R.string.elapsed_today)
        ElapsedUnit.YESTERDAY -> stringResource(R.string.elapsed_yesterday)
        ElapsedUnit.DAYS ->
            pluralStringResource(
                R.plurals.elapsed_days,
                period.value,
                period.value,
            )
        ElapsedUnit.WEEKS ->
            pluralStringResource(
                R.plurals.elapsed_weeks,
                period.value,
                period.value,
            )
        ElapsedUnit.MONTHS ->
            pluralStringResource(
                R.plurals.elapsed_months,
                period.value,
                period.value,
            )
        ElapsedUnit.YEARS ->
            pluralStringResource(
                R.plurals.elapsed_years,
                period.value,
                period.value,
            )
    }
}

@Composable
fun statusText(
    status: EventStatus,
    dueDate: LocalDate,
    today: LocalDate,
): String =
    when (status) {
        EventStatus.OVERDUE -> {
            val days = ChronoUnit.DAYS.between(dueDate, today).toInt()
            pluralStringResource(R.plurals.status_overdue_days, days, days)
        }
        EventStatus.TODAY -> stringResource(R.string.status_repeat_today)
        EventStatus.SOON -> stringResource(R.string.status_repeat_date, formatDate(dueDate))
        EventStatus.UPCOMING -> stringResource(R.string.status_next_date, formatDate(dueDate))
    }
