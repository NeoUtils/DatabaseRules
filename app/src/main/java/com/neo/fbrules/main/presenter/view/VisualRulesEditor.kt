package com.neo.fbrules.main.presenter.view

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.TextView
import com.neo.fbrules.R
import org.json.JSONObject

class VisualRulesEditor(
    context: Context, attr: AttributeSet? = null
) : LinearLayout(context, attr) {

    private lateinit var rulesJson: JSONObject

    var errorListener: ((String) -> Unit)? = null

    fun setRules(rules: String) {
        runCatching {
            rulesJson = JSONObject(rules).getJSONObject("rules")
            render()
        }.onFailure {
            errorListener?.invoke(
                context.getString(
                    R.string.text_visual_rules_error_invalid_rules
                )
            )
        }
    }

    private fun render() {
        for (key in rulesJson.keys()) {
            val any = rulesJson.get(key)
            addView(TextView(context).apply { text = key })
        }
    }

}