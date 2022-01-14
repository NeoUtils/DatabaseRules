package com.neo.fbrules.main.presenter.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import com.neo.fbrules.ERROR
import com.neo.fbrules.R
import com.neo.fbrules.databinding.FragmentVisualRulesEditorBinding
import com.neo.fbrules.handlerError
import com.neo.fbrules.main.presenter.components.ReadRulesJson
import com.neo.fbrules.main.presenter.adapter.RulesPathAdapter
import com.neo.fbrules.main.presenter.contract.RulesEditor
import com.neo.fbrules.main.presenter.fragment.dialog.AddRulePathDialog
import com.neo.fbrules.main.presenter.model.RuleModel
import com.neo.fbrules.util.showAlertDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

private typealias VisualEditorView = FragmentVisualRulesEditorBinding

class VisualEditorFragment : Fragment(), RulesEditor {

    private lateinit var binding: VisualEditorView
    private val rulesPathAdapter: RulesPathAdapter by setupVisualRulesAdapter()

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

        setupListeners()
        getRulesFromArguments()
    }

    private fun setupListeners() {
        binding.fbAddPathBtn.setOnClickListener {
            showAddPathDialog()
        }

        setFragmentResultListener(AddRulePathDialog.TAG) { _, bundle ->
            val rule = bundle.getParcelable<RuleModel>(RuleModel::class.java.simpleName)
            rule?.let {
                rulesPathAdapter.addRule(rule)
            }
        }
    }

    private fun showAddPathDialog() {
        val dialog = AddRulePathDialog()

        dialog.show(parentFragmentManager, AddRulePathDialog.TAG)
    }

    private fun getRulesFromArguments() {
        arguments?.getString("rules", null)?.let { setRules(it) }
    }

    private fun readRulesJson(rulesJson: JSONObject): Result<Any> = runCatching {
        rulesPathAdapter.clear()

        lifecycleScope.launch(Dispatchers.IO) {
            val rules = ReadRulesJson().getRulesModel(rulesJson)
            withContext(Dispatchers.Main) {
                rulesPathAdapter.setRules(rules)
            }
        }
    }.onFailure {
        handlerError(
            ERROR.UNRECOGNIZED_RULES, it
        )
    }

    override fun getRules(): String? {

        val rules = rulesPathAdapter.getRules()

        if (rules.isEmpty()) return null

        return ReadRulesJson().getRulesString(rules)
    }

    override fun setRules(rules: String) {
        var tryFix = false

        runCatching {

            if (rules.isEmpty()) {
                return@runCatching
            }

            val rulesJson = runCatching {
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

            readRulesJson(rulesJson)
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
        RulesPathAdapter().apply {
            binding.rvRules.adapter = this
        }
    }
}
