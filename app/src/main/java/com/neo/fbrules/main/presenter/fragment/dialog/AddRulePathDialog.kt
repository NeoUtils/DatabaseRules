package com.neo.fbrules.main.presenter.fragment.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResultListener
import com.neo.fbrules.databinding.DialogAddPathBinding
import com.neo.fbrules.main.presenter.adapter.RuleConditionsAdapter
import com.neo.fbrules.main.presenter.model.RuleCondition
import com.neo.fbrules.main.presenter.model.RuleModel
import com.neo.highlight.util.listener.HighlightTextWatcher
import com.neo.highlight.util.scheme.ColorScheme
import java.util.regex.Pattern

private typealias AddRulePathView = DialogAddPathBinding

class AddRulePathDialog : DialogFragment() {

    private lateinit var binding: AddRulePathView
    private val rulePath = RuleModel()

    private val ruleConditionsAdapter: RuleConditionsAdapter by setupRulesConditions()

    private fun setupRulesConditions() = lazy {
        RuleConditionsAdapter().apply {
            binding.rvRuleConditions.adapter = this
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val alert = AlertDialog.Builder(requireContext())

        binding = AddRulePathView.inflate(LayoutInflater.from(requireContext()))
        alert.setView(binding.root)

        setupView()
        setupListeners()

        return alert.create()
    }

    private fun setupListeners() {
        setFragmentResultListener(AddRuleConditionDialog::class.java.simpleName) { _, bundle ->
            val ruleCondition =
                bundle.getParcelable<RuleCondition>(RuleCondition::class.java.simpleName)

            ruleCondition?.let {
                addCondition(ruleCondition)
            }
        }
    }

    private fun addCondition(ruleCondition: RuleCondition) {
        ruleConditionsAdapter.addCondition(ruleCondition)
    }

    private fun setupView() {
        HighlightTextWatcher().apply {
            addScheme(
                ColorScheme(
                    Pattern.compile("/"),
                    Color.CYAN
                )
            )

            binding.tlPath.editText?.addTextChangedListener(this)
        }

        binding.mbAddCondition.setOnClickListener {
            showAddRuleCondition()
        }

        ruleConditionsAdapter.setConditions(rulePath.conditions)
    }

    private fun showAddRuleCondition() {
        val dialog = AddRuleConditionDialog()

        dialog.show(
            parentFragmentManager,
            AddRuleConditionDialog::class.java.simpleName
        )
    }

    companion object {
        val tag = AddRulePathDialog::class.java.simpleName
    }
}
