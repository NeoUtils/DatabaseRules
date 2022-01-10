package com.neo.fbrules.main.presenter.fragment.content

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.neo.fbrules.core.HistoricTextWatcher
import com.neo.fbrules.databinding.ContentMainBinding
import com.neo.fbrules.main.domain.model.HistoricModel
import com.neo.fbrules.main.domain.model.RulesEditor

class RulesEditorFragment : Fragment(), RulesEditor {

    private lateinit var historic: HistoricModel
    private lateinit var binding: ContentMainBinding

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

        binding = ContentMainBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListener()
    }

    private fun setupListener() {
        setupHistoric()
    }

    private fun setupHistoric() {
        val historyObserver = HistoricTextWatcher(historic)

        binding.rulesEditor.addTextChangedListener(historyObserver)

        historyObserver.historyListener = object : HistoricTextWatcher.HistoryListener {
            override fun hasUndo(has: Boolean) {
                binding.ibUndoBtn.isEnabled = has
                binding.ibUndoBtn.alpha = if (has) 1f else 0.5f
            }

            override fun hasRedo(has: Boolean) {
                binding.ibRedoBtn.isEnabled = has
                binding.ibRedoBtn.alpha = if (has) 1f else 0.5f
            }

            override fun update(history: Pair<Int, String>) {
                binding.rulesEditor.removeTextChangedListener(historyObserver)

                binding.rulesEditor.setText(history.second)
                binding.rulesEditor.setSelection(history.first)

                binding.rulesEditor.addTextChangedListener(historyObserver)
            }
        }

        binding.ibUndoBtn.setOnClickListener {
            historyObserver.undo()
        }

        binding.ibRedoBtn.setOnClickListener {
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