package com.neo.fbrules.main.presenter.components.view

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import com.neo.fbrules.R
import com.neo.fbrules.util.getCompatColor
import com.neo.fbrules.util.requestColor
import com.neo.highlight.util.listener.HighlightTextWatcher
import com.neo.highlight.util.scheme.ColorScheme
import java.util.regex.Pattern


class TextRulesEditor(
    context: Context, attr: AttributeSet? = null
) : AppCompatEditText(context, attr) {

    private val highlight = HighlightTextWatcher()

    init {
        setBackgroundColor(getContext().getCompatColor(R.color.bg_editors))

        highlight.apply {
            addScheme(
                ColorScheme(
                    Pattern.compile("(?<!\\w)(true|false)(?!\\w)"),
                    getContext().getCompatColor(R.color.bool)
                ),
                ColorScheme(
                    Pattern.compile("\"[^\"]*\""),
                    getContext().getCompatColor(R.color.string)
                ),
                ColorScheme(
                    Pattern.compile("[/]{2}.*"),
                    getContext().getCompatColor(R.color.comment)
                ),
                ColorScheme(
                    Pattern.compile("(?<=auth\\.)uid|auth|null"),
                    context.theme.requestColor(R.attr.colorAccent)
                ),
                ColorScheme(
                    Pattern.compile("===|==|!="),
                    context.theme.requestColor(R.attr.colorPrimary)
                ),
                ColorScheme(
                    Pattern.compile("\\$\\w+"),
                    context.requestColor(R.color.bg_variable)
                ),
                ColorScheme(
                    Pattern.compile("\"(\\.read|\\.write)\""),
                    context.theme.requestColor(R.attr.colorAccent)
                )
            )
        }

        addTextChangedListener(highlight)
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