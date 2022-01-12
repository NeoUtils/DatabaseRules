package com.neo.fbrules.main.presenter.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.neo.fbrules.ERROR
import com.neo.fbrules.R
import com.neo.fbrules.databinding.FragmentVisualRulesEditorBinding
import com.neo.fbrules.handlerError
import com.neo.fbrules.main.presenter.components.ReadRulesJson
import com.neo.fbrules.main.presenter.components.view.RulesPathAdapter
import com.neo.fbrules.main.presenter.contract.RulesEditor
import com.neo.fbrules.util.showAlertDialog
import org.json.JSONObject

private typealias VisualEditorView = FragmentVisualRulesEditorBinding

class VisualEditorFragment : Fragment(), RulesEditor {

    private lateinit var binding: VisualEditorView
    private val rulesPathAdapter: RulesPathAdapter by setupVisualRulesAdapter()
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

        arguments?.getString("rules", null)?.let { setRules(it) }
    }

    private fun setupView() {
        binding.rvRules.adapter = rulesPathAdapter
    }

    private fun readRulesJson() = runCatching {
        val rules = ReadRulesJson().getRules(rulesJson)
        rulesPathAdapter.setRules(rules)
    }.onFailure {
        handlerError(
            ERROR.UNRECOGNIZED_RULES, it
        )
    }

    override fun getRules(): String {
        return rulesJson.toString(4)
    }

    override fun setRules(rules: String) {
        var tryFix = false

        runCatching {

            if (rules.isEmpty()) {
                return@runCatching
            }

            rulesJson = runCatching {
                JSONObject(rules)
            }.getOrElse {
                tryFix = true
                tryFixRules(rules)
            }

            if (!rulesJson.has("rules")) {
                handlerError(
                    ERROR.INVALID_RULES,
                    IllegalArgumentException("rules not found")
                )
                return@runCatching
            }

            if (tryFix) {
                showFixedRulesAlert()
            }

            readRulesJson()
        }.onFailure {
            handlerError(
                ERROR.INVALID_JSON,
                it
            )
        }
    }

    private fun showFixedRulesAlert() {
        showAlertDialog(
            getString(R.string.text_visualEditor_fixedRules_rulesFiexTitle),
            getString(R.string.text_visualEditor_fixedRules_rulesFixedMessage)
        ) {
            positiveButton()
        }
    }

    private fun tryFixRules(rules: String): JSONObject {
        val detectCommaAtEndRegex = Regex(",(?=[^{}\\w]*\\})")
        val fixedCommaAtEnd = rules.replace(detectCommaAtEndRegex, "")
        return JSONObject(fixedCommaAtEnd)
    }

    private fun setupVisualRulesAdapter() = lazy {
        RulesPathAdapter()
    }
}
