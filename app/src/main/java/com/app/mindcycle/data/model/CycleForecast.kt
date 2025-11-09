package com.app.mindcycle.data.model

import org.threeten.bp.LocalDate

/**
 * Represents an offline forecast derived from recent period starts.
 */
data class CycleForecast(
    val predictedStartDate: LocalDate?,
    val windowStart: LocalDate?,
    val windowEnd: LocalDate?,
    val medianCycleLengthDays: Double,
    val sampleSize: Int,
    val confidenceScore: Double,
    val confidenceLevel: ConfidenceLevel,
    val mode: CycleMode,
    val insufficientData: Boolean = false,
    val insufficientReason: ForecastInsufficientReason? = null
)
