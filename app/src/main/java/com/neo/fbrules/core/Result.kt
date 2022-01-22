package com.neo.fbrules.core

import android.content.Context
import androidx.annotation.StringRes
import com.neo.fbrules.R

sealed class Result<out R> {

    data class Success<T>(val data: T) : Result<T>()

    data class Error(
        val type: Constants.ERROR = Constants.ERROR.UNKNOWN_ERROR,
        val title: String = type.name,
        val message: String? = null
    ) : Result<Nothing>() {
        fun getSafeMessage(context: Context): String {
            return message ?: context.getString(R.string.unknown_error)
        }
    }
}

class Message(
    @StringRes
    val title: Int = R.string.text_success,
    @StringRes
    val message: Int
)