package com.app.mindcycle.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.app.mindcycle.data.db.Converters
import org.threeten.bp.LocalDateTime
import java.util.*

@Entity(tableName = "mood_entries")
@TypeConverters(Converters::class)
data class MoodEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: LocalDateTime,
    val moodLevel: MoodLevel,
    val cyclePhase: CyclePhase,
    val note: String? = null,
    val symptoms: List<String> = emptyList(),
    val isPeriodStart: Boolean = false,
    val isPeriod: Boolean = false
) {
    companion object {
        fun create(
            date: LocalDateTime = LocalDateTime.now(),
            moodLevel: MoodLevel,
            cyclePhase: CyclePhase,
            note: String? = null,
            symptoms: List<String> = emptyList(),
            isPeriodStart: Boolean = false
        ): MoodEntry {
            return MoodEntry(
                date = date,
                moodLevel = moodLevel,
                cyclePhase = cyclePhase,
                note = note,
                symptoms = symptoms,
                isPeriodStart = isPeriodStart
            )
        }
    }
} 
