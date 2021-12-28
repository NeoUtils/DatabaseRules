package com.neo.fbrules.main.presenter.fragment

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.neo.fbrules.R
import com.neo.fbrules.databinding.DialogConfigBinding
import com.neo.fbrules.main.domain.model.DomainCredential
import com.neo.fbrules.util.*
import java.util.regex.Pattern

private typealias ConfigBottomSheetView = DialogConfigBinding

class ConfigBottomSheet(
    private var credential: DomainCredential? = null,
    private val update: (DomainCredential) -> Unit = { }
) : BottomSheetDialogFragment() {

    private lateinit var binding: ConfigBottomSheetView

    private val regex = Pattern.compile(".*(http)[s]?:[/]{2}|.firebaseio.com[/]?.*")

    override fun getTheme(): Int {
        return R.style.Theme_NeoFirebase_BottomSheet
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ConfigBottomSheetView.inflate(
            layoutInflater, container, false
        )

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val shared = requireContext().getSharedPreferences("CONFIG", Context.MODE_PRIVATE)

        val privateKeyEncrypted = shared.getString("private_key", null)
        val databaseKey = shared.getString("database_key", null)

        val saveCredentials = shared.getBoolean("SAVE_CREDENTIALS", false)

        setupViews(saveCredentials, privateKeyEncrypted == null)
        setupClicks(privateKeyEncrypted, databaseKey)
        setupListeners()
    }

    private fun setupClicks(privateKeyEncrypted: String?, databaseKey: String?) {

        binding.savedCredential.setOnClickListener {
            val dialog = EncryptionDialog(privateKeyEncrypted!!, EncryptionDialog.MODE.DECRYPT) {
                binding.privateKey.editText?.setText(it)
                binding.databaseKey.editText?.setText(databaseKey!!)
                credential = DomainCredential(
                    privateKey = it,
                    databaseKey = databaseKey!!
                )
                showSnackbar(binding.root, "Sucesso!!")
            }

            dialog.show(parentFragmentManager, EncryptionDialog.tag)
        }

        binding.confirmBtn.setOnClickListener { validate() }

        binding.help.setOnClickListener {
            showAlertDialog(
                getString(R.string.text_config_save_credential_help_title),
                getString(R.string.text_config_save_credential_help_message)
            ) {
                positiveButton()
            }
        }
    }

    private fun setupViews(saveCredentials: Boolean, savedCredentials: Boolean) {

        binding.saveCredentials.isChecked = saveCredentials

        credential?.let {
            binding.databaseKey.editText?.setText(it.databaseKey)
            binding.privateKey.editText?.setText(it.privateKey)
        }

        binding.savedCredential.visibility =
            when (savedCredentials) {
                true -> View.GONE
                false -> View.VISIBLE
            }
    }

    private fun setupListeners() {

        binding.saveCredentials.setOnCheckedChangeListener { _, isChecked ->
            requireContext().getSharedPreferences("CONFIG", Context.MODE_PRIVATE)
                .edit().apply {
                    putBoolean("SAVE_CREDENTIALS", isChecked)
                    apply()
                }
        }

        binding.databaseKey.editText?.addTextChangedListener {
            val text = it?.toString()

            if (text.isNullOrBlank()) {
                binding.databaseKey.error = "Digite o banco de dados"
            } else {
                binding.databaseKey.isErrorEnabled = false
                databaseKeyHighlight(it)
            }
        }

        binding.privateKey.editText?.addTextChangedListener {
            val text = it?.toString()

            if (text.isNullOrBlank()) {
                binding.privateKey.error = "Digite a chave privada"
            } else {
                binding.privateKey.isErrorEnabled = false
            }
        }
    }

    private fun databaseKeyHighlight(it: Editable) {
        setDefaultSpan()

        val matcher = regex.matcher(it.toString())

        while (matcher.find()) {

            it.setSpan(
                ForegroundColorSpan(Color.parseColor("#868C8C8C")),
                matcher.start(), matcher.end(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

    private fun setDefaultSpan() {
        binding.databaseKey.editText?.text?.apply {
            setSpan(
                ForegroundColorSpan(requireContext().getCompatColor(R.color.colorOnPrimary)),
                0,
                length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

    private fun validate() {
        var isValid = true
        val privateKey = binding.privateKey.editText?.text?.toString()
        var databaseKey = binding.databaseKey.editText?.text?.toString()

        if (databaseKey.isNullOrBlank()) {
            binding.databaseKey.error = "Digite o banco de dados"
            isValid = false
        }

        if (privateKey.isNullOrBlank()) {
            binding.privateKey.error = "Digite a chave privada"
            isValid = false
        }

        if (!isValid) return

        val matcher = regex.matcher(databaseKey!!)
        val isUrl = matcher.find()

        databaseKey = matcher.replaceAll("")


        if (isUrl) {
            showAlertDialog(
                title = getString(R.string.text_config_confirm_key_title),
                message = getString(R.string.text_config_confirm_key_message) + "\n\nkey: $databaseKey"
            ) {
                positiveButton {
                    confirm(privateKey!!, databaseKey)
                }

                negativeButton("Está errada")
            }
        } else {
            confirm(privateKey!!, databaseKey)
        }
    }

    private fun confirm(privateKey: String, databaseKey: String) {

        val confirm = {
            update.invoke(
                DomainCredential(
                    privateKey = privateKey,
                    databaseKey = databaseKey
                )
            )
            dismiss()
        }

        val isDiff = credential?.privateKey != privateKey ||
                credential?.databaseKey != databaseKey

        if (
            binding.saveCredentials.isChecked &&
            isDiff
        ) {
            val dialog = EncryptionDialog(
                privateKey,
                EncryptionDialog.MODE.ENCRYPT
            ) { encrypted ->

                requireContext().getSharedPreferences(
                    "CONFIG", Context.MODE_PRIVATE
                ).edit().apply {
                    putString("private_key", encrypted)
                    putString("database_key", databaseKey)
                    apply()
                }

                confirm.invoke()
            }
            dialog.show(parentFragmentManager, EncryptionDialog.tag)
        } else {
            confirm.invoke()
        }

    }

    companion object {
        val tag = ConfigBottomSheet::class.simpleName
    }

}

