package com.app.mindcycle.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.app.mindcycle.data.db.Converters
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime

@Entity(tableName = "contraception_log")
@TypeConverters(Converters::class)
data class ContraceptionLogEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val method: ContraceptionMethod = ContraceptionMethod.PILL,
    val scheduledDate: LocalDate,
    val takenAt: LocalDateTime? = null,
    val isSkipped: Boolean = false,
    val note: String? = null
)
