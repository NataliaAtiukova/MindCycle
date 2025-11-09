package com.app.mindcycle.data.forecast

import com.app.mindcycle.data.model.CycleForecast
import com.app.mindcycle.data.model.CycleMode
import com.app.mindcycle.data.model.ForecastInsufficientReason
import com.app.mindcycle.data.model.MoodEntry
import com.app.mindcycle.data.model.ConfidenceLevel
import org.threeten.bp.temporal.ChronoUnit
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToLong

class CycleForecastCalculator {

    fun forecast(periodStarts: List<MoodEntry>, mode: CycleMode): CycleForecast {
        val starts = periodStarts
            .asSequence()
            .filter { it.isPeriodStart }
            .sortedByDescending { it.date }
            .take(12)
            .sortedBy { it.date }
            .toList()

        if (starts.size < 3) {
            return CycleForecast(
                predictedStartDate = null,
                windowStart = null,
                windowEnd = null,
                medianCycleLengthDays = 0.0,
                sampleSize = starts.size,
                confidenceScore = 0.0,
                confidenceLevel = ConfidenceLevel.LOW,
                mode = mode,
                insufficientData = true,
                insufficientReason = ForecastInsufficientReason.NOT_ENOUGH_STARTS
            )
        }

        val intervals = starts
            .zipWithNext { previous, next ->
                ChronoUnit.DAYS.between(previous.date, next.date).toDouble()
            }
            .filter { it > 0 }

        if (intervals.isEmpty()) {
            return CycleForecast(
                predictedStartDate = null,
                windowStart = null,
                windowEnd = null,
                medianCycleLengthDays = 0.0,
                sampleSize = starts.size,
                confidenceScore = 0.0,
                confidenceLevel = ConfidenceLevel.LOW,
                mode = mode,
                insufficientData = true,
                insufficientReason = ForecastInsufficientReason.NO_STABLE_INTERVALS
            )
        }

        val filteredIntervals = filterOutliers(intervals)
        val median = median(filteredIntervals)
        val lastStartDate = starts.last().date.toLocalDate()
        val predictedStartDate = lastStartDate.plusDays(max(1L, median.roundToLong()))

        val iqr = calculateIqr(filteredIntervals)
        val normalizedVariance = if (median == 0.0) 1.0 else (iqr / median).coerceIn(0.0, 1.0)
        val sizeFactor = (filteredIntervals.size / 10.0).coerceIn(0.0, 1.0)
        val baseConfidence = ((1 - normalizedVariance) * 0.7 + sizeFactor * 0.3).coerceIn(0.0, 1.0)
        val confidenceScore = min(baseConfidence, mode.maxConfidence)
        val confidenceLevel = ConfidenceLevel.fromScore(confidenceScore)

        val variabilityFactor = normalizedVariance.coerceIn(0.0, 1.0)
        val range = mode.windowPercentRange
        val percent = if (range.start == range.endInclusive) {
            range.start.toDouble()
        } else {
            (range.start + (range.endInclusive - range.start) * variabilityFactor).toDouble()
        }
        val deltaDays = max(1L, (median * percent).roundToLong())
        val windowStart = predictedStartDate.minusDays(deltaDays)
        val windowEnd = predictedStartDate.plusDays(deltaDays)

        return CycleForecast(
            predictedStartDate = predictedStartDate,
            windowStart = windowStart,
            windowEnd = windowEnd,
            medianCycleLengthDays = median,
            sampleSize = filteredIntervals.size,
            confidenceScore = confidenceScore,
            confidenceLevel = confidenceLevel,
            mode = mode,
            insufficientData = false,
            insufficientReason = null
        )
    }

    private fun filterOutliers(values: List<Double>): List<Double> {
        if (values.size < 4) return values
        val sorted = values.sorted()
        val q1 = percentile(sorted, 0.25)
        val q3 = percentile(sorted, 0.75)
        val iqr = q3 - q1
        if (iqr == 0.0) return sorted
        val lower = q1 - 2 * iqr
        val upper = q3 + 2 * iqr
        val filtered = sorted.filter { it in lower..upper }
        return if (filtered.isEmpty()) sorted else filtered
    }

    private fun median(values: List<Double>): Double {
        if (values.isEmpty()) return 0.0
        val sorted = values.sorted()
        val middle = sorted.size / 2
        return if (sorted.size % 2 == 0) {
            (sorted[middle - 1] + sorted[middle]) / 2.0
        } else {
            sorted[middle]
        }
    }

    private fun calculateIqr(values: List<Double>): Double {
        if (values.size < 4) {
            val minValue = values.minOrNull() ?: return 0.0
            val maxValue = values.maxOrNull() ?: return 0.0
            return abs(maxValue - minValue)
        }
        val sorted = values.sorted()
        val q1 = percentile(sorted, 0.25)
        val q3 = percentile(sorted, 0.75)
        return q3 - q1
    }

    private fun percentile(sortedValues: List<Double>, percentile: Double): Double {
        if (sortedValues.isEmpty()) return 0.0
        val clamped = percentile.coerceIn(0.0, 1.0)
        val rank = clamped * (sortedValues.size - 1)
        val lowerIndex = floor(rank).toInt()
        val upperIndex = ceil(rank).toInt()
        if (lowerIndex == upperIndex) return sortedValues[lowerIndex]
        val fraction = rank - floor(rank)
        return sortedValues[lowerIndex] + fraction * (sortedValues[upperIndex] - sortedValues[lowerIndex])
    }
}
