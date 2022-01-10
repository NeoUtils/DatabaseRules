package com.neo.fbrules.main.presenter.components.view

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.TextView
import org.json.JSONObject

class VisualRulesEditor(
    context: Context, attr: AttributeSet? = null
) : LinearLayout(context, attr) {

    private lateinit var rulesJson: JSONObject

    var errorListener: ((ERROR, Throwable?) -> Unit)? = null

    init {
        orientation = VERTICAL
    }

    fun setRules(rules: String) {
        runCatching {
            rulesJson = JSONObject(rules).getJSONObject("rules")
            render()
        }.onFailure {
            errorListener?.invoke(
                ERROR.invalid_rules,
                it
            )
        }
    }

    private fun render() {

        for (key in rulesJson.keys()) {

            val any = rulesJson.get(key)

            when (any) {
                //rule
                is JSONObject -> {

                }
                //condition
                is String -> {

                }

                //literal value
                is Boolean -> {

                }

                else -> {
                    errorListener?.invoke(
                        ERROR.unrecognized_rule,
                        IllegalArgumentException("$any unrecognized")
                    )
                }
            }

            addView(TextView(context).apply { text = "$key=$any" })
        }
    }

    enum class ERROR {
        unrecognized_rule,
        invalid_rules
    }
}