package app.lasttime.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
internal interface EventDao {
    @Transaction
    @Query("SELECT * FROM events ORDER BY title COLLATE NOCASE")
    fun observeAll(): Flow<List<EventWithCompletions>>

    @Transaction
    @Query("SELECT * FROM events WHERE id = :eventId")
    fun observeById(eventId: Long): Flow<EventWithCompletions?>

    @Transaction
    @Query("SELECT * FROM events WHERE id = :eventId")
    suspend fun getById(eventId: Long): EventWithCompletions?

    @Transaction
    @Query("SELECT * FROM events")
    suspend fun getAll(): List<EventWithCompletions>

    @Insert
    suspend fun insertEvent(event: EventEntity): Long

    @Update
    suspend fun updateEvent(event: EventEntity)

    @Delete
    suspend fun deleteEvent(event: EventEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertCompletion(completion: CompletionEntity): Long

    @Update
    suspend fun updateCompletion(completion: CompletionEntity)

    @Delete
    suspend fun deleteCompletion(completion: CompletionEntity)

    @Query("SELECT * FROM completions WHERE id = :completionId")
    suspend fun getCompletion(completionId: Long): CompletionEntity?

    @Query("SELECT COUNT(*) FROM completions WHERE eventId = :eventId")
    suspend fun completionCount(eventId: Long): Int
}
