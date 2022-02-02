package com.neo.fbrules.main.presenter.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.neo.fbrules.databinding.FragmentTextRulesEditorBinding
import com.neo.fbrules.main.presenter.components.HistoricTextWatcher
import com.neo.fbrules.main.presenter.contract.RulesEditor
import com.neo.fbrules.main.presenter.model.HistoricModel

private typealias TextEditorView = FragmentTextRulesEditorBinding

/**
 * Text rule editor
 * @author Irineu A. Silva
 */
class TextEditorFragment : Fragment(), RulesEditor {

    private lateinit var historic: HistoricModel
    private lateinit var binding: TextEditorView

    //override Fragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        historic = getHistoricModel(savedInstanceState)
    }

    private fun getHistoricModel(savedInstanceState: Bundle?) =
        savedInstanceState?.getParcelable(HistoricModel::class.simpleName)
            ?: HistoricModel(list = mutableListOf(0 to ""))

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putParcelable(HistoricModel::class.simpleName, historic)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = TextEditorView.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupHistoric()
        setupArguments()
    }

    //override RulesEditor

    override fun getRules(): String {
        return binding.textRulesEditor.text.toString()
    }

    override fun setRules(rules: String) {
        binding.textRulesEditor.setText(rules)
    }

    //members functions

    private fun setupArguments() {
        arguments?.getString("rules", null)?.let { setRules(it) }
    }

    private fun setupHistoric() = with(binding) {

        val historyObserver = HistoricTextWatcher(historic).apply {
            historyListener = object : HistoricTextWatcher.HistoryListener {
                override fun hasUndo(has: Boolean) {
                    fab.ibUndoBtn.isEnabled = has
                    fab.ibUndoBtn.alpha = if (has) 1f else 0.5f
                }

                override fun hasRedo(has: Boolean) {
                    fab.ibRedoBtn.isEnabled = has
                    fab.ibRedoBtn.alpha = if (has) 1f else 0.5f
                }

                override fun update(history: Pair<Int, String>) {
                    textRulesEditor.removeTextChangedListener(this@apply)

                    textRulesEditor.setText(history.second)
                    textRulesEditor.setSelection(history.first)

                    textRulesEditor.addTextChangedListener(this@apply)
                }
            }
        }

        textRulesEditor.addTextChangedListener(historyObserver)

        fab.ibUndoBtn.setOnClickListener {
            historyObserver.undo()
        }

        fab.ibRedoBtn.setOnClickListener {
            historyObserver.redo()
        }
    }

}
