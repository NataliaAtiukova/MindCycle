package com.app.mindcycle.data.db

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.app.mindcycle.data.model.ContraceptionLogEntry
import com.app.mindcycle.data.model.ContraceptionMethod
import com.app.mindcycle.data.model.MoodEntry
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime

@Dao
interface MoodEntryDao {
    @Query("SELECT * FROM mood_entries WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    suspend fun getEntriesBetweenDates(startDate: LocalDateTime, endDate: LocalDateTime): List<MoodEntry>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: MoodEntry)

    @Delete
    suspend fun deleteEntry(entry: MoodEntry)

    @Query("SELECT * FROM mood_entries WHERE isPeriodStart = 1 ORDER BY date DESC")
    suspend fun getPeriodStarts(): List<MoodEntry>

    @Query("SELECT * FROM mood_entries ORDER BY date DESC LIMIT 1")
    suspend fun getLastEntry(): MoodEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertContraceptionEntry(entry: ContraceptionLogEntry)

    @Query("SELECT * FROM contraception_log WHERE scheduledDate = :date AND method = :method LIMIT 1")
    suspend fun getContraceptionEntry(date: LocalDate, method: ContraceptionMethod): ContraceptionLogEntry?

    @Query("SELECT * FROM contraception_log ORDER BY scheduledDate DESC LIMIT :limit")
    suspend fun getRecentContraceptionEntries(limit: Int = 14): List<ContraceptionLogEntry>

}

@Database(
    entities = [MoodEntry::class, ContraceptionLogEntry::class],
    version = 3,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class MoodDatabase : RoomDatabase() {
    abstract fun moodEntryDao(): MoodEntryDao

    companion object {
        @Volatile
        private var INSTANCE: MoodDatabase? = null

        fun buildDatabase(context: Context): MoodDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MoodDatabase::class.java,
                    "mind_cycle_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
} 
