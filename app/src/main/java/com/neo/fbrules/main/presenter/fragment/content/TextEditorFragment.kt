package com.neo.fbrules.main.presenter.fragment.content

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.neo.fbrules.databinding.ContentTextRulesEditorBinding
import com.neo.fbrules.main.presenter.contract.RulesEditor
import com.neo.fbrules.main.presenter.model.HistoricModel
import com.neo.fbrules.main.presenter.model.HistoricTextWatcher

private typealias TextEditorView = ContentTextRulesEditorBinding

class TextEditorFragment : Fragment(), RulesEditor {

    private lateinit var historic: HistoricModel
    private lateinit var binding: TextEditorView

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
    }

    private fun setupHistoric() = with(binding) {

        val historyObserver = HistoricTextWatcher(historic).apply {
            historyListener = object : HistoricTextWatcher.HistoryListener {
                override fun hasUndo(has: Boolean) {
                    ibUndoBtn.isEnabled = has
                    ibUndoBtn.alpha = if (has) 1f else 0.5f
                }

                override fun hasRedo(has: Boolean) {
                    ibRedoBtn.isEnabled = has
                    ibRedoBtn.alpha = if (has) 1f else 0.5f
                }

                override fun update(history: Pair<Int, String>) {
                    rulesEditor.removeTextChangedListener(this@apply)

                    rulesEditor.setText(history.second)
                    rulesEditor.setSelection(history.first)

                    rulesEditor.addTextChangedListener(this@apply)
                }
            }
        }

        rulesEditor.addTextChangedListener(historyObserver)

        ibUndoBtn.setOnClickListener {
            historyObserver.undo()
        }

        ibRedoBtn.setOnClickListener {
            historyObserver.redo()
        }
    }

    override fun getRules(): String {
        return binding.rulesEditor.text.toString()
    }

    override fun setRules(rules: String) {
        binding.rulesEditor.setText(rules)
    }

}