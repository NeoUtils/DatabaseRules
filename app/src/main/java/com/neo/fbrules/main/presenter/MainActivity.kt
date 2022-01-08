package com.neo.fbrules.main.presenter

import android.annotation.SuppressLint
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
import com.neo.fbrules.core.HistoricTextWatcher
import com.neo.fbrules.main.presenter.adapter.NeoUtilsAppsAdapter
import com.neo.fbrules.main.presenter.model.Update
import com.neo.fbrules.util.requestColor
import com.neo.fbrules.util.goToUrl
import com.neo.fbrules.util.visibility

private typealias MainActivityView = ActivityMainBinding

@AndroidEntryPoint
class MainActivity : BaseActivity<MainActivityView>() {

    private val viewModel: MainViewModel by viewModels()

    private val neoUtilsAppsAdapter: NeoUtilsAppsAdapter by neoUtilsAppsAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = MainActivityView.inflate(layoutInflater)
        setContentView(binding.root)

        init()
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()

        neoUtilsAppsAdapter.notifyDataSetChanged()
    }

    private fun init() {
        setupObservers()
        setupViews()
        setupListeners()

        viewModel.checkUpdate()
        viewModel.loadNeoUtilsApps()
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

        setupHistoric()
    }

    private fun setupHistoric() {
        val historyObserver = HistoricTextWatcher(viewModel.historic)

        historyObserver.historyListener = object : HistoricTextWatcher.HistoryListener {
            override fun hasUndo(has: Boolean) {
                binding.content.ibUndoBtn.isClickable = has
                binding.content.ibUndoBtn.alpha = if (has) 1f else 0.5f
            }

            override fun hasRedo(has: Boolean) {
                binding.content.ibRedoBtn.isClickable = has
                binding.content.ibRedoBtn.alpha = if (has) 1f else 0.5f
            }

            override fun update(history: Pair<Int, String>) {
                binding.content.rulesEditor.removeTextChangedListener(historyObserver)
                binding.content.rulesEditor.setText(history.second)
                binding.content.rulesEditor.setSelection(history.first)
                binding.content.rulesEditor.addTextChangedListener(historyObserver)
            }
        }

        binding.content.rulesEditor.addTextChangedListener(historyObserver)

        binding.content.ibUndoBtn.setOnClickListener {
            historyObserver.undo()
        }

        binding.content.ibRedoBtn.setOnClickListener {
            historyObserver.redo()
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

        binding.navBar.rvNeoUtilsApps.adapter = neoUtilsAppsAdapter
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

        viewModel.apps.observe(this) { apps ->
            if (apps.isEmpty()) {
                binding.navBar.rvNeoUtilsApps.visibility(false)
                binding.navBar.vDiv.visibility(false)
            } else {
                binding.navBar.rvNeoUtilsApps.visibility(true)
                binding.navBar.vDiv.visibility(true)

                neoUtilsAppsAdapter.setApps(apps)
            }
        }
    }

    private fun changeUpdateNotice(update: Update) = with(binding.navBar) {
        val stateVisibility = update.hasUpdate != null

        if (stateVisibility) {

            val hasUpdate = update.hasUpdate == true

            if (hasUpdate) {
                changeHasUpdate(update)
            } else {
                changeHasNotUpdate()
            }

            tvUpdateBtn.visibility(hasUpdate)
        }

        cdUpdate.visibility(stateVisibility)
    }

    private fun changeHasNotUpdate() = with(binding.navBar) {
        ivIcon.setImageResource(R.drawable.ic_checked)

        requestColor(R.color.green).let { color ->
            ivIcon.setColorFilter(color)
            tvLastVersion.setTextColor(color)
        }

        val version = "v" + BuildConfig.VERSION_NAME
        tvLastVersion.text = version

        tvMessage.text = getString(R.string.text_drawer_updated)

        cdUpdate.setOnClickListener(null)
    }

    private fun changeHasUpdate(update: Update) = with(binding.navBar) {
        ivIcon.setImageResource(R.drawable.ic_has_update)

        requestColor(R.color.yellow).let { color ->
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
                param("context", "Nova atualização")
                param("text", tvLastVersion.text.toString())
                param("type", "button")
            }

            goToUrl(downloadLink!!)
        }

        if (update.force) {
            showAlertDialog(
                "Atualização obrigatória",
                "Versão ${update.lastVersionName} disponível. Por favor atualize para continuar usando."
            ) {

                build.setCancelable(false)

                positiveButton("Atualizar") {

                    Firebase.analytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT) {
                        param("context", "Atualização obrigatória")
                        param("text", "Atualizar")
                        param("type", "button")
                    }

                    goToUrl(update.downloadLink!!)
                    finish()
                }
            }
        }
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

    private fun neoUtilsAppsAdapter() = lazy {
        NeoUtilsAppsAdapter()
    }
}