package com.neo.fbrules.main.presenter.fragment.bottomSheet

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.neo.fbrules.R
import com.neo.fbrules.databinding.DialogConfigCredentialsBinding
import com.neo.fbrules.main.domain.model.DomainCredential
import com.neo.fbrules.main.presenter.fragment.dialog.EncryptionDialog
import com.neo.fbrules.util.*
import com.neo.highlight.core.Highlight
import com.neo.highlight.util.scheme.OnClickScheme
import java.util.regex.Pattern

private typealias ConfigBottomSheetView = DialogConfigCredentialsBinding

class ConfigBottomSheet(
    private var credential: DomainCredential? = null,
    private val update: (DomainCredential) -> Unit = { }
) : BottomSheetDialogFragment() {

    private lateinit var binding: ConfigBottomSheetView

    private val regex = Pattern.compile(".*(http)[s]?:[/]{2}|.firebaseio.com[/]?.*")

    private val helpListeners = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {

            if (snapshot.exists()) {

                val highlight = Highlight(
                    listOf(
                        OnClickScheme(
                            Pattern.compile("(Obter ajuda)|(Get help)")
                        ) { _, _, _ ->
                            runCatching {
                                goToUrl(requireContext(), snapshot.value as String)
                            }.onFailure {
                                Firebase.crashlytics.recordException(it)
                            }
                        }.apply {
                            setPainTextUnderline(true)
                            setPainText(true)
                        }
                    )
                )

                binding.configHelp.visibility(true)

                binding.configHelpMessage.text =
                    highlight.getSpannable(getString(R.string.text_config_get_help))
            }
        }

        override fun onCancelled(error: DatabaseError) = Unit
    }

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

    override fun onDestroyView() {
        super.onDestroyView()

        firebaseEnvironment.child("config")
            .removeEventListener(helpListeners)

        firebaseEnvironment.child("config-pt")
            .removeEventListener(helpListeners)
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
                showSnackbar(binding.root, getString(R.string.text_alert_success))
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

        behavior?.state = BottomSheetBehavior.STATE_EXPANDED

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

        val languageConfigHelp =
            if (requireContext().resources.getBoolean(R.bool.english)) "config" else "config-pt"

        firebaseEnvironment.child(languageConfigHelp)
            .addListenerForSingleValueEvent(helpListeners)


        binding.configHelpMessage.movementMethod = LinkMovementMethod.getInstance()
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
                binding.databaseKey.error = getString(R.string.text_config_digite_database)
            } else {
                binding.databaseKey.isErrorEnabled = false
                databaseKeyHighlight(it)
            }
        }

        binding.privateKey.editText?.addTextChangedListener {
            val text = it?.toString()

            if (text.isNullOrBlank()) {
                binding.privateKey.error = getString(R.string.text_config_digite_private_key)
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
            binding.databaseKey.error = getString(R.string.text_config_digite_database)
            isValid = false
        }

        if (privateKey.isNullOrBlank()) {
            binding.privateKey.error = getString(R.string.text_config_digite_private_key)
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

                negativeButton(getString(R.string.button_config_is_wrong))
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

