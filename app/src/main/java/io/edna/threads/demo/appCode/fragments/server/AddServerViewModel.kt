package io.edna.threads.demo.appCode.fragments.server

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.text.Editable
import android.view.View
import androidx.databinding.Bindable
import androidx.databinding.Observable
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import androidx.navigation.findNavController
import io.edna.threads.demo.R
import io.edna.threads.demo.appCode.business.AfterTextChangedTextWatcher
import io.edna.threads.demo.appCode.business.StringsProvider
import io.edna.threads.demo.appCode.models.ServerConfig
import org.parceler.Parcels

class AddServerViewModel(
    private val stringsProvider: StringsProvider
) : ViewModel(), DefaultLifecycleObserver, Observable {

    private var srcServerConfig: ServerConfig? = null
    private var _serverConfigLiveData = MutableLiveData(ServerConfig())
    var finalServerConfigLiveData = MutableLiveData<ServerConfig>(null)
    var serverConfigLiveData: LiveData<ServerConfig> = _serverConfigLiveData

    private var _errorStringForServerNameFieldLiveData = MutableLiveData<String?>(null)
    var errorStringForServerNameFieldLiveData: LiveData<String?> =
        _errorStringForServerNameFieldLiveData

    private var _errorStringForProviderIdFieldLiveData = MutableLiveData<String?>(null)
    var errorStringForProviderIdFieldLiveData: LiveData<String?> =
        _errorStringForProviderIdFieldLiveData

    private var _errorStringForBaseUrlFieldLiveData = MutableLiveData<String?>(null)
    var errorStringForBaseUrlFieldLiveData: LiveData<String?> = _errorStringForBaseUrlFieldLiveData

    private var _errorStringForDatastoreUrlFieldLiveData = MutableLiveData<String?>(null)
    var errorStringForDatastoreUrlFieldLiveData: LiveData<String?> =
        _errorStringForDatastoreUrlFieldLiveData

    private var _errorStringForThreadsGateUrlFieldLiveData = MutableLiveData<String?>(null)
    var errorStringForThreadsGateUrlFieldLiveData: LiveData<String?> =
        _errorStringForThreadsGateUrlFieldLiveData

    fun click(view: View) {
        val navigationController: NavController =
            (view.context as Activity).findNavController(R.id.nav_host_fragment_content_main)
        when (view.id) {
            R.id.backButton -> navigationController.navigate(R.id.action_AddServerFragment_to_ServerListFragment)
            R.id.okButton -> {
                if (serverConfigLiveData.value?.isAllFieldsFilled() == true) {
                    finalServerConfigLiveData.postValue(serverConfigLiveData.value)
                    navigationController.navigate(R.id.action_AddServerFragment_to_ServerListFragment)
                } else {
                    setupErrorFields(serverConfigLiveData.value)
                }
            }
        }
    }

    fun initData(arguments: Bundle?) {
        if (arguments != null && arguments.containsKey(ServerListFragment.SERVER_CONFIG_KEY)) {
            val config: ServerConfig? = if (Build.VERSION.SDK_INT >= 33) {
                Parcels.unwrap(
                    arguments.getParcelable(
                        ServerListFragment.SERVER_CONFIG_KEY,
                        Parcelable::class.java
                    )
                )
            } else {
                Parcels.unwrap(arguments.getParcelable(ServerListFragment.SERVER_CONFIG_KEY))
            }
            setSrcConfig(config)
        }
    }

    private fun setSrcConfig(config: ServerConfig?) {
        if (config != null) {
            srcServerConfig = config
            _serverConfigLiveData.postValue(config.copy())
        }
    }

    private fun setupErrorFields(config: ServerConfig?) {
        if (config == null) {
            _errorStringForServerNameFieldLiveData.postValue(stringsProvider.requiredField)
            _errorStringForProviderIdFieldLiveData.postValue(stringsProvider.requiredField)
            _errorStringForBaseUrlFieldLiveData.postValue(stringsProvider.requiredField)
            _errorStringForDatastoreUrlFieldLiveData.postValue(stringsProvider.requiredField)
            _errorStringForThreadsGateUrlFieldLiveData.postValue(stringsProvider.requiredField)
        } else {
            if (config.name.isNullOrEmpty()) {
                _errorStringForServerNameFieldLiveData.postValue(stringsProvider.requiredField)
            }
            if (config.threadsGateProviderUid.isNullOrEmpty()) {
                _errorStringForProviderIdFieldLiveData.postValue(stringsProvider.requiredField)
            }
            if (config.serverBaseUrl.isNullOrEmpty()) {
                _errorStringForBaseUrlFieldLiveData.postValue(stringsProvider.requiredField)
            }
            if (config.datastoreUrl.isNullOrEmpty()) {
                _errorStringForDatastoreUrlFieldLiveData.postValue(stringsProvider.requiredField)
            }
            if (config.threadsGateUrl.isNullOrEmpty()) {
                _errorStringForThreadsGateUrlFieldLiveData.postValue(stringsProvider.requiredField)
            }
        }
    }

    @get:Bindable
    val nameTextWatcher = object : AfterTextChangedTextWatcher {
        override fun afterTextChanged(s: Editable?) {
            if (s != null) {
                if (_serverConfigLiveData.value?.name != s.toString()) {
                    serverConfigLiveData.value?.name = s.toString()
                    _serverConfigLiveData.postValue(serverConfigLiveData.value)
                }
                if (s.isNotEmpty()) {
                    _errorStringForServerNameFieldLiveData.postValue(null)
                }
            }
        }
    }

    @get:Bindable
    val providerIdTextWatcher = object : AfterTextChangedTextWatcher {
        override fun afterTextChanged(s: Editable?) {
            if (s != null) {
                if (serverConfigLiveData.value?.threadsGateProviderUid != s.toString()) {
                    serverConfigLiveData.value?.threadsGateProviderUid = s.toString()
                    _serverConfigLiveData.postValue(serverConfigLiveData.value)
                }
                if (s.isNotEmpty()) {
                    _errorStringForProviderIdFieldLiveData.postValue(null)
                }
            }
        }
    }

    @get:Bindable
    val baseUrlTextWatcher = object : AfterTextChangedTextWatcher {
        override fun afterTextChanged(s: Editable?) {
            if (s != null) {
                if (serverConfigLiveData.value?.serverBaseUrl != s.toString()) {
                    serverConfigLiveData.value?.serverBaseUrl = s.toString()
                    _serverConfigLiveData.postValue(serverConfigLiveData.value)
                }
                if (s.isNotEmpty()) {
                    _errorStringForBaseUrlFieldLiveData.postValue(null)
                }
            }
        }
    }

    @get:Bindable
    val datastoreUrlTextWatcher = object : AfterTextChangedTextWatcher {
        override fun afterTextChanged(s: Editable?) {
            if (s != null) {
                if (serverConfigLiveData.value?.datastoreUrl != s.toString()) {
                    serverConfigLiveData.value?.datastoreUrl = s.toString()
                    _serverConfigLiveData.postValue(serverConfigLiveData.value)
                }
                if (s.isNotEmpty()) {
                    _errorStringForDatastoreUrlFieldLiveData.postValue(null)
                }
            }
        }
    }

    @get:Bindable
    val threadsGateUrlTextWatcher = object : AfterTextChangedTextWatcher {
        override fun afterTextChanged(s: Editable?) {
            if (s != null) {
                if (serverConfigLiveData.value?.threadsGateUrl != s.toString()) {
                    serverConfigLiveData.value?.threadsGateUrl = s.toString()
                    _serverConfigLiveData.postValue(serverConfigLiveData.value)
                }
                if (s.isNotEmpty()) {
                    _errorStringForThreadsGateUrlFieldLiveData.postValue(null)
                }
            }
        }
    }

    override fun addOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback?) {}
    override fun removeOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback?) {}
}
