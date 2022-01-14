package com.neo.fbrules.main.presenter.components

import com.neo.fbrules.main.presenter.model.RuleCondition
import com.neo.fbrules.main.presenter.model.RuleModel
import com.neo.fbrules.util.isBoolean
import org.json.JSONObject
import java.lang.IllegalArgumentException

class ReadRulesJson {

    private val rules = mutableListOf<RuleModel>()

    fun getRulesModel(rulesJson: JSONObject): MutableList<RuleModel> {

        rules.clear()

        mapRules(
            RuleModel("rules"),
            rules,
            rulesJson.getJSONObject("rules")
        )

        return rules

    }

    private fun mapRules(rule: RuleModel, rules: MutableList<RuleModel>, jsonObject: JSONObject) {

        rules.add(rule)

        for (key in jsonObject.keys()) {

            when (val value = jsonObject.get(key)) {

                //path
                is JSONObject -> {

                    mapRules(
                        RuleModel(rule.path + "/$key"),
                        rules,
                        value
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

    fun getRulesString(rules: MutableList<RuleModel>): String {
        val result = JSONObject()

        for (rule in rules) {
            val jsonPath = getJsonPath(result, rule.path)
            jsonPath.apply {
                rule.conditions.forEach {
                    if (it.condition.isBoolean()) {
                        jsonPath.put(it.property, it.condition.toBoolean())
                    } else {
                        jsonPath.put(it.property, it.condition)
                    }
                }
            }
        }

        return result.toString(4)
    }

    private fun getJsonPath(origin: JSONObject, path: String) : JSONObject {
        val pathList = path.split("/")

        var result = origin

        for (it in pathList) {

            result = if (result.has(it)) {
                result.getJSONObject(it)
            } else {
                val temp = JSONObject()
                result.put(it, temp)
                temp
            }
        }

        return result
    }
}
