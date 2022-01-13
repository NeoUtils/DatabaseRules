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
import com.neo.fbrules.R
import com.neo.fbrules.databinding.DialogRuleConditionsBinding
import com.neo.fbrules.util.requestColor
import com.neo.fbrules.util.visibility
import com.neo.highlight.util.listener.HighlightTextWatcher
import com.neo.highlight.util.scheme.BackgroundScheme
import com.neo.highlight.util.scheme.ColorScheme
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

    private val properties = arrayListOf("Leitura" to ".read", "Escrita" to ".write")
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

        binding.tvTitle.text = "Adicionar condição"

        val propertiesAutocomplete = binding.tlProperty.editText as AutoCompleteTextView

        propertiesAutocomplete.setAdapter(propertiesAutocompleteAdapter)

        val conditionsAutocomplete = binding.tlCondition.editText as AutoCompleteTextView

        conditionsAutocomplete.setAdapter(conditionAutocompleteAdapter)

        setupHighLight()
    }

    private fun setupHighLight() {
        HighlightTextWatcher().apply {
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
                ),
                ColorScheme(
                    Pattern.compile("(.read)|(.write)"),
                    requireContext().theme.requestColor(R.attr.colorAccent)
                )
            )

            binding.tlProperty.editText?.addTextChangedListener(this)
        }

        HighlightTextWatcher().apply {
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
                    Pattern.compile("(auth)"),
                    requireContext().theme.requestColor(R.attr.colorAccent)
                ),
                ColorScheme(
                    Pattern.compile("(?<=auth\\.)uid"),
                    requireContext().theme.requestColor(R.attr.colorAccent)
                ),
                ColorScheme(
                    Pattern.compile("=="),
                    requireContext().theme.requestColor(R.attr.colorPrimary)
                ),
                ColorScheme(
                    Pattern.compile("\\$\\w+"),
                    requireContext().theme.requestColor(R.attr.colorAccent)
                ),
                ColorScheme(
                    Pattern.compile("^(true|false)\$"),
                    requestColor(R.color.bool)
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

        binding.ibCloseBtn.setOnClickListener {
            dismiss()
        }

        binding.tlProperty.editText?.addTextChangedListener { editable ->
            val value = editable?.toString()
            val property = properties.find { it.first == value }

            property?.let {

                binding.optionsProperty.ibCodeBtn.setOnClickListener {
                    propertiesAutocomplete.setText(property.second, false)
                }

            }

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

            binding.optionsConditions.ibCodeBtn.visibility(condition != null)
        }

        binding.optionsConditions.ibCodeBtn.visibility(false)

        binding.confirm.button.setOnClickListener {
            confirm()
        }

        binding.cancel.button.setOnClickListener {
            dismiss()
        }
    }

    private fun confirm() {

    }
}
