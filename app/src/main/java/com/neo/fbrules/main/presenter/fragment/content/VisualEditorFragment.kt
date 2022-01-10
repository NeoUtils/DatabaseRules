package com.neo.fbrules.main.presenter.fragment.content

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.neo.fbrules.R
import com.neo.fbrules.databinding.ContentVisualRulesEditorBinding
import com.neo.fbrules.main.presenter.components.view.VisualRulesEditor
import com.neo.fbrules.main.presenter.contract.RulesEditor
import com.neo.fbrules.util.showAlertDialog

private typealias VisualEditorView = ContentVisualRulesEditorBinding

class VisualEditorFragment : Fragment(), RulesEditor {

    private lateinit var binding: VisualEditorView

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

        binding.visualRulesEditor.errorListener = { type, throwable ->

            @StringRes
            val errorMessage = when (type) {

                VisualRulesEditor.ERROR.INVALID_RULES -> {
                    R.string.text_visual_rules_editor_error_invalid_rules
                }

                VisualRulesEditor.ERROR.UNRECOGNIZED_RULES -> {
                    R.string.text_visual_rules_editor_error_unrecognized_rule
                }

                VisualRulesEditor.ERROR.INVALID_JSON -> {
                    R.string.text_visual_rules_editor_error_invalid_json
                }
            }

            showAlertDialog("Error", getString(errorMessage))

            if (throwable != null) {
                Firebase.crashlytics.recordException(throwable)
            }
        }
    }

    override fun getRules(): String {
        return binding.visualRulesEditor.getRules()
    }

    override fun setRules(rules: String) {
        binding.visualRulesEditor.setRules(rules)
    }

}
