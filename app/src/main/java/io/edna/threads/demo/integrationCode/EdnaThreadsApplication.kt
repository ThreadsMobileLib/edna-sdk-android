package io.edna.threads.demo.integrationCode

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.perf.FirebasePerformance
import com.microsoft.appcenter.AppCenter
import com.microsoft.appcenter.analytics.Analytics
import com.microsoft.appcenter.crashes.Crashes
import io.edna.threads.demo.BuildConfig
import io.edna.threads.demo.appCode.business.PreferencesProvider
import io.edna.threads.demo.appCode.business.ServersProvider
import io.edna.threads.demo.appCode.business.appModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class EdnaThreadsApplication : Application() {
    private val serversProvider: ServersProvider by inject()
    private val preferences: PreferencesProvider by inject()
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    override fun onCreate() {
        // Инициализация ThreadsLib находится в integrationCode/LaunchViewModel/login(...)
        super.onCreate()
        context = this.applicationContext

        startAppCenter()

        startKoin {
            androidContext(this@EdnaThreadsApplication)
            modules(appModule)
        }

        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)
        FirebasePerformance.getInstance().isPerformanceCollectionEnabled = !BuildConfig.DEBUG
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

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
    }
}

const val ednaMockScheme = "http"
const val ednaMockHost = "localhost"
const val ednaMockPort = 8080
const val ednaMockUrl = "$ednaMockScheme://$ednaMockHost:$ednaMockPort/"
const val ednaMockThreadsGateUrl = "ws://$ednaMockHost:$ednaMockPort/gate/socket"
const val ednaMockThreadsGateProviderUid = "TEST_93jLrtnipZsfbTddRfEfbyfEe5LKKhTl"
const val ednaMockAllowUntrustedSSLCertificate = true
