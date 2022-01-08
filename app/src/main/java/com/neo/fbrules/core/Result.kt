package com.neo.fbrules.core

import androidx.annotation.StringRes
import com.neo.fbrules.R

sealed class Result<out R> {

    data class Success<T>(val data: T) : Result<T>()

    data class Error(
        val type: Constants.ERROR = Constants.ERROR.UNKNOWN_ERROR,
        val title: String = type.name,
        val message: String = type.message
    ) : Result<Nothing>()
}

class Message(
    @StringRes
    val title: Int = R.string.text_success,
    @StringRes
    val message: Int
)