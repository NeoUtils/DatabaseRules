package com.neo.fbrules.main.presenter.model

import org.json.JSONObject

data class RuleModel(
    val path: String,
    val jsonObject: JSONObject
) {
    val condition = mutableListOf<Pair<String, String>>()
}