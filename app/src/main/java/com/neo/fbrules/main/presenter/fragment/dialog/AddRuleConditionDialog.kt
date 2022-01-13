package com.neo.fbrules.main.presenter.fragment.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.fragment.app.DialogFragment
import com.neo.fbrules.R
import com.neo.fbrules.databinding.PopupConditionsBinding
import com.neo.highlight.util.listener.HighlightTextWatcher
import com.neo.highlight.util.scheme.BackgroundScheme
import java.util.regex.Pattern

class AddRuleConditionDialog : DialogFragment() {

    private lateinit var binding: PopupConditionsBinding

    private val propertiesAutocompleteAdapter: ArrayAdapter<String> by setupPropertiesAdapter()
    private val conditionAutocompleteAdapter: ArrayAdapter<String> by setupConditionAdapter()

    private val conditions = arrayListOf("Nenhum", "Todos", "Usuário logado")
    private val properties = arrayListOf("Leitura", "Escrita")

    private fun setupConditionAdapter() = lazy {
        ArrayAdapter(requireContext(), R.layout.dropdown_item, conditions)
    }

    private fun setupPropertiesAdapter() = lazy {
        ArrayAdapter(requireContext(), R.layout.dropdown_item, properties)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val alert = AlertDialog.Builder(requireContext())

        binding = PopupConditionsBinding.inflate(LayoutInflater.from(requireContext()))

        alert.setView(binding.root)
        alert.setCancelable(false)

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
                        properties
                            .joinToString(prefix = "(", separator = ")|(", postfix = ")")
                    ),
                    Color.GRAY
                )
            )

            binding.tlProperty.editText?.addTextChangedListener(this)
        }

        HighlightTextWatcher().apply {
            addScheme(
                BackgroundScheme(
                    Pattern.compile(
                        conditions
                            .joinToString(prefix = "(", separator = ")|(", postfix = ")")
                    ),
                    Color.GRAY
                )
            )

            binding.tlCondition.editText?.addTextChangedListener(this)
        }
    }

    private fun setupListener() {

        val propertiesAutocomplete = binding.tlProperty.editText as AutoCompleteTextView

        binding.ibExpandPropertyBtn.setOnClickListener {
            propertiesAutocomplete.showDropDown()
        }

        val conditionsAutocomplete = binding.tlCondition.editText as AutoCompleteTextView
        conditionsAutocomplete.setAdapter(conditionAutocompleteAdapter)

        binding.ibExpandConditionBtn.setOnClickListener {
            conditionsAutocomplete.showDropDown()
        }

        binding.ibCloseBtn.setOnClickListener {
            dismiss()
        }
    }
}
