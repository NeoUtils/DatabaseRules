package com.neo.fbrules.main.presenter.components

import com.neo.fbrules.main.presenter.model.RuleModel
import org.json.JSONObject
import java.lang.IllegalArgumentException

class ReadRulesJson {

    private val rules = mutableListOf<RuleModel>()

    fun getRules(rulesJson: JSONObject): List<RuleModel> {

        rules.clear()

        mapRules(
            RuleModel("rules", rulesJson.getJSONObject("rules")),
            rules
        )

        return rules

    }

    private fun mapRules(rule: RuleModel, rules: MutableList<RuleModel>) {

        rules.add(rule)

        for (key in rule.jsonObject.keys()) {

            when (val value = rule.jsonObject.get(key)) {

                //path
                is JSONObject -> {
                    mapRules(
                        RuleModel(rule.path + "/$key", value),
                        rules
                    )
                }

                //condition
                is String, is Boolean -> {
                    rule.condition.add(key to "$value")
                }

                else -> {
                    throw IllegalArgumentException("$value not recognized as JSON")
                }
            }
        }
    }
}
