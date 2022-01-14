package com.neo.fbrules.core

import java.util.regex.Pattern

object Expression {
    val variable: Pattern = Pattern.compile("((?<=/)|^)\\$\\w+(?=/?)$")
}