package im.threads.android.network;

import android.text.TextUtils;
import android.util.Log;

import java.util.concurrent.TimeUnit;

import im.threads.android.R;
import im.threads.android.core.ThreadsDemoApplication;
import im.threads.config.HttpClientSettings;
import im.threads.internal.Config;
import im.threads.internal.model.SslSocketFactoryConfig;
import im.threads.internal.utils.SSLCertificateInterceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

class ServerAPI {

    private static final String TAG = ServerAPI.class.getSimpleName();
    private static IServerAPI serverAPI;

    static IServerAPI getAPI() {
        String serverBaseUrl = ThreadsDemoApplication.getAppContext().getString(R.string.server_base_url);
        if (TextUtils.isEmpty(serverBaseUrl)) {
            Log.w(TAG, "Server base url is empty");
            return null;
        } else {
            if (serverAPI == null) {
                serverAPI = createServerAPI(serverBaseUrl);
            }
            return serverAPI;
        }
    }

    private static IServerAPI createServerAPI(String serverBaseUrl) {
        Config config = Config.instance;
        HttpClientSettings httpSettings = config.requestConfig.getAuthHttpClientSettings();
        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(serverBaseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create());
        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder()
                .addInterceptor(new HttpLoggingInterceptor()
                        .setLevel(HttpLoggingInterceptor.Level.BODY))
                .connectTimeout(httpSettings.getConnectTimeoutMillis(), TimeUnit.MILLISECONDS)
                .readTimeout(httpSettings.getReadTimeoutMillis(), TimeUnit.MILLISECONDS)
                .writeTimeout(httpSettings.getWriteTimeoutMillis(), TimeUnit.MILLISECONDS);
        if (Config.instance.isDebugLoggingEnabled) {
            httpClientBuilder.addInterceptor(new SSLCertificateInterceptor());
        }
        SslSocketFactoryConfig sslSocketFactoryConfig = config.sslSocketFactoryConfig;
        if (sslSocketFactoryConfig != null) {
            httpClientBuilder.sslSocketFactory(
                    sslSocketFactoryConfig.getSslSocketFactory(),
                    sslSocketFactoryConfig.getTrustManager()
            );
            httpClientBuilder.hostnameVerifier((hostname, session) -> true);
        }
        builder.client(httpClientBuilder.build());
        Retrofit retrofit = builder.build();
        return retrofit.create(IServerAPI.class);
    }
}
