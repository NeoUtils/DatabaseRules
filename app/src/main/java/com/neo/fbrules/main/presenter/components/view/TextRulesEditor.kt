package com.neo.fbrules.main.presenter.components.view

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import com.neo.fbrules.R
import com.neo.fbrules.core.constants.Highlighting
import com.neo.fbrules.util.getCompatColor
import com.neo.highlight.util.listener.HighlightTextWatcher


class TextRulesEditor(
    context: Context, attr: AttributeSet? = null
) : AppCompatEditText(context, attr) {


    init {
        setBackgroundColor(getContext().getCompatColor(R.color.bg_editors))
        setupHighlighting(context)
    }

    private fun setupHighlighting(context: Context) {
        HighlightTextWatcher().apply {
            schemes = Highlighting(context).completeSyntax
            addTextChangedListener(this)
        }
    }


    override fun onTextChanged(
        text: CharSequence?,
        start: Int,
        lengthBefore: Int,
        lengthAfter: Int
    ) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter)

        text?.let {

            val count = lengthAfter - lengthBefore
            val selection = selectionEnd

            if (count == 1 && selection > 0) {
                newInsert(text[selection - 1], text)
            }
        }

    }

    private fun newInsert(insertion: Char, text: CharSequence) {
        val selection = selectionEnd
        when (insertion) {
            '\n' -> {

                val indentCount = getIndent(text.subSequence(0, selection).toString())
                val indent = toString(indentCount)

                if (text.length > selection && text[selection] == '}') {
                    val before = toString(indentCount - 4)

                    this.text!!.insert(selection, "$indent\n$before")

                    setSelection(selection + indent.length)
                } else {
                    this.text!!.insert(selection, indent)
                }
            }
            '{' -> {
                this.text!!.insert(selection, "}")
                setSelection(selection)
            }
        }
    }

    private fun toString(indent: Int): CharSequence {
        var localIndent = indent
        val result = StringBuilder()

        while (localIndent > 0) {
            localIndent--
            result.append(" ")
        }
        return result
    }

    private fun getIndent(before: String): Int {
        var indent = 0
        for (c in before.toCharArray()) {
            if (c == '{') {
                indent += 4
            }
            if (c == '}') {
                indent -= 4
            }
        }
        return indent
    }
}