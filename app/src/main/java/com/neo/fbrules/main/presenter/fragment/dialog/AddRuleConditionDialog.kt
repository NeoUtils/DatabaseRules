package com.neo.fbrules.main.presenter.fragment.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.neo.fbrules.ERROR
import com.neo.fbrules.R
import com.neo.fbrules.core.Expression
import com.neo.fbrules.core.constants.Highlighting
import com.neo.fbrules.databinding.DialogRuleConditionsBinding
import com.neo.fbrules.handlerError
import com.neo.fbrules.main.presenter.model.RuleCondition
import com.neo.fbrules.main.presenter.model.RuleModel
import com.neo.fbrules.util.requestColor
import com.neo.fbrules.util.visibility
import com.neo.highlight.util.listener.HighlightTextWatcher
import com.neo.highlight.util.scheme.BackgroundScheme
import com.neo.highlight.util.scheme.ColorScheme
import org.json.JSONObject
import java.util.regex.Pattern

private typealias AddRuleConditionView = DialogRuleConditionsBinding

class AddRuleConditionDialog : DialogFragment() {

    private lateinit var binding: AddRuleConditionView

    private val propertiesAutocompleteAdapter: ArrayAdapter<String> by setupPropertiesAdapter()
    private val conditionAutocompleteAdapter: ArrayAdapter<String> by setupConditionAdapter()

    private val conditions = arrayListOf(
        "Nenhum" to "false",
        "Usuário logado" to "auth.uid == \$uid",
        "Todos" to "true"
    )

    private val conditionsFirst get() = conditions.map { it.first }

    private val properties = arrayListOf(
        "Leitura" to ".read",
        "Escrita" to ".write"
    )

    private val propertiesFirst get() = properties.map { it.first }

    private fun setupConditionAdapter() = lazy {
        ArrayAdapter(requireContext(), R.layout.dropdown_item, conditionsFirst)
    }

    private fun setupPropertiesAdapter() = lazy {
        ArrayAdapter(requireContext(), R.layout.dropdown_item, propertiesFirst)
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val alert = AlertDialog.Builder(requireContext())

        binding = AddRuleConditionView.inflate(LayoutInflater.from(requireContext()))

        alert.setView(binding.root)

        isCancelable = false

        setupListener()
        setupView()

        return alert.create()
    }

    private fun setupView() {


        binding.head.tvTitle.text = "Adicionar condição"

        binding.head.ibCloseBtn.visibility(false)

        val propertiesAutocomplete = binding.tlProperty.editText as AutoCompleteTextView

        propertiesAutocomplete.setAdapter(propertiesAutocompleteAdapter)

        val conditionsAutocomplete = binding.tlCondition.editText as AutoCompleteTextView

        conditionsAutocomplete.setAdapter(conditionAutocompleteAdapter)

        setupHighlight()
    }

    private fun setupHighlight() {
        HighlightTextWatcher().apply {

            schemes = Highlighting(requireContext()).propertySyntax

            addScheme(
                BackgroundScheme(
                    Pattern.compile(
                        propertiesFirst.joinToString(
                            prefix = "(",
                            separator = ")|(",
                            postfix = ")"
                        )
                    ),
                    Color.GRAY
                )
            )

            binding.tlProperty.editText?.addTextChangedListener(this)
        }

        HighlightTextWatcher().apply {

            schemes = Highlighting(requireContext()).conditionSyntax

            addScheme(
                BackgroundScheme(
                    Pattern.compile(
                        conditionsFirst.joinToString(
                            prefix = "(",
                            separator = ")|(",
                            postfix = ")"
                        )
                    ),
                    Color.GRAY
                ),
                ColorScheme(
                    Expression.variable,
                    requireContext().theme.requestColor(R.attr.colorAccent)
                )
            )

            binding.tlCondition.editText?.addTextChangedListener(this)
        }
    }

    private fun setupListener() {

        val propertiesAutocomplete = binding.tlProperty.editText as AutoCompleteTextView
        propertiesAutocomplete.setAdapter(propertiesAutocompleteAdapter)

        binding.optionsProperty.ibExpandBtn.setOnClickListener {
            propertiesAutocomplete.showDropDown()
        }

        val conditionsAutocomplete = binding.tlCondition.editText as AutoCompleteTextView
        conditionsAutocomplete.setAdapter(conditionAutocompleteAdapter)

        binding.optionsConditions.ibExpandBtn.setOnClickListener {
            conditionsAutocomplete.showDropDown()
        }
        binding.tlProperty.editText?.addTextChangedListener { editable ->
            val value = editable?.toString()
            val property = properties.find { it.first == value }

            property?.let {

                binding.optionsProperty.ibCodeBtn.setOnClickListener {
                    propertiesAutocomplete.setText(property.second, false)
                }

            }

            binding.tlProperty.isErrorEnabled = false
            binding.optionsProperty.ibCodeBtn.visibility(property != null)
        }

        binding.optionsProperty.ibCodeBtn.visibility(false)

        binding.tlCondition.editText?.addTextChangedListener { editable ->
            val value = editable?.toString()
            val condition = conditions.find { it.first == value }

            condition?.let {

                binding.optionsConditions.ibCodeBtn.setOnClickListener {
                    conditionsAutocomplete.setText(condition.second, false)
                }
            }
            binding.tlCondition.isErrorEnabled = false
            binding.optionsConditions.ibCodeBtn.visibility(condition != null)
        }

        binding.optionsConditions.ibCodeBtn.visibility(false)

        binding.confirm.button.setOnClickListener {
            confirm()
        }

        binding.cancel.button.setOnClickListener {
            dismiss()
        }

        binding.head.ibBackBtn.setOnClickListener {
            dismiss()
        }

    }

    private fun confirm() {

        val condition = getCondition().trim()
        val property = getProperty().trim()

        if (!validate(condition, property)) return

        val result = Bundle().apply {
            putParcelable(
                RuleCondition::class.java.simpleName,
                RuleCondition(property, condition)
            )

            arguments?.let {
                val position = it.getInt("position", -1)
                if (position != -1) {
                    putInt("position", position)
                }
            }
        }

        setFragmentResult(TAG, result); dismiss()
    }

    private fun validate(condition: String, property: String): Boolean {

        if (property.isBlank()) {
            binding.tlProperty.error = "Digite alguma propriedade"
            return false
        }

        if (condition.isBlank()) {
            binding.tlCondition.error = "Digite alguma condição"
            return false
        }

        return runCatching {
            JSONObject("{\"$property\":\"$condition\"}")
            true
        }.getOrElse {
            handlerError(ERROR.INVALID_JSON, it)
            false
        }
    }

    private fun getProperty(): String {
        val propertyValue = binding.tlProperty.editText!!.text.toString()
        return properties.find { it.first == propertyValue }?.second ?: propertyValue
    }

    private fun getCondition(): String {
        val conditionValue = binding.tlCondition.editText!!.text.toString()
        return conditions.find { it.first == conditionValue }?.second ?: conditionValue
    }

    companion object {
        val TAG = AddRuleConditionDialog::class.java.simpleName
    }
}
