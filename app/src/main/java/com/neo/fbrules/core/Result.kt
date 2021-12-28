package com.neo.fbrules.core

sealed class Result<out R> {

    data class Success<T>(val data: T) : Result<T>()

    data class Error(
        val type: Constants.ERROR = Constants.ERROR.UNKNOWN_ERROR,
        val title: String = type.name,
        val message: String = type.message
    ) : Result<Nothing>()
}

class Message(
    val title: String = "Message",
    val message: String
)