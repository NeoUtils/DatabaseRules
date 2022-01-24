package com.neo.fbrules.main.presenter.fragment.dialog

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import com.google.gson.Gson
import com.neo.fbrules.R
import com.neo.fbrules.core.Expression
import com.neo.fbrules.databinding.DialogPathBinding
import com.neo.fbrules.main.presenter.adapter.RulesAdapter
import com.neo.fbrules.main.presenter.model.RuleModel
import com.neo.fbrules.main.presenter.model.PathModel
import com.neo.fbrules.util.requestColor
import com.neo.fbrules.util.showAlertDialog
import com.neo.fbrules.util.visibility
import com.neo.highlight.util.listener.HighlightTextWatcher
import com.neo.highlight.util.scheme.ColorScheme
import org.json.JSONObject
import java.util.regex.Pattern

private typealias AddRulePathView = DialogPathBinding

class PathDialog : DialogFragment(), RulesAdapter.OnRuleClickListener {

    private lateinit var binding: AddRulePathView
    private lateinit var oldPath: String

    private val rulesAdapter: RulesAdapter by setupRulesConditions()

    private lateinit var pathModel: PathModel
    private val conditions get() = pathModel.rules
    private val path get() = pathModel.rootPath

    private val isEdit
        get() = arguments?.let {
            it.getInt("position", -1) != -1
        } ?: false

    //setup

    private fun setupRulesConditions() = lazy {
        RulesAdapter(this) { pathModel }.apply {
            binding.rvRuleConditions.adapter = this
        }
    }

    //override DialogFragment

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val alert = AlertDialog.Builder(requireContext())

        binding = AddRulePathView.inflate(LayoutInflater.from(requireContext()))
        alert.setView(binding.root)

        isCancelable = true

        setupHighlight()
        setupArguments()

        setupView()
        setupListeners()

