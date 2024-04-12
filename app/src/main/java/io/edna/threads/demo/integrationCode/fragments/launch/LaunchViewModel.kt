package io.edna.threads.demo.integrationCode.fragments.launch

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import androidx.navigation.findNavController
import im.threads.business.UserInfoBuilder
import im.threads.business.config.BaseConfig
import im.threads.business.models.enums.ApiVersionEnum
import im.threads.business.models.enums.ApiVersionEnum.Companion.defaultApiVersionEnum
import im.threads.business.models.enums.CurrentUiTheme
import im.threads.ui.core.ThreadsLib
import io.edna.threads.demo.BuildConfig
import io.edna.threads.demo.R
import io.edna.threads.demo.appCode.business.PreferencesProvider
import io.edna.threads.demo.appCode.business.ServersProvider
import io.edna.threads.demo.appCode.business.UiThemeProvider
import io.edna.threads.demo.appCode.business.VolatileLiveData
import io.edna.threads.demo.appCode.models.ServerConfig
import io.edna.threads.demo.appCode.models.TestData
import io.edna.threads.demo.appCode.models.UiTheme
import io.edna.threads.demo.appCode.models.UserInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.parceler.Parcels

class LaunchViewModel(
    private val preferences: PreferencesProvider,
    private val uiThemeProvider: UiThemeProvider,
    private val serversProvider: ServersProvider
) : ViewModel(), DefaultLifecycleObserver {
    val currentUiThemeLiveData: MutableLiveData<UiTheme> = MutableLiveData()
    val themeSelectorLiveData: VolatileLiveData<CurrentUiTheme> = VolatileLiveData()
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private var _selectedApiVersionLiveData = MutableLiveData(getSelectedApiVersion())
    var selectedApiVersionLiveData: LiveData<String> = _selectedApiVersionLiveData

    private var _selectedUserLiveData = MutableLiveData(getSelectedUser())
    var selectedUserLiveData: LiveData<UserInfo?> = _selectedUserLiveData

    val _preregisterLiveData: MutableLiveData<Boolean> = MutableLiveData()
    val preregisterLiveData: LiveData<Boolean> = _preregisterLiveData

    private var _selectedServerLiveData = MutableLiveData(getSelectedServer())
    var selectedServerConfigLiveData: LiveData<ServerConfig?> = _selectedServerLiveData

    private var _enabledLoginButtonLiveData = MutableLiveData(false)
    var enabledLoginButtonLiveData: LiveData<Boolean> = _enabledLoginButtonLiveData

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        preferences.cleanJsonOnPreferences()
        getSelectedServer()?.let { server ->
            if (server.isAllFieldsFilled()) {
                _selectedServerLiveData.postValue(server)
                changeServerSettings(server)
            }
        }
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        checkUiTheme()
        initPreregisterCheckbox()
    }

    fun click(view: View) {
        val navigationController: NavController =
            (view.context as Activity).findNavController(R.id.nav_host_fragment_content_main)
        when (view.id) {
            R.id.serverButton -> navigationController.navigate(R.id.action_LaunchFragment_to_ServerListFragment)
            R.id.demonstrations -> navigationController.navigate(R.id.action_LaunchFragment_to_DemonstrationsListFragment)
            R.id.uiTheme -> themeSelectorLiveData.postValue(ThreadsLib.getInstance().currentUiTheme)
            R.id.userButton -> navigationController.navigate(R.id.action_LaunchFragment_to_UserListFragment)
            R.id.login -> { login(navigationController) }
        }
    }

    fun saveUserSelectedUiTheme(theme: CurrentUiTheme) {
        coroutineScope.launch {
            ThreadsLib.getInstance().currentUiTheme = theme
            currentUiThemeLiveData.postValue(getCurrentUiTheme(theme))
            applyCurrentUiTheme(theme)
        }
    }

    fun callInitUser(user: UserInfo) {
        ThreadsLib.getInstance().initUser(
            UserInfoBuilder(user.userId!!)
                .setAuthData(user.authorizationHeader, user.xAuthSchemaHeader)
                .setClientData(user.userData)
                .setClientIdSignature(user.signature)
                .setAppMarker(user.appMarker),
            isPreregisterEnabled
        )
    }

    private fun login(navigationController: NavController) {
        if (!ThreadsLib.isInitialized()) {
            return
        }

        val serverConfig = _selectedServerLiveData.value
        val user = _selectedUserLiveData.value
        val isUserHasRequiredFields = user?.userId != null

        if (serverConfig != null && isUserHasRequiredFields) {
            var apiVersion: ApiVersionEnum? = _selectedApiVersionLiveData.value?.let {
                ApiVersionEnum.createApiVersionEnum(it)
            }
            if (apiVersion == null) {
                apiVersion = ApiVersionEnum.defaultApiVersionEnum
            }
            BaseConfig.getInstance().apiVersion = apiVersion
            changeServerSettings(serverConfig)

            if (user != null && !isPreregisterEnabled) callInitUser(user)
            navigationController.navigate(R.id.action_LaunchFragment_to_ChatAppFragment)
        }
    }

    private fun changeServerSettings(serverConfig: ServerConfig) {
        ThreadsLib.changeServerSettings(
            serverConfig.serverBaseUrl,
            serverConfig.datastoreUrl,
            serverConfig.threadsGateUrl,
            serverConfig.threadsGateProviderUid,
            serverConfig.trustedSSLCertificates,
            serverConfig.allowUntrustedSSLCertificate
        )
    }

    private fun applyCurrentUiTheme(currentUiTheme: CurrentUiTheme) {
        coroutineScope.launch(Dispatchers.Main) {
            when (currentUiTheme) {
                CurrentUiTheme.SYSTEM -> AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_FOLLOW_SYSTEM)
                CurrentUiTheme.LIGHT -> AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO)
                CurrentUiTheme.DARK -> AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES)
            }
        }
    }

    internal fun checkUiTheme() {
        if (ThreadsLib.isInitialized()) {
            val uiTheme = ThreadsLib.getInstance().currentUiTheme
            currentUiThemeLiveData.postValue(getCurrentUiTheme(uiTheme))
            applyCurrentUiTheme(uiTheme)
        }
    }

    private fun initPreregisterCheckbox() {
        _preregisterLiveData.postValue(isPreregisterEnabled)
    }

    private fun getCurrentUiTheme(currentUiTheme: CurrentUiTheme): UiTheme {
        return when (currentUiTheme) {
            CurrentUiTheme.LIGHT -> UiTheme.LIGHT
            CurrentUiTheme.DARK -> UiTheme.DARK
            CurrentUiTheme.SYSTEM -> {
                if (uiThemeProvider.isDarkThemeOn()) {
                    UiTheme.DARK
                } else {
                    UiTheme.LIGHT
                }
            }
        }
    }

    fun callFragmentResultListener(key: String, bundle: Bundle) {
        if (key == LaunchFragment.SELECTED_USER_KEY && bundle.containsKey(LaunchFragment.SELECTED_USER_KEY)) {
            val user: UserInfo? = if (Build.VERSION.SDK_INT >= 33) {
                Parcels.unwrap(bundle.getParcelable(LaunchFragment.SELECTED_USER_KEY, Parcelable::class.java))
            } else {
                Parcels.unwrap(bundle.getParcelable(LaunchFragment.SELECTED_USER_KEY))
            }
            if (user != null && user.isAllFieldsFilled()) {
                _selectedUserLiveData.postValue(user)
                preferences.saveSelectedUser(user)
            }
        }
        if (key == LaunchFragment.SELECTED_SERVER_CONFIG_KEY && bundle.containsKey(LaunchFragment.SELECTED_SERVER_CONFIG_KEY)) {
            var server: ServerConfig? = if (Build.VERSION.SDK_INT >= 33) {
                Parcels.unwrap(bundle.getParcelable(LaunchFragment.SELECTED_SERVER_CONFIG_KEY, Parcelable::class.java))
            } else {
                Parcels.unwrap(bundle.getParcelable(LaunchFragment.SELECTED_SERVER_CONFIG_KEY))
            }
            if (server == null || !server.isAllFieldsFilled()) {
                server = serversProvider.getSelectedServer()
            }
            if (server != null && server.isAllFieldsFilled()) {
                _selectedServerLiveData.postValue(server)
                serversProvider.saveSelectedServer(server)
                changeServerSettings(server)
            }
        }
    }

    fun subscribeForData(lifecycleOwner: LifecycleOwner) {
        selectedUserLiveData.observe(lifecycleOwner) {
            _enabledLoginButtonLiveData.postValue(it?.isAllFieldsFilled())
        }
    }

    fun onPreregisterCheckedChange(isChecked: Boolean) {
        isPreregisterEnabled = isChecked
    }

    internal fun setSelectedApiVersion(apiVersion: String?) {
        if (!apiVersion.isNullOrBlank()) {
            preferences.saveSelectedApiVersion(apiVersion)
            _selectedApiVersionLiveData.postValue(apiVersion)
        }
    }

    private fun getSelectedApiVersion(): String {
        val apiVersion = preferences.getSelectedApiVersion()
        return if (apiVersion.isNullOrBlank()) {
            defaultApiVersionEnum.toString()
        } else {
            apiVersion
        }
    }

    private fun getSelectedUser(): UserInfo? {
        val testData = BuildConfig.TEST_DATA.get() as? String
        return if (testData.isNullOrEmpty()) {
            val user = preferences.getSelectedUser()
            preferences.getAllUserList().forEach {
                if (it.userId == user?.userId) {
                    return user
                }
            }
            UserInfo()
        } else if (testData.isNotEmpty()) {
            TestData.fromJson(testData).userInfo
        } else {
            null
        }
    }

    private fun getSelectedServer(): ServerConfig? {
        val testData = BuildConfig.TEST_DATA.get() as? String
        return if (testData.isNullOrEmpty()) {
            preferences.getSelectedServer()?.let { server ->
                preferences.getAllServers().forEach {
                    if (server.name == it.name) {
                        return ServerConfig(
                            it.name,
                            it.threadsGateProviderUid,
                            it.datastoreUrl,
                            it.serverBaseUrl,
                            it.threadsGateUrl,
                            it.isFromApp,
                            it.isShowMenu,
                            it.filesAndMediaMenuItemEnabled,
                            it.trustedSSLCertificates,
                            it.allowUntrustedSSLCertificate
                        )
                    }
                }
            }
            if (preferences.getAllServers().size > 0) {
                preferences.getAllServers()[0]
            } else {
                null
            }
        } else if (testData.isNotEmpty()) {
            TestData.fromJson(testData).serverConfig
        } else {
            null
        }
    }

    companion object {
        var isPreregisterEnabled = false
    }
}
