package com.neo.fbrules.main.presenter.fragment.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import com.neo.fbrules.R
import com.neo.fbrules.databinding.DialogInsertPasswordBinding
import com.neo.fbrules.util.AES
import com.neo.fbrules.util.showSnackbar

private typealias EncryptionDialogView = DialogInsertPasswordBinding

class EncryptionDialog(
    private val content: String,
    private val mode: MODE,
    private val success: (String) -> Unit = { }
) : DialogFragment() {

    private lateinit var binding: EncryptionDialogView

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val alert = AlertDialog.Builder(requireContext())

        when (mode) {
            MODE.DECRYPT -> {
                alert.setTitle(getString(R.string.text_config_decrypt_title))
                alert.setMessage(getString(R.string.text_config_decrypt_message))
            }
            MODE.ENCRYPT -> {
                alert.setTitle(getString(R.string.text_config_encrypt_title))
                alert.setMessage(getString(R.string.text_config_encrypt_message))
            }
        }

        binding = EncryptionDialogView.inflate(layoutInflater, null, false)
        alert.setView(binding.root)

        binding.password.editText?.addTextChangedListener {
            val value = it?.toString()

            when {
                value.isNullOrBlank() -> {
                    binding.password.error = getString(R.string.text_digite_password)
                }
                value.length < 4 -> {
                    binding.password.error = getString(R.string.text_error_little_password)
                }
                else -> {
                    binding.password.isErrorEnabled = false
                }
            }
        }

        binding.confirmBtn.text = when (mode) {
            MODE.DECRYPT -> getString(R.string.text_recovery)
            MODE.ENCRYPT -> getString(R.string.text_save)
        }

        binding.confirmBtn.setOnClickListener {
            validate()
        }

        return alert.create()
    }

    private fun validate() {
        val password = binding.password.editText?.text?.toString()

        if (password.isNullOrBlank()) {
            binding.password.error = getString(R.string.text_digite_password)
            return
        }

        if (password.length < 4) {
            binding.password.error = getString(R.string.text_error_little_password)
            return
        }

        when (mode) {
            MODE.ENCRYPT -> {
                encrypt(password)
            }

            MODE.DECRYPT -> {
                decrypt(password)
            }
        }
    }

    private fun encrypt(password: String) {

        val encrypted = AES.encrypt(content, password)

        success.invoke(encrypted)
        dismiss()
    }

    private fun decrypt(password: String) {

        val decrypted = AES.decrypt(content, password)

        if (decrypted == null) {
            showSnackbar(binding.root, getString(R.string.text_error_password_different))
            return
        }

        success.invoke(decrypted)
        dismiss()
    }

    enum class MODE {
        ENCRYPT,
        DECRYPT
    }

    companion object {
        val tag = EncryptionDialog::class.simpleName
    }
}
