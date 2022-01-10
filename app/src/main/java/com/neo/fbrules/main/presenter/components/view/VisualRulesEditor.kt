package com.neo.fbrules.main.presenter.components.view

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import com.neo.fbrules.main.presenter.components.ReadRulesJson
import org.json.JSONObject

class VisualRulesEditor(
    context: Context, attr: AttributeSet? = null
) : LinearLayout(context, attr) {

    private lateinit var rulesJson: JSONObject

    var errorListener: ((ERROR, Throwable?) -> Unit)? = null

    fun setRules(rules: String) = runCatching {
        rulesJson = JSONObject(rules)

        if (!rulesJson.has("rules")) {
            errorListener?.invoke(
                ERROR.INVALID_RULES,
                IllegalArgumentException("rules key not found")
            )
        }

        readRulesJson()
    }.onFailure {
        errorListener?.invoke(
            ERROR.INVALID_JSON,
            it
        )
    }

    fun getRules(): String {
        return rulesJson.toString(4)
    }

    private fun readRulesJson() = runCatching {
        ReadRulesJson().getRules(rulesJson)
    }.onFailure {
        errorListener?.invoke(
            ERROR.UNRECOGNIZED_RULES, it
        )
    }

    enum class ERROR {
        UNRECOGNIZED_RULES,
        INVALID_RULES,
        INVALID_JSON
    }
}