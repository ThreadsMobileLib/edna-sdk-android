package im.threads.android.core;

import android.app.PendingIntent;
import android.content.Context;
import android.text.TextUtils;

import androidx.multidex.MultiDexApplication;

import com.edna.android.push_lite.PushController;

import java.util.Collections;
import java.util.List;

import im.threads.ConfigBuilder;
import im.threads.ThreadsLib;
import im.threads.android.R;
import im.threads.android.data.Card;
import im.threads.android.data.TransportConfig;
import im.threads.android.push.HCMTokenRefresher;
import im.threads.android.ui.BottomNavigationActivity;
import im.threads.android.utils.PrefUtils;
import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;

public class ThreadsDemoApplication extends MultiDexApplication {

    private static final BehaviorSubject<Integer> unreadMessagesSubject = BehaviorSubject.create();
    private static Context appContext;
    private Disposable disposable;

    public static Context getAppContext() {
        return appContext;
    }

    public static BehaviorSubject<Integer> getUnreadMessagesSubject() {
        return unreadMessagesSubject;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        appContext = getApplicationContext();
        disposable = Completable.fromAction(() -> HCMTokenRefresher.requestToken(this))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        () -> {
                        },
                        e -> {
                        }
                );
        PushController.getInstance(this).init();

        ConfigBuilder configBuilder = new ConfigBuilder(this)
                .pendingIntentCreator(new CustomPendingIntentCreator())
                .unreadMessagesCountListener(unreadMessagesSubject::onNext)
                .surveyCompletionDelay(2000)
                .historyLoadingCount(50)
                .isDebugLoggingEnabled(true)
                .certificateRawResIds(Collections.singletonList(R.raw.edna));
        TransportConfig transportConfig = PrefUtils.getTransportConfig(this);
        if (transportConfig != null) {
            configBuilder.serverBaseUrl(transportConfig.getBaseUrl())
                    .threadsGateUrl(transportConfig.getThreadsGateUrl())
                    .threadsGateProviderUid(transportConfig.getThreadsGateProviderUid())
                    .threadsGateHCMProviderUid(transportConfig.getThreadsGateHCMProviderUid());
        }
        ThreadsLib.init(configBuilder);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        disposable.dispose();
    }

    private static class CustomPendingIntentCreator implements ThreadsLib.PendingIntentCreator {
        @Override
        public PendingIntent create(Context context, String appMarker) {
            if (!TextUtils.isEmpty(appMarker)) {
                //This is an example of creating pending intent for multi-chat app
                List<Card> clientCards = PrefUtils.getCards(context);
                Card pushClientCard = null;
                for (Card clientCard : clientCards) {
                    if (appMarker.equalsIgnoreCase(clientCard.getAppMarker())) {
                        pushClientCard = clientCard;
                    }
                }
                if (pushClientCard != null) {
                    return BottomNavigationActivity.createPendingIntent(
                            context,
                            pushClientCard.getUserId(),
                            pushClientCard.getClientData(),
                            pushClientCard.getAppMarker(),
                            pushClientCard.getClientIdSignature(),
                            pushClientCard.getAuthToken(),
                            pushClientCard.getAuthSchema(),
                            PrefUtils.getTheme(context)
                    );
                }
            } else {
                //This is an example of creating pending intent for single-chat app
                List<Card> clientCards = PrefUtils.getCards(context);
                if (!clientCards.isEmpty()) {
                    Card pushClientCard = clientCards.get(0);
                    return BottomNavigationActivity.createPendingIntent(
                            context,
                            pushClientCard.getUserId(),
                            pushClientCard.getClientData(),
                            pushClientCard.getAppMarker(),
                            pushClientCard.getClientIdSignature(),
                            pushClientCard.getAuthToken(),
                            pushClientCard.getAuthSchema(),
                            PrefUtils.getTheme(context)
                    );
                }
            }
            return null;
        }
    }
}
