package com.neo.fbrules.main.presenter.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.io.Serializable

@Parcelize
data class RuleModel(
    var path: String = "",
    val conditions: MutableList<RuleCondition> = mutableListOf()
) : Parcelable, Serializable

@Parcelize
data class RuleCondition(
    val property: String,
    var condition: String
) : Parcelable, Serializable