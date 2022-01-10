package com.neo.fbrules.main.presenter.fragment.content

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.neo.fbrules.R
import com.neo.fbrules.databinding.ContentVisualRulesEditorBinding
import com.neo.fbrules.main.presenter.components.ReadRulesJson
import com.neo.fbrules.main.presenter.components.view.VisualRulesAdapter
import com.neo.fbrules.main.presenter.contract.RulesEditor
import com.neo.fbrules.util.showAlertDialog
import org.json.JSONObject
import java.util.regex.Pattern

private typealias VisualEditorView = ContentVisualRulesEditorBinding

class VisualEditorFragment : Fragment(), RulesEditor {

    private lateinit var binding: VisualEditorView
    private val visualRulesAdapter: VisualRulesAdapter by setupVisualRulesAdapter()
    private lateinit var rulesJson: JSONObject

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = VisualEditorView.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupView()
    }

    private fun setupView() {
        binding.rvRules.adapter = visualRulesAdapter
    }

    private fun readRulesJson() = runCatching {
        val rules = ReadRulesJson().getRules(rulesJson)
        visualRulesAdapter.setRules(rules)
    }.onFailure {
        handlerError(
            ERROR.UNRECOGNIZED_RULES, it
        )
    }

    override fun getRules(): String {
        return rulesJson.toString(4)
    }

    override fun setRules(rules: String) {
        runCatching {

            if (rules.isEmpty()) {
                return@runCatching
            }

            rulesJson = runCatching {
                JSONObject(rules)
            }.getOrElse {
                tryFix(rules)
            }

            if (!rulesJson.has("rules")) {
                handlerError(
                    ERROR.INVALID_RULES,
                    IllegalArgumentException("rules key not found")
                )
                return@runCatching
            }

            readRulesJson()
        }.onFailure {

            handlerError(
                ERROR.INVALID_JSON,
                it
            )
        }
    }

    private fun tryFix(rules: String): JSONObject {
        val fix0 = rules.replace(Regex(",\\s*\\}\\s*,"), "},")
        val fix1 = fix0.replace(Regex("\\}\\s*,\\s*\\}"), "}}")
        return JSONObject(fix1)
    }

    private fun setupVisualRulesAdapter() = lazy {
        VisualRulesAdapter()
    }

    private fun handlerError(type: ERROR, throwable: Throwable? = null) {

        @StringRes
        val errorMessage = when (type) {

            ERROR.INVALID_RULES -> {
                R.string.text_visual_rules_editor_error_invalid_rules
            }

            ERROR.UNRECOGNIZED_RULES -> {
                R.string.text_visual_rules_editor_error_unrecognized_rule
            }

            ERROR.INVALID_JSON -> {
                R.string.text_visual_rules_editor_error_invalid_json
            }
        }

        showAlertDialog("Error", getString(errorMessage))

        if (throwable != null) {
            Firebase.crashlytics.recordException(throwable)
        }
    }

    enum class ERROR {
        UNRECOGNIZED_RULES,
        INVALID_RULES,
        INVALID_JSON
    }
}
