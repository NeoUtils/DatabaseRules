package com.neo.fbrules.main.presenter.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.neo.fbrules.R
import com.neo.fbrules.core.ERROR
import com.neo.fbrules.core.handlerError
import com.neo.fbrules.databinding.FragmentVisualRulesEditorBinding
import com.neo.fbrules.main.presenter.components.ReadRulesJson
import com.neo.fbrules.main.presenter.adapter.RulesPathAdapter
import com.neo.fbrules.main.presenter.contract.RulesEditor
import com.neo.fbrules.main.presenter.fragment.dialog.AddRuleConditionDialog
import com.neo.fbrules.main.presenter.fragment.dialog.PathDialog
import com.neo.fbrules.main.presenter.model.RuleCondition
import com.neo.fbrules.main.presenter.model.RuleModel
import com.neo.fbrules.util.showAlertDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

private typealias VisualEditorView = FragmentVisualRulesEditorBinding

class VisualEditorFragment : Fragment(),
    RulesEditor,
    RulesPathAdapter.RulesPathListener {

    private lateinit var binding: VisualEditorView
    private val rulesPathAdapter: RulesPathAdapter by setupVisualRulesAdapter()
    private val rules get() = rulesPathAdapter.getRules()

    //setups

    private fun setupVisualRulesAdapter() = lazy {
        RulesPathAdapter(this).apply {
            binding.rvRules.adapter = this
        }
    }

    //override Fragment

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

        setupArguments()
        setupListeners()
    }

    //override RulesPathListener

    override fun onAddRule(pathPosition: Int) {
        showRuleConditionDialog(pathPosition, null)
    }

    override fun onEditPath(rule: RuleModel, position: Int) {
        showPathDialog(rule, position)
    }

    override fun onEditRule(rule: RuleCondition, pathPosition: Int, rulePosition: Int) {
        showRuleConditionDialog(pathPosition, rule, rulePosition)
    }

    override fun onRemovePath(pathPosition: Int) {

        val pathToRemove = rules[pathPosition].rootPath
        val allRulesToRemove = rules.filter { it.rootPath.startsWith(pathToRemove) }

        val listedPaths: String = allRulesToRemove
            .joinToString(prefix = "\n", separator = ",\n") { it.rootPath }

        showAlertDialog(
            getString(R.string.text_visualEditor_onRemovePath_title),
            getString(R.string.text_visualEditor_onRemovePath_message, listedPaths)
        ) {
            positiveButton(getString(R.string.btn_remove)) {

                rules.removeAll { allRulesToRemove.contains(it) }
                rulesPathAdapter.updateAll()

            }
            negativeButton(getString(R.string.btn_cancel))
        }
    }

    override fun onRemoveRule(pathPosition: Int, rulePosition: Int) {
        val path = rules[pathPosition]

        showAlertDialog(
            getString(R.string.text_visualEditor_onRemoveRule_title),
            getString(R.string.text_visualEditor_onRemoveRule_message, path)
        ) {
            positiveButton(getString(R.string.btn_remove)) {
                path.conditions.removeAt(rulePosition)
                rulesPathAdapter.updateAll()
            }
            negativeButton(getString(R.string.btn_cancel))
        }
    }

    //override RulesEditor

    override fun getRules(): String? {

        if (rules.isEmpty()) return null

        return ReadRulesJson().getRulesString(rules)
    }

    override fun setRules(rules: String) {
        var tryFix = false

        runCatching {

            rulesPathAdapter.clear()

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

    //member functions

    private fun setupListeners() {
        binding.fbAddPathBtn.setOnClickListener {
            showPathDialog()
        }
    }

    private fun setupArguments() {
        arguments?.getString("rules", null)?.let { setRules(it) }
    }

    private fun showRuleConditionDialog(
        pathPosition: Int,
        rule: RuleCondition? = null,
        rulePosition: Int = -1
    ) {

        registerAddConditionListener()

        val dialog = AddRuleConditionDialog()

        dialog.arguments = Bundle().apply {
            putInt("path_position", pathPosition)
            putInt("rule_position", rulePosition)

            rule?.let {
                putSerializable(RuleCondition::class.java.simpleName, rule)
            }
        }

        dialog.show(
            parentFragmentManager,
            AddRuleConditionDialog.TAG
        )
    }

    private fun registerAddConditionListener() {
        setFragmentResultListener(AddRuleConditionDialog.TAG) { _, bundle ->
            val condition =
                bundle.getParcelable<RuleCondition>(RuleCondition::class.java.simpleName)

            val pathPosition = bundle.getInt("path_position", -1)

            if (condition != null && pathPosition != -1) {

                val rulePosition = bundle.getInt("rule_position", -1)

                if (rulePosition != -1) {
                    editRule(condition, pathPosition, rulePosition)
                } else {
                    addRule(condition, pathPosition)
                }
            }
        }
    }

    private fun editRule(
        condition: RuleCondition,
        pathPosition: Int,
        rulePosition: Int
    ) {
        rules[pathPosition].conditions[rulePosition] = condition
        rulesPathAdapter.updateAll()
    }

    private fun addRule(
        condition: RuleCondition,
        position: Int
    ) {
        val rule = rules[position]
        val foundEqualsProperty = rule.conditions.find { it.property == condition.property }

        foundEqualsProperty?.let {
            showAlertDialog(
                "Error",
                getString(R.string.text_visualEditor_addRuleError_hasPropertyError, it.property)
            ) {
                positiveButton()
            }
        } ?: run {
            rule.conditions.add(condition)
            rulesPathAdapter.setRules(rules)
        }
    }


    private fun showPathDialog(
        rule: RuleModel? = null,
        position: Int? = null
    ) {

        registerAddPathListener()

        val dialog = PathDialog()

        if (rule != null && position != null) {
            dialog.arguments = Bundle().apply {
                putString(RuleModel::class.java.simpleName, Gson().toJson(rule))
                putInt("position", position)
            }
        }

        dialog.show(
            parentFragmentManager,
            PathDialog.TAG
        )
    }

    private fun registerAddPathListener() {
        setFragmentResultListener(PathDialog.TAG) { _, bundle ->
            val rule = bundle.getParcelable<RuleModel>(RuleModel::class.java.simpleName)
            rule?.let {
                val position = bundle.getInt("position", -1)
                if (position != -1) {
                    editPath(rule, position)
                } else {
                    addPath(rule)
                }
            }
        }
    }

    private fun addPath(rule: RuleModel) {

        val foundEqualsPath = rules.find { it.rootPath == rule.rootPath }

        foundEqualsPath?.let {
            showAlertDialog(
                "Error",
                getString(R.string.text_visualEditor_addPathError_hasPathError, it.actualPath)
            ) {
                positiveButton()
            }
        } ?: runCatching {
            rulesPathAdapter.addRule(rule)
        }.onFailure {
            handlerError(ERROR.INVALID_JSON, it)
        }
    }

    private fun editPath(rule: RuleModel, position: Int) {
        runCatching {
            rulesPathAdapter.editRule(rule, position)
        }.onFailure {
            handlerError(ERROR.INVALID_JSON, it)
        }
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

    private fun showFixedRulesAlert() {
        showAlertDialog(
            getString(R.string.text_visualEditor_fixedRules_title),
            getString(R.string.text_visualEditor_fixedRules_message)
        ) {
            positiveButton()
        }
    }

    private fun tryFixRules(rules: String): JSONObject {
        val detectCommaAtEndRegex = Regex(",(?=[^{}\\w]*\\})")
        val fixedCommaAtEnd = rules.replace(detectCommaAtEndRegex, "")
        return JSONObject(fixedCommaAtEnd)
    }
}
