package io.edna.threads.demo.integrationCode

import android.app.Application
import android.content.Intent
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.perf.FirebasePerformance
import im.threads.business.UserInfoBuilder
import im.threads.business.core.UnreadMessagesCountListener
import im.threads.business.logger.LoggerConfig
import im.threads.business.logger.LoggerRetentionPolicy
import im.threads.business.markdown.MarkdownConfig
import im.threads.ui.ChatStyle
import im.threads.ui.config.ConfigBuilder
import im.threads.ui.core.ThreadsLib
import io.edna.threads.demo.BuildConfig
import io.edna.threads.demo.R
import io.edna.threads.demo.appCode.business.PreferencesProvider
import io.edna.threads.demo.appCode.business.ServersProvider
import io.edna.threads.demo.appCode.business.appModule
import io.edna.threads.demo.integrationCode.fragments.launch.LaunchFragment.Companion.APP_INIT_THREADS_LIB_ACTION
import io.edna.threads.demo.integrationCode.fragments.launch.LaunchFragment.Companion.APP_UNREAD_COUNT_BROADCAST
import io.edna.threads.demo.integrationCode.fragments.launch.LaunchFragment.Companion.UNREAD_COUNT_KEY
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import java.io.File

class EdnaThreadsApplication : Application() {
    private val serversProvider: ServersProvider by inject()
    private val preferences: PreferencesProvider by inject()
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val asyncInit = false

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@EdnaThreadsApplication)
            modules(appModule)
        }

        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)
        FirebasePerformance.getInstance().isPerformanceCollectionEnabled = !BuildConfig.DEBUG

        if (asyncInit) {
            coroutineScope.launch {
                initThreadsLib()
                initUser()
                sendBroadcast(Intent(APP_INIT_THREADS_LIB_ACTION))
            }
        } else {
            initThreadsLib()
            initUser()
        }
    }

    private fun initThreadsLib() {
        val loggerConfig = LoggerConfig.Builder(this@EdnaThreadsApplication)
            .logToFile()
            .dir(File(this.filesDir, "logs"))
            .retentionPolicy(LoggerRetentionPolicy.TOTAL_SIZE)
            .maxTotalSize(5242880)
            .build()

        val configBuilder = ConfigBuilder(this)
            .unreadMessagesCountListener(object : UnreadMessagesCountListener {
                override fun onUnreadMessagesCountChanged(count: Int) {
                    val intent = Intent(APP_UNREAD_COUNT_BROADCAST)
                    intent.putExtra(UNREAD_COUNT_KEY, count)
                    sendBroadcast(intent)
                }
            })
            .surveyCompletionDelay(2000)
            .historyLoadingCount(50)
            .isDebugLoggingEnabled(true)
            .showAttachmentsButton()
            .enableLogging(loggerConfig)
            .keepSocketActive(true)

        serversProvider.getSelectedServer()?.let { server ->
            configBuilder.serverBaseUrl(server.serverBaseUrl)
            configBuilder.datastoreUrl(server.datastoreUrl)
            configBuilder.threadsGateUrl(server.threadsGateUrl)
            configBuilder.threadsGateProviderUid(server.threadsGateProviderUid)
            configBuilder.trustedSSLCertificates(server.trustedSSLCertificates)
            configBuilder.allowUntrustedSSLCertificates(server.allowUntrustedSSLCertificate)
            configBuilder.setNewChatCenterApi()
        }

        ThreadsLib.init(configBuilder)
        ThreadsLib.getInstance().apply {
            applyLightTheme(getLightChatTheme())
            applyDarkTheme(getDarkChatTheme())
        }
    }

    private fun initUser() {
        val user = preferences.getSelectedUser()
        if (user != null && user.isAllFieldsFilled()) {
            ThreadsLib.getInstance().initUser(
                UserInfoBuilder(user.userId!!)
                    .setAuthData(user.authorizationHeader, user.xAuthSchemaHeader)
                    .setClientData(user.userData)
                    .setClientIdSignature(user.signature)
                    .setAppMarker(user.appMarker),
                false
            )
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
}

private const val LATO_BOLD_FONT_PATH = "fonts/lato-bold.ttf"
private const val LATO_LIGHT_FONT_PATH = "fonts/lato-light.ttf"
private const val LATO_REGULAR_FONT_PATH = "fonts/lato-regular.ttf"
