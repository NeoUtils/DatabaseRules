package com.neo.fbrules.core

import android.app.Dialog
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.neo.fbrules.util.SnackbarConfig
import com.neo.fbrules.util.showSnackbar

abstract class BaseActivity<T : ViewBinding> : AppCompatActivity() {

    protected lateinit var binding: T

    //UTILS
    protected fun showSnackbar(
        message: String,
        config: SnackbarConfig.() -> Unit = {}
    ) = showSnackbar(binding.root, message, config)

    fun showLoading() {
        dialog = Dialog(this).apply {
            setContentView(ProgressBar(this@BaseActivity))
            setCancelable(false)
            window!!.setBackgroundDrawableResource(android.R.color.transparent)
            show()
        }
    }

    fun hideLoading() {
        dialog?.dismiss()
    }

    companion object{
        private var dialog : Dialog? = null
    }
}