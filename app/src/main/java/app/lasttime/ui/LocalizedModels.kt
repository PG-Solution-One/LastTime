package app.lasttime.ui

import androidx.annotation.DrawableRes
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import app.lasttime.R
import app.lasttime.domain.DraftValidationError
import app.lasttime.domain.EventCategory
import app.lasttime.domain.RepeatUnit

@StringRes
private fun EventCategory.titleResource(): Int =
    when (this) {
        EventCategory.CAR -> R.string.category_car
        EventCategory.HOME -> R.string.category_home
        EventCategory.HEALTH -> R.string.category_health
        EventCategory.SHOPPING -> R.string.category_shopping
        EventCategory.PLANTS -> R.string.category_plants
        EventCategory.TRAVEL -> R.string.category_travel
        EventCategory.SOCIAL -> R.string.category_social
        EventCategory.OTHER -> R.string.category_other
    }

@Composable
fun EventCategory.localizedTitle(): String = stringResource(titleResource())

@DrawableRes
fun EventCategory.iconResource(): Int =
    when (this) {
        EventCategory.CAR -> R.drawable.ms_directions_car
        EventCategory.HOME -> R.drawable.ms_home
        EventCategory.HEALTH -> R.drawable.ms_favorite
        EventCategory.SHOPPING -> R.drawable.ms_shopping_bag
        EventCategory.PLANTS -> R.drawable.ms_local_florist
        EventCategory.TRAVEL -> R.drawable.ms_flight
        EventCategory.SOCIAL -> R.drawable.ms_group
        EventCategory.OTHER -> R.drawable.ms_more_horiz
    }

@StringRes
private fun RepeatUnit.titleResource(): Int =
    when (this) {
        RepeatUnit.DAYS -> R.string.unit_days
        RepeatUnit.WEEKS -> R.string.unit_weeks
        RepeatUnit.MONTHS -> R.string.unit_months
        RepeatUnit.YEARS -> R.string.unit_years
    }

@PluralsRes
private fun RepeatUnit.intervalResource(): Int =
    when (this) {
        RepeatUnit.DAYS -> R.plurals.interval_days
        RepeatUnit.WEEKS -> R.plurals.interval_weeks
        RepeatUnit.MONTHS -> R.plurals.interval_months
        RepeatUnit.YEARS -> R.plurals.interval_years
    }

@Composable
fun RepeatUnit.localizedTitle(): String = stringResource(titleResource())

@Composable
fun RepeatUnit.localizedInterval(amount: Int): String = pluralStringResource(intervalResource(), amount, amount)

@StringRes
fun DraftValidationError.messageResource(): Int =
    when (this) {
        DraftValidationError.TITLE_REQUIRED -> R.string.validation_title_required
        DraftValidationError.TITLE_TOO_LONG -> R.string.validation_title_too_long
        DraftValidationError.NOTE_TOO_LONG -> R.string.validation_note_too_long
        DraftValidationError.FUTURE_DATE -> R.string.validation_future_date
        DraftValidationError.REMINDER_OUT_OF_RANGE -> R.string.validation_reminder_range
        DraftValidationError.REMINDER_AFTER_INTERVAL -> R.string.validation_reminder_interval
    }
