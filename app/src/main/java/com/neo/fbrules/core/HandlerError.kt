package com.neo.fbrules.core

import android.content.Context
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.neo.fbrules.R
import com.neo.fbrules.util.showAlertDialog


fun Fragment.handlerError(type: ERROR, throwable: Throwable? = null) =
    requireContext().handlerError(type, throwable)

fun Context.handlerError(type: ERROR, throwable: Throwable? = null) {

    @StringRes
    val errorMessage = when (type) {

        ERROR.INVALID_RULES -> {
            R.string.text_visual_rules_editor_error_invalid_rules
        }

        ERROR.UNRECOGNIZED_RULES -> {
            R.string.text_visual_rules_editor_error_unrecognized_rule
        }

        ERROR.INVALID_JSON -> {
            R.string.text_visual_rules_editor_error_invalid_json
        }
    }

    showAlertDialog(this, "Error", getString(errorMessage)) {
        if (throwable != null) {
            negativeButton("log") {
                showAlertDialog(
                    this@handlerError,
                    "Error",
                    getString(errorMessage) + "\n\n" + throwable.message
                )
            }
        }
    }

    if (throwable != null) {
        Firebase.crashlytics.recordException(throwable)
    }
}

enum class ERROR {
    UNRECOGNIZED_RULES,
    INVALID_RULES,
    INVALID_JSON
}
