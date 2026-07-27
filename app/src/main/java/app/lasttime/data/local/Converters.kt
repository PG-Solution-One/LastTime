package app.lasttime.data.local

import androidx.room.TypeConverter
import app.lasttime.domain.EventCategory
import app.lasttime.domain.RepeatUnit
import java.time.Instant
import java.time.LocalDate

internal class Converters {
    @TypeConverter
    fun localDateToEpochDay(value: LocalDate): Long = value.toEpochDay()

    @TypeConverter
    fun epochDayToLocalDate(value: Long): LocalDate = LocalDate.ofEpochDay(value)

    @TypeConverter
    fun instantToEpochMillis(value: Instant): Long = value.toEpochMilli()

    @TypeConverter
    fun epochMillisToInstant(value: Long): Instant = Instant.ofEpochMilli(value)

    @TypeConverter
    fun categoryToString(value: EventCategory): String = value.name

    @TypeConverter
    fun stringToCategory(value: String): EventCategory = EventCategory.valueOf(value)

    @TypeConverter
    fun repeatUnitToString(value: RepeatUnit): String = value.name

    @TypeConverter
    fun stringToRepeatUnit(value: String): RepeatUnit = RepeatUnit.valueOf(value)
}
