package im.threads.android.network;

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

    private static IServerAPI serverAPI;

    public static IServerAPI getAPI() {
        if (serverAPI == null) {
            serverAPI = createServerAPI();
        }

        return serverAPI;
    }

    private static IServerAPI createServerAPI() {

        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(ThreadsDemoApplication.getAppContext().getString(R.string.serverBaseUrl))
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create());

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

        ChatStyle.updateContext(ThreadsDemoApplication.getAppContext());
        if (ChatStyle.getInstance().isDebugLoggingEnabled) {
            httpClient.addInterceptor(new HttpLoggingInterceptor()
                    .setLevel(HttpLoggingInterceptor.Level.BODY));
        }

        httpClient.connectTimeout(30, TimeUnit.SECONDS);
        builder.client(httpClient.build());

        Retrofit retrofit = builder.build();

        return retrofit.create(IServerAPI.class);
    }

}
