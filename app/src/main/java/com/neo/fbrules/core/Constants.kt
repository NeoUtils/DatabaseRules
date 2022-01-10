package com.neo.fbrules.core

import androidx.annotation.StringRes
import com.neo.fbrules.R

object Constants {
    const val FIREBASE_RULES = ".settings/rules.json"

    enum class ERROR(@StringRes val message: Int) {
        CREDENTIAL_NOT_FOUND(R.string.credentials_not_found),
        UNKNOWN_ERROR(R.string.unknown_error),
    }
}