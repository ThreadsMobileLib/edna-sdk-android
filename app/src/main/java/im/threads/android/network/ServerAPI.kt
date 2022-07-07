package im.threads.android.network

import android.text.TextUtils
import android.util.Log
import com.pandulapeter.beagle.logOkHttp.BeagleOkHttpLogger
import im.threads.android.core.ThreadsDemoApplication.Companion.appContext
import im.threads.android.use_cases.developer_options.DebugMenuInteractor
import im.threads.android.use_cases.developer_options.DebugMenuUseCase
import im.threads.internal.Config
import im.threads.internal.utils.SSLCertificateInterceptor
import okhttp3.Interceptor
import okhttp3.OkHttpClient.Builder
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLSession

internal object ServerAPI {
    private val TAG = ServerAPI::class.java.simpleName
    private var serverAPI: IServerAPI? = null

    private val developerOptions: DebugMenuUseCase = DebugMenuInteractor(appContext)

    @JvmStatic
    val aPI: IServerAPI?
        get() {
            val serverBaseUrl = developerOptions.getCurrentServer().serverBaseUrl
            return if (TextUtils.isEmpty(serverBaseUrl)) {
                Log.w(TAG, "Server base url is empty")
                null
            } else {
                if (serverAPI == null) {
                    serverAPI = createServerAPI(serverBaseUrl)
                }
                serverAPI
            }
        }

    private fun createServerAPI(serverBaseUrl: String): IServerAPI {
        val config = Config.instance
        val (connectTimeoutMillis, readTimeoutMillis, writeTimeoutMillis) =
            config.requestConfig.authHttpClientSettings
        val builder = Retrofit.Builder()
            .baseUrl(serverBaseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        val httpClientBuilder = Builder()
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .apply { (BeagleOkHttpLogger.logger as? Interceptor?)?.let { addInterceptor(it) } }
            .connectTimeout(connectTimeoutMillis.toLong(), TimeUnit.MILLISECONDS)
            .readTimeout(readTimeoutMillis.toLong(), TimeUnit.MILLISECONDS)
            .writeTimeout(writeTimeoutMillis.toLong(), TimeUnit.MILLISECONDS)
        if (Config.instance.isDebugLoggingEnabled) {
            httpClientBuilder.addInterceptor(SSLCertificateInterceptor())
        }
        val sslSocketFactoryConfig = config.sslSocketFactoryConfig
        if (sslSocketFactoryConfig != null) {
            httpClientBuilder.sslSocketFactory(
                sslSocketFactoryConfig.sslSocketFactory,
                sslSocketFactoryConfig.trustManager
            )
            httpClientBuilder.hostnameVerifier(HostnameVerifier { hostname: String?, session: SSLSession? -> true })
        }
        builder.client(httpClientBuilder.build())
        val retrofit = builder.build()
        return retrofit.create(IServerAPI::class.java)
    }
}
