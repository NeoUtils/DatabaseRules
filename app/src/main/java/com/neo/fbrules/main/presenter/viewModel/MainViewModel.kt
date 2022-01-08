package com.neo.fbrules.main.presenter.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neo.fbrules.core.*
import com.neo.fbrules.main.domain.model.DomainCredential
import com.neo.fbrules.main.domain.useCase.*
import com.neo.fbrules.main.presenter.model.NeoUtilsApp
import com.neo.fbrules.main.presenter.model.Update
import com.neo.fbrules.util.environment
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getRules: GetRules,
    private val setRules: SetRules
) : ViewModel() {

    var credential: DomainCredential? = null

    //OBSERVABLES

    //default
    private val _rules = MutableLiveData<String>()
    val rules : LiveData<String> get() = _rules

    val error = MutableSingleLiveData<Result.Error>()

    val alert = MutableSingleLiveData<Message>()

    val message = MutableSingleLiveData<Message>()

    private val _update = MutableLiveData(Update())
    val update : LiveData<Update> = _update

    private val _apps = MutableLiveData(listOf<NeoUtilsApp>())
    val apps : LiveData<List<NeoUtilsApp>> = _apps

    val loading = MutableLiveData(false)

    //especial
    val configBottomSheet = MutableSingleLiveData<() -> Unit>()
    val decryptBottomSheet = MutableSingleLiveData<Unit>()

    init {
        decryptBottomSheet.setValue(Unit)

        if (environment == "development") {
            credential = DomainCredential(
                databaseKey = "test-81a49-default-rtdb",
                privateKey = "eJiSOixN5fIR2lacQKI2e9MB4njVMDg2ymUlXaml"
            )
        }
    }

    //actions
    fun pullRules() {
        viewModelScope.launch {
            when (
                val response = verifyCredential { credential ->
                    loading.postValue(true)
                    getRules(credential)
                }
            ) {
                is Result.Success -> {
                    val data = response.data
                    _rules.postValue(data)
                }

                is Result.Error -> {

                    when (response.type) {
                        Constants.ERROR.CREDENTIAL_NOT_FOUND -> {
                            configBottomSheet.postValue {
                                pullRules()
                            }
                        }

                        else -> {
                            error.postValue(
                                Result.Error(
                                    response.type,
                                    response.title,
                                    response.message
                                )
                            )
                        }
                    }
                }
            }

            loading.postValue(false)
        }
    }

    fun pushRules(rules: String?) {

        if (rules == null) {
            error.setValue(Result.Error(message = "rules is null"))
            return
        }

        viewModelScope.launch {
            when (
                val response = verifyCredential { credential ->
                    loading.postValue(true)
                    setRules(rules, credential)
                }
            ) {
                is Result.Success -> {
                    message.postValue(
                        Message(
                            title = "Success",
                            message = "Regras atualizadas!"
                        )
                    )
                }

                is Result.Error -> {

                    when (response.type) {
                        Constants.ERROR.CREDENTIAL_NOT_FOUND -> {
                            configBottomSheet.postValue {
                                pushRules(rules)
                            }
                        }

                        else -> {
                            error.postValue(response)
                        }
                    }
                }
            }

            loading.postValue(false)
        }
    }

    private suspend fun <T> verifyCredential(
        function: suspend (DomainCredential) -> Result<T>
    ): Result<T> {
        return if (credential == null) {
            Result.Error(Constants.ERROR.CREDENTIAL_NOT_FOUND)
        } else {
            function.invoke(credential!!)
        }
    }

    fun openConfig() {
        configBottomSheet.setValue {}
    }

    fun checkUpdate() {
        UpdateManager(object : UpdateManager.UpdateListener {
            override fun updated() {
                _update.value = Update(
                    hasUpdate = false
                )
            }

            override fun hasUpdate(
                lastVersionCode: Int,
                lastVersionName: String,
                downloadLink: String,
                force: Boolean
            ) {
                _update.value = Update(
                    hasUpdate = true,
                    lastVersionCode = lastVersionCode,
                    lastVersionName = lastVersionName,
                    downloadLink = downloadLink,
                    force = force
                )
            }
        })
    }

    fun loadNeoUtilsApps() {
        NeoUtilsAppsManager(object : NeoUtilsAppsManager.AppsListener {
            override fun change(apps: List<NeoUtilsApp>) {
                this@MainViewModel._apps.value = apps
            }
        })
    }
}