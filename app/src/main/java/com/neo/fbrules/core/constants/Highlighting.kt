package com.neo.fbrules.core.constants

import android.content.Context
import com.neo.fbrules.R
import com.neo.fbrules.util.getCompatColor
import com.neo.fbrules.util.requestColor
import com.neo.highlight.util.scheme.ColorScheme
import java.util.regex.Pattern

class Highlighting(context: Context) {

    val propertySyntax = listOf(
        ColorScheme(
            Pattern.compile("(\\.read|\\.write)"),
            context.theme.requestColor(R.attr.colorAccent)
        )
    )

    val conditionSyntax = listOf(
        ColorScheme(
            Pattern.compile("(?<=auth\\.)uid|auth"),
            context.theme.requestColor(R.attr.colorAccent)
        ),
        ColorScheme(
            Pattern.compile("===|==|!="),
            context.theme.requestColor(R.attr.colorPrimary)
        ),
        ColorScheme(
            Pattern.compile("(true|false|null)"),
            context.requestColor(R.color.syntax_literal)
        ),
        ColorScheme(
            Pattern.compile("(\"[^\"]*\")|('[^']*')"),
            context.requestColor(R.color.syntax_string)
        )
    )

    val completeSyntax = listOf(
        propertySyntax[0],
        ColorScheme(
            Pattern.compile("(?<!\\w)(true|false|null)(?!\\w)"),
            context.getCompatColor(R.color.syntax_literal)
        ),
        ColorScheme(
            Pattern.compile("\"[^\"]*\""),
            context.getCompatColor(R.color.syntax_string)
        ),
        ColorScheme(
            Pattern.compile("[/]{2}.*"),
            context.getCompatColor(R.color.comment)
        ),
        ColorScheme(
            Pattern.compile("(?<=auth\\.)uid|auth"),
            context.theme.requestColor(R.attr.colorAccent)
        ),
        ColorScheme(
            Pattern.compile("===|==|!="),
            context.theme.requestColor(R.attr.colorPrimary)
        ),
        ColorScheme(
            Pattern.compile("\\$\\w+"),
            context.requestColor(R.color.syntax_variable)
        )
    )
}