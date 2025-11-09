package com.app.mindcycle.data.model

import androidx.annotation.StringRes
import com.app.mindcycle.R

enum class ContraceptionMethod(@StringRes val labelRes: Int) {
    PILL(R.string.method_pill),
    PATCH(R.string.method_patch),
    RING(R.string.method_ring);

    companion object {
        fun fromNameOrNull(name: String?): ContraceptionMethod? = values().firstOrNull { it.name == name }
    }
}
