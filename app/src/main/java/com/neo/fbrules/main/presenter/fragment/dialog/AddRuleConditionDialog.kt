package com.neo.fbrules.main.presenter.fragment.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.DialogFragment
import com.neo.fbrules.databinding.DialogAddPathBinding
import com.neo.fbrules.databinding.PopupConditionsBinding

class AddRuleConditionDialog : DialogFragment() {

    private lateinit var binding: PopupConditionsBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val alert = AlertDialog.Builder(requireContext())

        binding = PopupConditionsBinding.inflate(LayoutInflater.from(requireContext()))
        alert.setView(binding.root)

        setupView()

        return alert.create()
    }

    private fun setupView() {
    }


}
