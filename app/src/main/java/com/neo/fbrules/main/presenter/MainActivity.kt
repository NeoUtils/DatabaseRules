package com.neo.fbrules.main.presenter

import android.os.Bundle
import androidx.activity.viewModels
import com.neo.fbrules.core.BaseActivity
import com.neo.fbrules.databinding.ActivityMainBinding
import com.neo.fbrules.main.domain.model.DomainCredential
import com.neo.fbrules.main.presenter.fragment.ConfigBottomSheet
import com.neo.fbrules.main.presenter.fragment.EncryptionDialog
import com.neo.fbrules.main.presenter.viewModel.MainViewModel
import com.neo.fbrules.util.showAlertDialog
import dagger.hilt.android.AndroidEntryPoint
import androidx.appcompat.app.ActionBarDrawerToggle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import com.neo.fbrules.BuildConfig
import com.neo.fbrules.R
import com.neo.fbrules.main.presenter.model.Update
import com.neo.fbrules.util.color
import com.neo.fbrules.util.goToUrl
import com.neo.fbrules.util.visibility

private typealias MainActivityView = ActivityMainBinding

@AndroidEntryPoint
class MainActivity : BaseActivity<MainActivityView>() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = MainActivityView.inflate(layoutInflater)
        setContentView(binding.root)

        init()

    }

    private fun init() {
        setupObservers()
        setupViews()
        setupListeners()

        viewModel.checkUpdate()
    }

    private fun setupListeners() {
        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.pull -> {
                    viewModel.pullRules()
                    true
                }

                R.id.push -> {
                    val rules = binding.content.rulesEditor.editableText?.toString()
                    viewModel.pushRules(rules)
                    true
                }

                R.id.config -> {
                    viewModel.openConfig()
                    true
                }

                else -> false
            }
        }
    }

    private fun setupViews() = with(binding) {
        drawerLayout.apply {
            val toggle = ActionBarDrawerToggle(
                this@MainActivity,
                this,
                binding.toolbar,
                R.string.text_drawer_open,
                R.string.text_drawer_close
            )
            addDrawerListener(toggle)
            toggle.syncState()
        }
    }

    private fun setupObservers() {

        viewModel.rules.observe(this) { rules ->
            binding.content.rulesEditor.setText(rules)
        }

        viewModel.error.singleObserve(this) { error ->
            showAlertDialog(error.title, error.message) {
                positiveButton()
            }
        }

        viewModel.alert.singleObserve(this) { alert ->
            showAlertDialog(alert.title, alert.message) {
                positiveButton()
            }
        }

        viewModel.message.singleObserve(this) { message ->
            showSnackbar(message.message)
        }

        viewModel.loading.observe(this) { show ->
            if (show) {
                showLoading()
            } else {
                hideLoading()
            }
        }
        viewModel.configBottomSheet.singleObserve(this) { request ->
            showConfigBottomSheet(request)
        }

        viewModel.decryptBottomSheet.singleObserve(this) {
            showDecryptDialog()
        }

        viewModel.update.observe(this) { update ->
            changeUpdateNotice(update)
        }
    }

    private fun changeUpdateNotice(update: Update) = with(binding.navBar) {
        val visible = update.hasUpdate != null

        if (visible) {

            val hasUpdate = update.hasUpdate == true

            if (hasUpdate) {
                ivIcon.setImageResource(R.drawable.ic_has_update)

                color(R.color.yellow).let { color ->
                    ivIcon.setColorFilter(color)
                    tvLastVersion.setTextColor(color)
                }

                val version = "v" + update.lastVersionName!!
                tvLastVersion.text = version

                tvMessage.text = getString(R.string.text_drawer_has_update)

                cdUpdate.setOnClickListener {
                    val downloadLink = update.downloadLink

                    Firebase.analytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT) {
                        param(FirebaseAnalytics.Param.ITEM_ID, it.id.toString())
                        param(
                            FirebaseAnalytics.Param.ITEM_NAME,
                            tvLastVersion.text.toString()
                        )
                        param(FirebaseAnalytics.Param.CONTENT_TYPE, "button")
                    }

                    goToUrl(downloadLink!!)
                }

                //showUpdateDialog(update)

            } else {
                ivIcon.setImageResource(R.drawable.ic_checked)

                color(R.color.green).let { color ->
                    ivIcon.setColorFilter(color)
                    tvLastVersion.setTextColor(color)
                }

                val version = "v" + BuildConfig.VERSION_NAME
                tvLastVersion.text = version

                tvMessage.text = getString(R.string.text_drawer_updated)

                cdUpdate.setOnClickListener(null)
            }

            tvUpdateBtn.visibility(hasUpdate)
        }

        cdUpdate.visibility(visible)
    }

    private fun showDecryptDialog() {
        val sharedPreferences = getSharedPreferences("CONFIG", MODE_PRIVATE)

        val privateKeyEncrypted = sharedPreferences.getString("private_key", null)
        val databaseKey = sharedPreferences.getString("database_key", null)

        if (privateKeyEncrypted.isNullOrBlank() || databaseKey.isNullOrBlank()) {
            viewModel.configBottomSheet.postValue { }
            return
        }

        val encryptionDialog = EncryptionDialog(
            privateKeyEncrypted,
            EncryptionDialog.MODE.DECRYPT
        ) { decrypted ->
            viewModel.credential = DomainCredential(
                privateKey = decrypted,
                databaseKey = databaseKey
            )

            showSnackbar("Sucesso!!")
        }

        encryptionDialog.show(supportFragmentManager, EncryptionDialog.tag)
    }

    private fun showConfigBottomSheet(request: () -> Unit) {
        val configBottomSheet = ConfigBottomSheet(viewModel.credential) { credential ->
            viewModel.credential = credential
            request.invoke()
            showSnackbar("Sucesso!!")
        }

        configBottomSheet.show(supportFragmentManager, "config_dialog")
    }
}
