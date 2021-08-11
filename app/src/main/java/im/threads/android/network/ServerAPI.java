package im.threads.android.network;

import android.text.TextUtils;
import android.util.Log;
import im.threads.android.R;
import im.threads.android.core.ThreadsDemoApplication;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.concurrent.TimeUnit;

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
        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(serverBaseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create());
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder()
                .addInterceptor(new HttpLoggingInterceptor()
                        .setLevel(HttpLoggingInterceptor.Level.BODY));
        httpClient.connectTimeout(2, TimeUnit.SECONDS);
        builder.client(httpClient.build());
        Retrofit retrofit = builder.build();
        return retrofit.create(IServerAPI.class);
    }
}
