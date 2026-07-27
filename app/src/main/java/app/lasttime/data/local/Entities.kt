package app.lasttime.data.local

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import app.lasttime.domain.EventCategory
import app.lasttime.domain.RepeatUnit
import java.time.Instant
import java.time.LocalDate

@Entity(tableName = "events")
internal data class EventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: EventCategory,
    val repeatAmount: Int,
    val repeatUnit: RepeatUnit,
    val reminderDaysBefore: Int?,
    val note: String,
    val createdAt: Instant,
    val updatedAt: Instant,
)

@Entity(
    tableName = "completions",
    foreignKeys = [
        ForeignKey(
            entity = EventEntity::class,
            parentColumns = ["id"],
            childColumns = ["eventId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("eventId"),
        Index(value = ["eventId", "completedDate"], unique = true),
    ],
)
internal data class CompletionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val eventId: Long,
    val completedDate: LocalDate,
    val createdAt: Instant,
)

internal data class EventWithCompletions(
    @Embedded val event: EventEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "eventId",
    )
    val completions: List<CompletionEntity>,
)
