package com.neo.fbrules.util

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.view.View
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.neo.fbrules.R
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import androidx.core.content.ContextCompat.startActivity


//ALERT DIALOG

//alert dialog activity

/**
 * @author Irineu A. Silva
 */
fun Activity.showAlertDialog(
    title: String, message: String, config: AlertDialogConfig.() -> Unit
) = showAlertDialog(this, title, message, config)

//alert dialog fragment

/**
 * @author Irineu A. Silva
 */
fun Fragment.showAlertDialog(
    title: String, message: String, config: AlertDialogConfig.() -> Unit = {}
) = showAlertDialog(requireContext(), title, message, config)

//alert dialog global

/**
 * @author Irineu A. Silva
 */
fun showAlertDialog(
    context: Context,
    title: String,
    message: String,
    config: AlertDialogConfig.() -> Unit
): AlertDialog {

    //BUILD
    val build = AlertDialog.Builder(context)

    build.setTitle(title)
    build.setMessage(message)

    config.invoke(AlertDialogConfig(build))

    //CREATE AND SHOW
    return build.show()
}

/**
 * @author Irineu A. Silva
 */
class AlertDialogConfig(val build: AlertDialog.Builder) {
    fun positiveButton(text: String = "OK", function: () -> Unit = {}) {
        build.setPositiveButton(text) { _, _ ->
            function.invoke()
        }
    }

    fun negativeButton(text: String = "Não", function: () -> Unit = {}) {
        build.setNegativeButton(text) { _, _ ->
            function.invoke()
        }
    }

    fun addOnDismiss(function: () -> Unit) {
        build.setOnDismissListener {
            function.invoke()
        }
    }
}

//SNACKBAR

//activity snackbar

//snackbar global

/**
 * @author Irineu A. Silva
 */
fun showSnackbar(view: View, text: String, config: SnackbarConfig.() -> Unit = {}) {

    //BUILD
    val build = Snackbar.make(view, text, Snackbar.LENGTH_SHORT)

    config.invoke(SnackbarConfig(build))

    build.show()
}

/**
 * @author Irineu A. Silva
 */
class SnackbarConfig(private val build: Snackbar) {
    fun addOnDismiss(function: () -> Unit = {}) {
        build.addCallback(
            object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                    function.invoke()
                }
            }
        )
    }
}

//NETWORK

/**
 * @author Irineu A. Silva
 */
fun ResponseBody.errorMessage(): String {
    val jsonString = this.charStream().readText()
    return JSONObject(jsonString).getString("error")
}

/**
 * @author Irineu A. Silva
 */
fun String.toRequestBody(): RequestBody {
    return RequestBody.create(
        okhttp3.MediaType.parse("application/json; charset=utf-8"), this
    )
}

//DPI

fun Fragment.dip(int: Int): Int {
    return (requireContext().resources.getDimension(R.dimen.dimen_1dp) * int).toInt()
}


//COMPAT

fun Context.getCompatColor(@ColorRes colorResId: Int): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        this.getColor(colorResId)
    } else {
        ContextCompat.getColor(this, colorResId)
    }
}

//LINK

fun goToUrl(context: Context, url: String) {
    runCatching {
        Firebase.crashlytics.log("opening url $url")
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }.onFailure {
        showAlertDialog(
            context,
            "Impossível abrir link",
            "Ocorreu um erro ao tentar abrir o link \"$url\""
        ) {
            positiveButton()
        }
        Firebase.crashlytics.recordException(it)
    }
}

fun goToApp(context: Context, packageName: String) {
    context.packageManager.getLaunchIntentForPackage(packageName)?.let {
        context.startActivity(it)
    }
}

fun Activity.goToUrl(url: String) = goToUrl(this, url)