package com.neo.fbrules.main.presenter.fragment.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.neo.fbrules.R
import com.neo.fbrules.databinding.DialogAddPathBinding
import com.neo.fbrules.util.requestColor
import com.neo.highlight.util.listener.HighlightTextWatcher
import com.neo.highlight.util.scheme.ColorScheme
import java.util.regex.Pattern

class AddPathDialog : DialogFragment() {

    private lateinit var binding: DialogAddPathBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val alert = AlertDialog.Builder(requireContext())

        binding = DialogAddPathBinding.inflate(LayoutInflater.from(requireContext()))
        alert.setView(binding.root)

        setupView()

        return alert.create()
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
    }

    companion object {
        val tag = AddPathDialog::class.java.simpleName
    }
}
