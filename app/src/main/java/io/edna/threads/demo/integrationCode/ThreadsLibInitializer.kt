package io.edna.threads.demo.integrationCode

import android.content.Context
import android.content.Intent
import im.threads.business.core.UnreadMessagesCountListener
import im.threads.business.logger.LoggerConfig
import im.threads.business.logger.LoggerRetentionPolicy
import im.threads.business.models.enums.ApiVersionEnum
import im.threads.ui.config.ConfigBuilder
import im.threads.ui.core.ThreadsLib
import io.edna.threads.demo.appCode.models.ServerConfig
import io.edna.threads.demo.appCode.themes.ChatThemes
import io.edna.threads.demo.integrationCode.fragments.launch.LaunchFragment.Companion.APP_UNREAD_COUNT_BROADCAST
import io.edna.threads.demo.integrationCode.fragments.launch.LaunchFragment.Companion.UNREAD_COUNT_KEY
import java.io.File

class ThreadsLibInitializer() {

    fun initThreadsLib(context: Context, apiVersionEnum: ApiVersionEnum, server: ServerConfig) {
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

        // Устанавливаем параметры подключения к серверу
        configBuilder.apply {
            serverBaseUrl(server.serverBaseUrl)
            datastoreUrl(server.datastoreUrl)
            threadsGateUrl(server.threadsGateUrl)
            threadsGateProviderUid(server.threadsGateProviderUid)
            trustedSSLCertificates(server.trustedSSLCertificates)
            allowUntrustedSSLCertificates(server.allowUntrustedSSLCertificate)
            setNewChatCenterApi()
            setApiVersion(apiVersionEnum)
        }

        // Инициализация библиотеки. Только после данного вызова можно начинать работу с SDK
        ThreadsLib.init(configBuilder)
        ThreadsLib.getInstance().apply {
            // Кастомизация внешнего вида. Поддержка темной темы
            val themes = ChatThemes()
            applyLightTheme(themes.getLightChatTheme())
            applyDarkTheme(themes.getDarkChatTheme())
        }
    }
}
