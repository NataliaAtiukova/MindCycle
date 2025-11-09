package com.app.mindcycle.data.model

import androidx.annotation.StringRes
import com.app.mindcycle.R

enum class ReminderType(val id: String, @StringRes val titleRes: Int, val deepLinkRoute: String) {
    DAY_LOG(
        id = "day_log",
        titleRes = R.string.reminder_day_log,
        deepLinkRoute = "today"
    ),
    SYMPTOM_LOG(
        id = "symptom_log",
        titleRes = R.string.reminder_symptom_log,
        deepLinkRoute = "today"
    ),
    CONTRACEPTION_INTAKE(
        id = "hc_intake",
        titleRes = R.string.reminder_hc,
        deepLinkRoute = "today"
    );

    companion object {
        fun fromId(id: String): ReminderType = values().firstOrNull { it.id == id } ?: DAY_LOG
    }
}
