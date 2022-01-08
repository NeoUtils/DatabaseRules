package com.neo.fbrules.core

import android.text.Editable
import android.text.Selection
import android.text.TextWatcher
import com.neo.fbrules.main.domain.model.HistoricModel
import kotlinx.coroutines.*

class HistoricTextWatcher(private val model: HistoricModel) : TextWatcher {

    var historyListener: HistoryListener? = null

    private var job: Job? = null

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

    override fun afterTextChanged(rules: Editable) {
        job?.cancel()
        job = CoroutineScope(Dispatchers.IO).launch {
            delay(150)
            val position = Selection.getSelectionStart(rules)
            addRule(position, rules.toString())
        }
    }

    @Synchronized
    private fun addRule(position: Int, rules: String) = with(model) {

        if (list.last().second == rules) return@with

        if (point < list.size) {
            for (index in list.size downTo point) {

                if (index == 1) continue

                list.removeAt(index - 1)
            }
        }

        list.add(position to rules)

        if (list.size > MAX_HISTORIC) {
            list.removeAt(0)
        }

        point = list.size

        update()
    }

    @Synchronized
    fun undo() = with(model) {
        if (list.isEmpty() || point == 1) return@with

        historyListener?.update(list[(--point) - 1])

        update()
    }

    fun redo() = with(model) {
        if (point == list.size) return@with

        historyListener?.update(list[(++point) - 1])

        update()
    }

    fun update() = with(model) {
        historyListener?.hasUndo(point != 1)
        historyListener?.hasRedo(point < list.size)
    }

    fun getActual(): String {
        return model.list[model.point - 1].second
    }

    interface HistoryListener {
        fun hasRedo(has: Boolean)
        fun hasUndo(has: Boolean)
        fun update(history: Pair<Int, String>)
    }

    private companion object {
        const val MAX_HISTORIC = 50
    }
}
