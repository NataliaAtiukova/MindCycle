package com.app.mindcycle.data.model

import androidx.annotation.StringRes
import com.app.mindcycle.R

enum class ConfidenceLevel(@StringRes val labelRes: Int) {
    LOW(R.string.confidence_low),
    MEDIUM(R.string.confidence_medium),
    HIGH(R.string.confidence_high);

    companion object {
        fun fromScore(score: Double): ConfidenceLevel = when {
            score < 0.35 -> LOW
            score < 0.7 -> MEDIUM
            else -> HIGH
        }
    }
}
