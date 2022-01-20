package com.neo.fbrules.main.presenter.fragment.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import com.neo.fbrules.R
import com.neo.fbrules.core.Expression
import com.neo.fbrules.databinding.DialogAddPathBinding
import com.neo.fbrules.main.presenter.adapter.RuleConditionsAdapter
import com.neo.fbrules.main.presenter.model.RuleCondition
import com.neo.fbrules.main.presenter.model.RuleModel
import com.neo.fbrules.util.requestColor
import com.neo.fbrules.util.showAlertDialog
import com.neo.fbrules.util.visibility
import com.neo.highlight.util.listener.HighlightTextWatcher
import com.neo.highlight.util.scheme.ColorScheme
import org.json.JSONObject
import java.util.regex.Pattern

private typealias AddRulePathView = DialogAddPathBinding

class AddRulePathDialog : DialogFragment() {

    private lateinit var binding: AddRulePathView

    private val ruleConditionsAdapter: RuleConditionsAdapter by setupRulesConditions()

    private lateinit var ruleModel: RuleModel
    private val conditions get() = ruleModel.conditions
    private val path get() = ruleModel.path

    private fun setupRulesConditions() = lazy {
        RuleConditionsAdapter({ ruleModel }).apply {
            binding.rvRuleConditions.adapter = this
        }
    }

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

    private fun setupListeners() {

        binding.tlPath.editText?.addTextChangedListener {
            val value = it?.toString()

            if (value != null) {
                ruleModel.path = value
                ruleConditionsAdapter.updateAll()
            }

            binding.tlPath.isErrorEnabled = false
        }

        binding.mbAddCondition.setOnClickListener {
            showAddRuleCondition()
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


    private fun setupView() {

        binding.head.tvTitle.text = "Adicionar Regra"

        binding.tlPath.editText?.setText(path)

        ruleConditionsAdapter.updateAll()

        binding.head.ibBackBtn.visibility(false)
    }

    private fun setupArguments() {
        arguments?.let { it ->
            it.getSerializable(RuleModel::class.java.simpleName)?.also {
                ruleModel = it as RuleModel
            }
        } ?: run {
            ruleModel = RuleModel()
        }
    }

    private fun setupHighlight() {
        HighlightTextWatcher().apply {
            addScheme(
                ColorScheme(
                    Pattern.compile("/"),
                    Color.CYAN
                ),
                ColorScheme(
                    Expression.variable,
                    requestColor(R.color.syntax_variable)
                )
            )
            binding.tlPath.editText?.addTextChangedListener(this)
        }
    }

    private fun showAddRuleCondition() {

        registerAddCondition()

        val dialog = AddRuleConditionDialog()

        dialog.show(
            parentFragmentManager,
            AddRuleConditionDialog.TAG
        )
    }

    private fun registerAddCondition() {
        setFragmentResultListener(AddRuleConditionDialog.TAG) { _, bundle ->
            val ruleCondition =
                bundle.getParcelable<RuleCondition>(RuleCondition::class.java.simpleName)

            ruleCondition?.let {
                addCondition(ruleCondition)
            }
        }
    }

    private fun addCondition(ruleCondition: RuleCondition) {
        if (conditions.any { it.property == ruleCondition.property }) {
            showAlertDialog("Error", "Essa propriedade já existe") {
                positiveButton()
            }
            return
        }

        conditions.add(ruleCondition)
        ruleConditionsAdapter.updateAll()
    }

    private fun confirm() {
        val path = binding.tlPath.editText!!.text.toString()

        if (!validate(path.trim(), conditions)) return

        val result = Bundle().apply {

            val ruleModel =
                RuleModel(
                    "rules/${path.substringAfter("rules/")}"
                        .replace("//", "/"), conditions
                )

            putParcelable(
                RuleModel::class.java.simpleName,
                ruleModel
            )

            arguments?.let {
                putInt("position", it.getInt("position", -1))
            }
        }

        setFragmentResult(TAG, result); dismiss()
    }

    private fun validate(path: String, conditions: MutableList<RuleCondition>): Boolean {

        if (path.isBlank()) {
            binding.tlPath.error = "Digite o caminho"
            return false
        }

        return runCatching {
            JSONObject().put(path, JSONObject().apply {
                conditions.forEach {
                    put(it.property, it.condition)
                }
            })
            true
        }.getOrElse { false }
    }

    companion object {
        val TAG: String = AddRulePathDialog::class.java.simpleName
    }
}
