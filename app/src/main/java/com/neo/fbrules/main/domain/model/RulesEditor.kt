package com.neo.fbrules.main.domain.model

interface RulesEditor {
    fun getRules(): String
    fun setRules(rules: String)

}