        return alert.create()
    }

    @SuppressLint("SetTextI18n")
    private fun setupListeners() {

        binding.tlPath.editText?.addTextChangedListener {
            val value = it?.toString()

            if (value != null) {

                val rulePath = value.toRulePath()

                if (oldPath.length == 1 || rulePath.startsWith(oldPath)) {
                    pathModel.rootPath = rulePath
                    rulesAdapter.updateAll()
                } else {
                    binding.tlPath.editText?.apply {
                        setText(oldPath.fromRulePath())
                        setSelection(this.length())
                    }
                }
            }

            binding.tlPath.isErrorEnabled = false
        }

        binding.mbAddCondition.setOnClickListener {
            showRuleDialog()
        }

        binding.head.ibCloseBtn.setOnClickListener {
            dismiss()
        }

        binding.cancel.button.setOnClickListener {
            dismiss()
        }

        binding.confirm.button.setOnClickListener {
            confirm()
        }
    }

    //override OnRuleClickListener

    override fun onRuleEdit(rule: RuleModel, position: Int) {
        showRuleDialog(rule, position)
    }

    override fun onRuleRemove(rule: RuleModel, position: Int) {

        showAlertDialog(
            getString(R.string.text_visualEditor_onRemoveRule_title),
            getString(R.string.text_visualEditor_onRemoveRule_message, rule)
        ) {
            positiveButton(getString(R.string.btn_remove)) {
                pathModel.rules.removeAt(position)
                rulesAdapter.updateAll()
            }

            negativeButton(getString(R.string.btn_cancel))
        }
    }

    //member functions

    private fun setupView() {

        binding.head.tvTitle.text = if (isEdit) {
            getString(R.string.text_visualEditor_pathDialog_editPathTitle)
        } else {
            getString(R.string.text_visualEditor_pathDialog_addPathTitle)
        }

        binding.confirm.button.text = if (isEdit) {
            getString(R.string.btn_edit)
        } else {
            getString(R.string.btn_add)
        }

        binding.cancel.button.text = getString(R.string.btn_cancel)

        if (path.isNotEmpty()) {
            binding.tlPath.editText?.setText(path.substringAfter("rules/"))

            if (path == "rules") {
                binding.tlPath.isEnabled = false
            }
        }

        rulesAdapter.updateAll()

        binding.head.ibBackBtn.visibility(false)
    }

    private fun setupArguments() {
        pathModel = arguments?.let { it ->

            it.getString(
                PathModel::class.java.simpleName
            )?.let {
                Gson().fromJson(it, PathModel::class.java)
            }

        } ?: PathModel()

        oldPath = "${pathModel.parentPath}/"
    }

    private fun setupHighlight() {
        HighlightTextWatcher().apply {
            addScheme(
                ColorScheme(
                    Pattern.compile("/"),
                    Color.CYAN
                ),
                ColorScheme(
                    Expression.variableInProperty,
                    requestColor(R.color.syntax_variable)
                )
            )
            binding.tlPath.editText?.addTextChangedListener(this)
        }
    }

    private fun showRuleDialog(
        rule: RuleModel? = null,
        rulePosition: Int? = null
    ) {

        registerAddCondition()

        RuleDialog().apply {

            if (rule != null && rulePosition != null) {
                arguments = Bundle().apply {
                    putSerializable(RuleModel::class.java.simpleName, rule)
                    putInt("rule_position", rulePosition)
                }
            }

            show(
                this@PathDialog.parentFragmentManager,
                RuleDialog.TAG
            )
        }
    }

    private fun registerAddCondition() =
        setFragmentResultListener(RuleDialog.TAG) { _, bundle ->
            val rules =
                bundle.getParcelable<RuleModel>(RuleModel::class.java.simpleName)

            rules?.let {
                val rulePosition = bundle.getInt("rule_position", -1)

                if (rulePosition != -1) {
                    editRule(rules, rulePosition)
                } else {
                    addCondition(rules)
                }
            }
        }

    private fun editRule(rule: RuleModel, position: Int) {
        conditions[position] = rule
        rulesAdapter.updateAll()
    }

    private fun addCondition(rule: RuleModel) {
        val foundEqualsProperty = conditions.find { it.property == rule.property }
        foundEqualsProperty?.let {
            showAlertDialog(
                "Error",
                getString(R.string.text_visualEditor_addRuleError_hasPropertyError, it.property)
            ) {
                positiveButton()
            }
        } ?: run {
            conditions.add(rule)
            rulesAdapter.updateAll()
        }
    }

    private fun confirm() {
        val path = binding.tlPath.editText!!.text.toString()

        if (!validate(path.trim(), conditions)) return

        val result = Bundle().apply {

            val ruleModel =
                PathModel(
                    path.toRulePath(), conditions
                )

            putParcelable(
                PathModel::class.java.simpleName,
                ruleModel
            )

            arguments?.let {
                putInt("position", it.getInt("position", -1))
            }
        }

        setFragmentResult(TAG, result); dismiss()
    }

    private fun String.toRulePath() : String {
        val hasRulesPath = this.substringBefore("/") == "rules"
        return if (hasRulesPath) this else "rules/$this"
    }

    private fun String.fromRulePath() : String {
        val hasRulesPath = this.substringBefore("/") == "rules"
        return if (hasRulesPath) this.substringAfter("rules/") else this
    }

    private fun validate(
        path: String,
        rule: MutableList<RuleModel>
    ): Boolean {

        if (path.isBlank()) {
            binding.tlPath.error =
                getString(R.string.text_visualEditor_pathDialog_validateError_enterPath)
            return false
        }

        if (path.substringAfterLast("/").isEmpty()) {
            binding.tlPath.error =
                getString(R.string.text_visualEditor_pathDialog_validateError_enterPath)
            return false
        }

        return runCatching {
            JSONObject().put(path, JSONObject().apply {
                rule.forEach {
                    put(it.property, it.condition)
                }
            })
            true
        }.getOrElse { false }
    }

    companion object {
        val TAG: String = PathDialog::class.java.simpleName
    }
}
