package com.app.mindcycle.data.model

import androidx.annotation.StringRes
import com.app.mindcycle.R

/**
 * Represents cycle tracking modes that affect forecasting and UI emphasis.
 */
enum class CycleMode(
    val id: String,
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
    val windowPercentRange: ClosedFloatingPointRange<Float>,
    val maxConfidence: Double = 1.0,
    val hideForecastOnToday: Boolean = false,
    val hideFertilityBlocks: Boolean = false,
    val enableContraceptionJournal: Boolean = false
) {
    STANDARD(
        id = "standard",
        titleRes = R.string.mode_standard,
        descriptionRes = R.string.mode_standard_desc,
        windowPercentRange = 0.15f..0.15f
    ),
    IRREGULAR(
        id = "irregular",
        titleRes = R.string.mode_irregular,
        descriptionRes = R.string.mode_irregular_desc,
        windowPercentRange = 0.25f..0.35f,
        maxConfidence = 0.5
    ),
    PERIMENOPAUSE(
        id = "perimenopause",
        titleRes = R.string.mode_perimenopause,
        descriptionRes = R.string.mode_perimenopause_desc,
        windowPercentRange = 0.20f..0.25f,
        hideForecastOnToday = true
    ),
    HORMONAL_CONTRACEPTION(
        id = "hc",
        titleRes = R.string.mode_hc,
        descriptionRes = R.string.mode_hc_desc,
        windowPercentRange = 0.15f..0.20f,
        hideFertilityBlocks = true,
        enableContraceptionJournal = true
    );

    companion object {
        fun fromId(id: String?): CycleMode = values().firstOrNull { it.id == id } ?: STANDARD
    }
}
