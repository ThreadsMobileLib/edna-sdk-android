package im.threads.android.core;

import android.app.PendingIntent;
import android.content.Context;
import android.text.TextUtils;

import androidx.multidex.MultiDexApplication;

import java.util.List;

import im.threads.ConfigBuilder;
import im.threads.ThreadsLib;
import im.threads.android.data.Card;
import im.threads.android.ui.BottomNavigationActivity;
import im.threads.android.utils.ChatStyleBuilderHelper;
import im.threads.android.utils.PrefUtils;
import io.reactivex.subjects.BehaviorSubject;

public class ThreadsDemoApplication extends MultiDexApplication {

    private static Context appContext;

    private static BehaviorSubject<Integer> unreadMessagesSubject = BehaviorSubject.create();

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
        ConfigBuilder configBuilder = new ConfigBuilder(this)
                .pendingIntentCreator(new CustomPendingIntentCreator())
                .unreadMessagesCountListener(count -> unreadMessagesSubject.onNext(count))
                .surveyCompletionDelay(2000)
                .historyLoadingCount(50)
                .isDebugLoggingEnabled(true);
        ThreadsLib.init(configBuilder);
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
                    ChatStyleBuilderHelper.ChatDesign chatDesign = ChatStyleBuilderHelper.ChatDesign.BLUE;
                    if (appMarker.endsWith("CRG")) {
                        chatDesign = ChatStyleBuilderHelper.ChatDesign.GREEN;
                    }
                    return BottomNavigationActivity.createPendingIntent(
                            context,
                            pushClientCard.getUserId(),
                            pushClientCard.getClientData(),
                            pushClientCard.getAppMarker(),
                            pushClientCard.getClientIdSignature(),
                            pushClientCard.getAuthToken(),
                            pushClientCard.getAuthSchema(),
                            chatDesign
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
                            ChatStyleBuilderHelper.ChatDesign.GREEN
                    );
                }
            }
            return null;
        }
    }
}
