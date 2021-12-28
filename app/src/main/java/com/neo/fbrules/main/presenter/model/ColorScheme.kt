package com.neo.fbrules.main.presenter.model

import androidx.annotation.ColorInt
import java.util.regex.Pattern

class ColorScheme(
    regex: String,
    @ColorInt val color: Int
) {
    val pattern: Pattern = Pattern.compile(regex)
}