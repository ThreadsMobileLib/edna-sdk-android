package io.edna.threads.demo.integrationCode

import android.app.Application
import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.perf.FirebasePerformance
import com.microsoft.appcenter.AppCenter
import com.microsoft.appcenter.analytics.Analytics
import com.microsoft.appcenter.crashes.Crashes
import im.threads.business.core.ContextHolder
import im.threads.business.models.enums.ApiVersionEnum
import im.threads.ui.ChatCenterPushMessageHelper
import io.edna.threads.demo.BuildConfig
import io.edna.threads.demo.appCode.business.PreferencesProvider
import io.edna.threads.demo.appCode.business.ServersProvider
import io.edna.threads.demo.appCode.business.appModule
import io.edna.threads.demo.appCode.models.ServerConfig
import io.edna.threads.demo.appCode.push.HCMTokenRefresher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class EdnaThreadsApplication : Application() {
    private val serversProvider: ServersProvider by inject()
    private val preferences: PreferencesProvider by inject()
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val asyncInit = false

    override fun onCreate() {
        super.onCreate()

        startAppCenter()

        startKoin {
            androidContext(this@EdnaThreadsApplication)
            modules(appModule)
        }

        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)
        FirebasePerformance.getInstance().isPerformanceCollectionEnabled = !BuildConfig.DEBUG

        val sdkInitializer = ThreadsLibInitializer()
        val apiVersion = ApiVersionEnum.createApiVersionEnum(preferences.getSelectedApiVersion())
        if (!BuildConfig.IS_MOCK_WEB_SERVER.get()) {
            // Инициализация сдк обычным способом
            serversProvider.getSelectedServer()?.let {
                sdkInitializer.initThreadsLib(this.applicationContext, apiVersion, it)
            } ?: Log.i("Init sdk", "No server")
        } else {
            // Инициализация сдк моком сервера для UI тестов
            val serverConfig = ServerConfig(
                "Mock Server",
                ednaMockThreadsGateProviderUid,
                ednaMockUrl,
                ednaMockUrl,
                ednaMockThreadsGateUrl,
                allowUntrustedSSLCertificate = ednaMockAllowUntrustedSSLCertificate
            )
            sdkInitializer.initThreadsLib(this.applicationContext, apiVersion, serverConfig)
        }
        checkAndUpdateTokens()
    }

    private fun checkAndUpdateTokens() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                ChatCenterPushMessageHelper().setFcmToken(token)
            }
        }
        HCMTokenRefresher.requestToken(ContextHolder.context)
    }

    private fun startAppCenter() {
        if (BuildConfig.DEBUG.not()) {
            System.getenv("APP_CENTER_KEY")?.let { appCenterKey ->
                AppCenter.start(
                    this,
                    appCenterKey,
                    Analytics::class.java,
                    Crashes::class.java
                )
            }
        }
    }
}

private const val LATO_BOLD_FONT_PATH = "fonts/lato-bold.ttf"
private const val LATO_LIGHT_FONT_PATH = "fonts/lato-light.ttf"
private const val LATO_REGULAR_FONT_PATH = "fonts/lato-regular.ttf"

const val ednaMockScheme = "http"
const val ednaMockHost = "localhost"
const val ednaMockPort = 8080
const val ednaMockUrl = "$ednaMockScheme://$ednaMockHost:$ednaMockPort/"
const val ednaMockThreadsGateUrl = "ws://$ednaMockHost:$ednaMockPort/gate/socket"
const val ednaMockThreadsGateProviderUid = "TEST_93jLrtnipZsfbTddRfEfbyfEe5LKKhTl"
const val ednaMockAllowUntrustedSSLCertificate = true
