package com.neo.fbrules.util

import android.view.View
import android.view.ViewGroup

fun View.visibility(visible: Boolean) {
    visibility = if (visible) View.VISIBLE else View.GONE
}
