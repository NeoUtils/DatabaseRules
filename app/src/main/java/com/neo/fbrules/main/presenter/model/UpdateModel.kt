package com.neo.fbrules.main.presenter.model

data class UpdateModel(
    val hasUpdate : Boolean? = null,
    val lastVersionCode : Int? = null,
    val lastVersionName : String? = null,
    val downloadLink : String? = null,
    val force : Boolean = false
)