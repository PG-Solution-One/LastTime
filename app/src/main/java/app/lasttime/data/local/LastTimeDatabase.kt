package app.lasttime.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [EventEntity::class, CompletionEntity::class],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
internal abstract class LastTimeDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao

    companion object {
        fun create(context: Context): LastTimeDatabase =
            Room
                .databaseBuilder(
                    context.applicationContext,
                    LastTimeDatabase::class.java,
                    "last-time.db",
                ).build()
    }
}
