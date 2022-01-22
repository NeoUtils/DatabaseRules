package com.neo.fbrules.main.presenter.components

import com.neo.fbrules.main.presenter.model.RuleModel
import com.neo.fbrules.main.presenter.model.PathModel
import com.neo.fbrules.util.isBoolean
import org.json.JSONObject
import java.lang.IllegalArgumentException

class ReadRulesJson {

    private val rules = mutableListOf<PathModel>()

    fun getRulesModel(rulesJson: JSONObject): MutableList<PathModel> {

        rules.clear()

        if (!rulesJson.has("rules")) {
            throw IllegalArgumentException("rules not found")
        }

        mapRules(
            PathModel("rules"),
            rules,
            rulesJson.getJSONObject("rules")
        )

        return rules

    }

    private fun mapRules(path: PathModel, paths: MutableList<PathModel>, jsonObject: JSONObject) {

        paths.add(path)

        for (key in jsonObject.keys()) {

            when (val value = jsonObject.get(key)) {

                //path
                is JSONObject -> {

                    mapRules(
                        PathModel(path.rootPath + "/$key"),
                        paths,
                        value
                    )
                }

                //condition
                is String -> {
                    path.rules.add(RuleModel(key, value))
                }

                //condition
                is Boolean -> {
                    path.rules.add(RuleModel(key, value.toString()))
                }

                else -> {
                    throw IllegalArgumentException("$value not recognized as JSON")
                }
            }
        }
    }

    fun getRulesString(paths: MutableList<PathModel>): String {
        val result = JSONObject()

        for (rule in paths) {
            val jsonPath = getJsonPath(result, rule.rootPath)
            jsonPath.apply {
                rule.rules.forEach {
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
