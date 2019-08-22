package im.threads.android.network;

import android.text.TextUtils;
import android.util.Log;

import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import java.util.concurrent.TimeUnit;

import im.threads.android.R;
import im.threads.android.core.ThreadsDemoApplication;
import im.threads.model.ChatStyle;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ServerAPI {

    private static String LOG_TAG = ServerAPI.class.getSimpleName();
    private static IServerAPI serverAPI;

    public static IServerAPI getAPI() {

        String serverBaseUrl = ThreadsDemoApplication.getAppContext().getString(R.string.serverBaseUrl);

        if (TextUtils.isEmpty(serverBaseUrl)) {
            Log.w(LOG_TAG, "Server base url is empty");
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

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

        ChatStyle.updateContext(ThreadsDemoApplication.getAppContext());
        if (ChatStyle.getInstance().isDebugLoggingEnabled) {
            httpClient.addInterceptor(new HttpLoggingInterceptor()
                    .setLevel(HttpLoggingInterceptor.Level.BODY));
        }

        httpClient.connectTimeout(2, TimeUnit.SECONDS);
        builder.client(httpClient.build());

        Retrofit retrofit = builder.build();

        return retrofit.create(IServerAPI.class);
    }

}
