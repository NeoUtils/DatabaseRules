package com.neo.fbrules.main.presenter.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.io.Serializable

@Parcelize
data class RuleModel(
    var rootPath: String = "",
    var conditions: MutableList<RuleCondition> = mutableListOf()
) : Parcelable, Serializable {
    val relativePath get() = rootPath.substringAfter("rules/")

    val parentPath get() = if (rootPath.contains("/"))
        rootPath.substringBeforeLast("/") else ""

    val actualPath get() = rootPath.substringAfterLast("/")
}

@Parcelize
data class RuleCondition(
    val property: String,
    var condition: String
) : Parcelable, Serializable