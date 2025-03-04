package io.edna.threads.demo.integrationCode.fragments.launch

import android.app.Activity
import android.content.Context
import android.content.Intent
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
import com.google.firebase.messaging.FirebaseMessaging
import im.threads.business.UserInfoBuilder
import im.threads.business.config.BaseConfig
import im.threads.business.models.enums.ApiVersionEnum
import im.threads.business.models.enums.ApiVersionEnum.Companion.defaultApiVersionEnum
import im.threads.business.models.enums.CurrentUiTheme
import im.threads.ui.ChatCenterPushMessageHelper
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
import io.edna.threads.demo.appCode.push.HCMTokenRefresher
import io.edna.threads.demo.integrationCode.fragments.launch.LaunchFragment.Companion.APP_INIT_THREADS_LIB_ACTION
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

    private var _restartAppLiveData = MutableLiveData(false)
    var restartAppLiveData: LiveData<Boolean> = _restartAppLiveData

    private val asyncInit = false

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        preferences.cleanJsonOnPreferences()
        getSelectedServer()?.let { server ->
            if (server.isAllFieldsFilled()) {
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
            R.id.userButton -> navigationController.navigate(R.id.action_LaunchFragment_to_UserListFragment)
            R.id.login -> { login(navigationController, view.context) }
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
        // Установка пользователя. Обязательный параметр только userId
        ThreadsLib.getInstance().initUser(
            UserInfoBuilder(user.userId!!)
                .setAuthData(user.authorizationHeader, user.xAuthSchemaHeader)
                .setClientData(user.userData)
                .setClientIdSignature(user.signature)
                .setAppMarker(user.appMarker)
                .setUserName(user.userName),
            isPreregisterEnabled
        )
    }

    private fun login(navigationController: NavController, context: Context) {
        val serverConfig = _selectedServerLiveData.value
        val user = _selectedUserLiveData.value
        val isUserHasRequiredFields = user?.userId != null

        _preregisterLiveData.value = false
        isPreregisterEnabled = false

        if (serverConfig != null && isUserHasRequiredFields) {
            if (!ThreadsLib.isInitialized()) {
                // SDK можно инициализировать асинхронно в фоновом потоке
                if (asyncInit) {
                    coroutineScope.launch {
                        initApiVersion()
                        // Отправьте событие, чтобы SDK могло понять, что фоновая инициализация завершена
                        context.sendBroadcast(Intent(APP_INIT_THREADS_LIB_ACTION))
                        withContext(Dispatchers.Main) {
                            // Обновление Firebase/Huawei токенов для проброса в чат перед установкой пользователя
                            checkAndUpdateTokens(context.applicationContext) {
                                // Шаг 2. Установка пользователя и открытие фрагмента с чатом
                                initUserAndOpenChat(user, navigationController, context)
                            }
                        }
                    }
                } else {
                    initApiVersion()
                    checkAndUpdateTokens(context.applicationContext) {
                        // Шаг 2. Установка пользователя и открытие фрагмента с чатом
                        initUserAndOpenChat(user, navigationController, context)
                    }
                }
            } else {
                val willBeRestarted = changeServerSettings(serverConfig)
                if (!willBeRestarted) {
                    initUserAndOpenChat(user, navigationController, context)
                }
                return
            }
        }
    }

    private fun initApiVersion() {
        var apiVersion: ApiVersionEnum? = _selectedApiVersionLiveData.value?.let {
            ApiVersionEnum.createApiVersionEnum(it)
        }
        if (apiVersion == null) {
            apiVersion = ApiVersionEnum.defaultApiVersionEnum
        }

        // Данная строка нужна для установки версии backend api для подключения. Необязательный параметр
        BaseConfig.getInstance().apiVersion = apiVersion
    }

    private fun initUserAndOpenChat(user: UserInfo?, navigationController: NavController, context: Context) {
        if (user != null && !isPreregisterEnabled) {
            // Установка пользователя
            callInitUser(user)
        }
        // Открытие фрагмента с чатом
        navigationController.navigate(R.id.action_LaunchFragment_to_ChatAppFragment)
    }

    private fun checkAndUpdateTokens(context: Context, callback: () -> Unit) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                ChatCenterPushMessageHelper().setFcmToken(token)
                callback()
            }
        }
        HCMTokenRefresher.requestToken(context)
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
                changeServerSettings(server)
            }
        }
    }

    /**
     * Меняет настройки сервера и сохраняет пользователя.
     * В случае необходимости перезапуска аппа возвращает true
     * @param server новые настройки сервера
     */
    private fun changeServerSettings(server: ServerConfig): Boolean {
        val selectedServer = serversProvider.getSelectedServer()
        if (selectedServer == null || selectedServer.equals(server) || BuildConfig.IS_MOCK_WEB_SERVER.get()) {
            return false
        } else {
            _selectedServerLiveData.postValue(server)
            serversProvider.saveSelectedServer(server)

            if (ThreadsLib.isInitialized()) {
                _restartAppLiveData.postValue(true)
                return true
            }

            return false
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
