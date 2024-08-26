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
import im.threads.business.core.UnreadMessagesCountListener
import im.threads.business.logger.LoggerConfig
import im.threads.business.logger.LoggerRetentionPolicy
import im.threads.business.markdown.MarkdownConfig
import im.threads.business.models.enums.ApiVersionEnum
import im.threads.business.models.enums.ApiVersionEnum.Companion.defaultApiVersionEnum
import im.threads.business.models.enums.CurrentUiTheme
import im.threads.ui.ChatCenterPushMessageHelper
import im.threads.ui.ChatStyle
import im.threads.ui.config.ConfigBuilder
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
import io.edna.threads.demo.integrationCode.EdnaThreadsApplication
import io.edna.threads.demo.integrationCode.fragments.launch.LaunchFragment.Companion.APP_INIT_THREADS_LIB_ACTION
import io.edna.threads.demo.integrationCode.fragments.launch.LaunchFragment.Companion.APP_UNREAD_COUNT_BROADCAST
import io.edna.threads.demo.integrationCode.fragments.launch.LaunchFragment.Companion.UNREAD_COUNT_KEY
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.parceler.Parcels
import java.io.File

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

    private fun initThreadsLib(context: Context) {
        // Устанавливает конфиг для внутреннего логгера. Необязательный параметр
        val loggerConfig = LoggerConfig.Builder(context)
            .logToFile()
            .dir(File(context.filesDir, "logs"))
            .retentionPolicy(LoggerRetentionPolicy.TOTAL_SIZE)
            .maxTotalSize(5242880)
            .build()

        // Устанавливает общий конфиг для библиотеки. Обязательный параметр только context
        val configBuilder = ConfigBuilder(context)
            .unreadMessagesCountListener(object : UnreadMessagesCountListener {
                override fun onUnreadMessagesCountChanged(count: Int) {
                    val intent = Intent(APP_UNREAD_COUNT_BROADCAST)
                    intent.putExtra(UNREAD_COUNT_KEY, count)
                    context.sendBroadcast(intent)
                }
            })
            .surveyCompletionDelay(2000)
            .historyLoadingCount(50)
            .isDebugLoggingEnabled(true)
            .showAttachmentsButton()
            .enableLogging(loggerConfig)

        serversProvider.getSelectedServer()?.let { server ->
            // Устанавливаем параметры подключения к серверу
            configBuilder.serverBaseUrl(server.serverBaseUrl)
            configBuilder.datastoreUrl(server.datastoreUrl)
            configBuilder.threadsGateUrl(server.threadsGateUrl)
            configBuilder.threadsGateProviderUid(server.threadsGateProviderUid)
            configBuilder.trustedSSLCertificates(server.trustedSSLCertificates)
            configBuilder.allowUntrustedSSLCertificates(server.allowUntrustedSSLCertificate)
            configBuilder.setNewChatCenterApi()
            configBuilder.setApiVersion(ApiVersionEnum.V18)
        }

        // Инициализация библиотеки. Только после данного вызова можно начинать работу с SDK
        ThreadsLib.init(configBuilder)
        ThreadsLib.getInstance().apply {
            // Кастомизация внешнего вида. Поддержка темной темы
            applyLightTheme(getLightChatTheme())
            applyDarkTheme(getDarkChatTheme())
        }
    }

    private fun getLightChatTheme() = getMainChatTheme().apply {
        setConsultSearchingProgressColor(R.color.light_toolbar)
        setChatBodyIconsTint(R.color.light_toolbar)
        enableLinkPreview()
        setChatTitleStyle(
            R.string.app_name,
            R.string.demo_alt_threads_operator_subtitle,
            R.color.light_toolbar,
            R.color.alt_threads_chat_context_menu,
            R.color.alt_threads_chat_toolbar_text,
            R.color.light_statusbar,
            R.bool.alt_threads_chat_is_light_status_bar,
            R.color.light_toolbar,
            R.color.alt_threads_chat_toolbar_hint,
            true
        )
        setOutgoingMessageBubbleColor(R.color.light_outgoing_bubble)
        setScrollDownButtonIcon(R.drawable.alt_threads_scroll_down_icon_light)
        setRecordButtonBackgroundColor(R.color.light_toolbar)
        setOutgoingMessageTextColor(R.color.black_color)
        setOutgoingImageTimeBackgroundColor(R.color.light_outgoing_image_time_background)
        setOutgoingMessageTimeColor(R.color.light_outgoing_time_text)
        setMessageSendingResources(null, R.color.light_icons)
        setMessageSentResources(null, R.color.light_icons)
        setMessageDeliveredResources(null, R.color.light_icons)
        setMessageReadResources(null, R.color.light_icons)
        setMessageFailedResources(null, R.color.light_icons)
        setChatHighlightingColor(R.color.light_highlighting)
        setIncomingMessageLinkColor(R.color.light_links)
        setOutgoingMessageLinkColor(R.color.light_links)
    }

    private fun getDarkChatTheme() = getMainChatTheme().apply {
        setConsultSearchingProgressColor(R.color.dark_toolbar)
        setChatBodyIconsTint(R.color.dark_toolbar)
        enableLinkPreview()
        setChatTitleStyle(
            R.string.demo_alt_threads_contact_center,
            R.string.demo_alt_threads_operator_subtitle,
            R.color.dark_toolbar,
            R.color.alt_threads_chat_context_menu,
            R.color.alt_threads_chat_toolbar_text,
            R.color.alt_threads_chat_status_bar,
            R.bool.alt_threads_chat_is_light_status_bar,
            R.color.alt_threads_chat_toolbar_menu_item_black,
            R.color.alt_threads_chat_toolbar_hint,
            true
        )
        setOutgoingMessageBubbleColor(R.color.dark_outgoing_bubble)
        setScrollDownButtonIcon(R.drawable.alt_threads_scroll_down_icon_black)
        setRecordButtonBackgroundColor(R.color.dark_toolbar)
        setOutgoingMessageTextColor(R.color.white_color_fa)
        setOutgoingImageTimeBackgroundColor(R.color.dark_outgoing_image_time_background)
        setOutgoingMessageTimeColor(R.color.dark_outgoing_time_text)
        setMessageSendingResources(null, R.color.dark_icons)
        setMessageSentResources(null, R.color.dark_icons)
        setMessageDeliveredResources(null, R.color.dark_icons)
        setMessageReadResources(null, R.color.dark_icons)
        setMessageFailedResources(null, R.color.dark_icons)
        setChatHighlightingColor(R.color.dark_highlighting)
        setIncomingMessageLinkColor(R.color.dark_links)
        setOutgoingMessageLinkColor(R.color.dark_links)
        setDelimitersColors(
            im.threads.R.color.ecc_error_red_df0000,
            R.color.dark_links
        )
        setChatBackgroundColor(R.color.dark_chat_background)
        setSystemMessageStyle(
            null,
            null,
            R.color.dark_system_text,
            null,
            null,
            R.color.dark_links
        )
        setSurveyStyle(
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            R.color.dark_system_text,
            R.color.dark_system_text
        )
        setDownloadButtonTintResId(im.threads.R.color.ecc_white)
        setDownloadButtonBackgroundTintResId(R.color.dark_links)
        setChatErrorScreenStyle(
            null,
            null,
            R.color.white_color,
            null,
            null,
            null,
            null
        )
        setOutgoingQuoteViewStyle(
            R.color.dark_links,
            R.color.black_color,
            R.color.alt_threads_chat_toolbar_hint,
            R.color.white_color_fa
        )
        setIncomingQuoteViewStyle(
            R.color.dark_links,
            R.color.black_color,
            R.color.alt_threads_chat_toolbar_hint,
            R.color.white_color_fa
        )
    }

    private fun getMainChatTheme(): ChatStyle {
        val chatStyle = ChatStyle()
            .setDefaultFontBold(LATO_BOLD_FONT_PATH)
            .setDefaultFontLight(LATO_LIGHT_FONT_PATH)
            .setDefaultFontRegular(LATO_REGULAR_FONT_PATH)
            .setScrollChatToEndIfUserTyping(false)

        val markdownConfig = MarkdownConfig()
        markdownConfig.isLinkUnderlined = true
        chatStyle
            .setChatSubtitleShowConsultOrgUnit(true)
            .setIncomingMarkdownConfiguration(markdownConfig)
            .setOutgoingMarkdownConfiguration(markdownConfig)
            .setVisibleChatTitleShadow(R.bool.alt_threads_chat_title_shadow_is_visible)
            .setShowConsultSearching(true)
            .setVoiceMessageEnabled(true)
            .showChatBackButton(true)
            .setIngoingPadding(
                R.dimen.alt_greenBubbleIncomingPaddingLeft,
                R.dimen.alt_greenBubbleIncomingPaddingTop,
                R.dimen.alt_greenBubbleIncomingPaddingRight,
                R.dimen.alt_greenBubbleIncomingPaddingBottom
            )
            .setIncomingImageBordersSize(
                R.dimen.alt_incomingImageLeftBorderSize,
                R.dimen.alt_incomingImageTopBorderSize,
                R.dimen.alt_incomingImageRightBorderSize,
                R.dimen.alt_incomingImageBottomBorderSize
            )
            .setOutgoingImageBordersSize(
                R.dimen.alt_outgoingImageLeftBorderSize,
                R.dimen.alt_outgoingImageTopBorderSize,
                R.dimen.alt_outgoingImageRightBorderSize,
                R.dimen.alt_outgoingImageBottomBorderSize
            )
            .setIncomingImageMask(R.drawable.alt_thread_incoming_image_mask)
            .setOutgoingImageMask(R.drawable.alt_thread_outgoing_image_mask)
            .setOutgoingBubbleMask(R.drawable.alt_thread_outgoing_bubble)
            .setIncomingBubbleMask(R.drawable.alt_thread_incoming_bubble)

        return chatStyle
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
        if (!ThreadsLib.isInitialized()) initSdk(EdnaThreadsApplication.context)

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

        if (serverConfig != null && isUserHasRequiredFields) {
            var apiVersion: ApiVersionEnum? = _selectedApiVersionLiveData.value?.let {
                ApiVersionEnum.createApiVersionEnum(it)
            }
            if (apiVersion == null) {
                apiVersion = ApiVersionEnum.defaultApiVersionEnum
            }

            if (!ThreadsLib.isInitialized()) {
                // SDK можно инициализировать асинхронно в фоновом потоке
                if (asyncInit) {
                    coroutineScope.launch {
                        // Шаг 1. Инициализация SDK
                        initSdk(context.applicationContext)
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
                    // Шаг 1. Инициализация SDK
                    initSdk(context.applicationContext)
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

    /**
     * Шаг 1. Инициализация SDK
     */
    private fun initSdk(applicationContext: Context) {
        var apiVersion: ApiVersionEnum? = _selectedApiVersionLiveData.value?.let {
            ApiVersionEnum.createApiVersionEnum(it)
        }
        if (apiVersion == null) {
            apiVersion = ApiVersionEnum.defaultApiVersionEnum
        }

        initThreadsLib(applicationContext)
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
        if (selectedServer == null || selectedServer.equals(server)) {
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

private const val LATO_BOLD_FONT_PATH = "fonts/lato-bold.ttf"
private const val LATO_LIGHT_FONT_PATH = "fonts/lato-light.ttf"
private const val LATO_REGULAR_FONT_PATH = "fonts/lato-regular.ttf"
