package com.neo.fbrules.main.presenter.fragment.content

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.neo.fbrules.databinding.ContentVisualRulesEditorBinding
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

        binding.visualRulesEditor.errorListener = {
            showAlertDialog("Error", it)
        }
    }

    override fun getRules(): String {
        TODO("Not yet implemented")
    }

    override fun setRules(rules: String) {
        binding.visualRulesEditor.setRules(rules)
    }

}
