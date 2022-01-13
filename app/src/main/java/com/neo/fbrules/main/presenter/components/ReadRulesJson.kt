package com.neo.fbrules.main.presenter.components

import com.neo.fbrules.main.presenter.model.RuleCondition
import com.neo.fbrules.main.presenter.model.RuleModel
import org.json.JSONObject
import java.lang.IllegalArgumentException

class ReadRulesJson {

    private val rules = mutableListOf<RuleModel>()
    lateinit var jsonObject : JSONObject

    fun getRules(rulesJson: JSONObject): List<RuleModel> {
        jsonObject = rulesJson.getJSONObject("rules")

        rules.clear()

        mapRules(
            RuleModel("rules"),
            rules
        )

        return rules

    }

    private fun mapRules(rule: RuleModel, rules: MutableList<RuleModel>) {

        rules.add(rule)

        for (key in jsonObject.keys()) {

            when (val value = jsonObject.get(key)) {

                //path
                is JSONObject -> {

                    this.jsonObject = value

                    mapRules(
                        RuleModel(rule.path + "/$key"),
                        rules
                    )
                }

                //condition
                is String -> {
                    rule.conditions.add(RuleCondition(key, value))
                }

                //condition
                 is Boolean -> {
                    rule.conditions.add(RuleCondition(key, value.toString()))
                }

                else -> {
                    throw IllegalArgumentException("$value not recognized as JSON")
                }
            }
        }
    }
}
