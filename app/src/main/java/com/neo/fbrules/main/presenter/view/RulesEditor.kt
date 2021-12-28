package com.neo.fbrules.main.presenter.view

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import com.neo.fbrules.R
import com.neo.fbrules.main.presenter.model.ColorScheme
import com.neo.fbrules.util.getCompatColor
import java.util.regex.Matcher


class RulesEditor(
    context: Context, attr: AttributeSet? = null
) : AppCompatEditText(context, attr) {

    private val schemes: MutableList<ColorScheme> = mutableListOf()

    init {
        setBackgroundColor(getContext().getCompatColor(R.color.bg_editor))

        schemes.add(
            ColorScheme(
                "(?<!\\w)(true|false)(?!\\w)",
                getContext().getCompatColor(R.color.bool)
            )
        )
        schemes.add(
            ColorScheme(
                "\"[^\"]*\"",
                getContext().getCompatColor(R.color.string)
            )
        )

        schemes.add(
            ColorScheme(
                "[/]{2}.*",
                getContext().getCompatColor(R.color.comment)
            )
        )
    }


    override fun onTextChanged(
        text: CharSequence?,
        start: Int,
        lengthBefore: Int,
        lengthAfter: Int
    ) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter)

        Handler(Looper.getMainLooper()).post {
            highlight();
        }

        text?.let {

            val count = lengthAfter - lengthBefore
            val selection = selectionEnd

            if (count == 1) {
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

    private fun highlight() {
        text?.let { editable ->

            setSpanDefault(editable)

            for (scheme in schemes) {
                val mather: Matcher = scheme.pattern.matcher(editable)
                while (mather.find()) {
                    editable.setSpan(
                        ForegroundColorSpan(scheme.color),
                        mather.start(), mather.end(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            }
        }
    }

    private fun setSpanDefault(editable: Editable) {
        editable.setSpan(
            ForegroundColorSpan(
                ContextCompat.getColor(
                    context, R.color.others
                )
            ), 0, editable.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }

}