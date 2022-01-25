package com.neo.fbrules.main.presenter.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.io.Serializable

@Parcelize
data class PathModel(
    var rootPath: String = "",
    var rules: MutableList<RuleModel> = mutableListOf()
) : Parcelable, Serializable {
    val relativePath get() = rootPath.substringAfter("rules/")

    val parentPath
        get() = if (rootPath.contains("/"))
            rootPath.substringBeforeLast("/") else ""

    val actualPath get() = rootPath.substringAfterLast("/")

    var showCode = false
}

@Parcelize
data class RuleModel(
    val property: String,
    var condition: String
) : Parcelable, Serializable {
    override fun toString(): String {
        return "$property:$condition"
    }
}