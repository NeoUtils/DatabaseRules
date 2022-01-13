package com.neo.fbrules.util

import com.neo.fbrules.R
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

fun Context.dp(size: Int): Int = dp(size.toFloat()).toInt()
fun Fragment.dp(size: Int) = requireContext().dp(size)

fun Context.dp(size: Float): Float = size * resources.getDimension(R.dimen.dimen_1dp)
fun Fragment.dp(size: Float) = requireContext().dp(size)

@ColorInt
fun Resources.Theme.requestColor(colorRes: Int): Int {
    val typedValue = TypedValue()
    this.resolveAttribute(colorRes, typedValue, true)
    return typedValue.data
}

@ColorInt
fun Context.requestColor(@ColorRes colorRes: Int) = ContextCompat.getColor(this, colorRes)

@ColorInt
fun Fragment.requestColor(@ColorRes colorRes: Int) = requireContext().requestColor(colorRes)

fun runOnMainThread(delay: Long = 0, function: () -> Unit) {
    Handler(Looper.getMainLooper()).postDelayed(function, delay)
}

val BottomSheetDialogFragment.behavior get() = (this.dialog as? BottomSheetDialog)?.behavior

fun isInstalled(
    packageName: String,
    packageManager: PackageManager
): Boolean {

    return runCatching {
        packageManager.getPackageInfo(packageName, 0)
        true
    }.getOrElse {
        false
    }
}