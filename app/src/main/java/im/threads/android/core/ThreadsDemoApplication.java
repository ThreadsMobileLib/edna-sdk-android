package im.threads.android.core;

import android.app.PendingIntent;
import android.content.Context;
import android.support.multidex.MultiDexApplication;
import android.text.TextUtils;

import java.util.List;

import im.threads.android.data.Card;
import im.threads.android.ui.BottomNavigationActivity;
import im.threads.android.utils.ChatBuilderHelper;
import im.threads.android.utils.PrefUtils;
import im.threads.controllers.ChatController;

public class ThreadsDemoApplication extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();

        ChatController.setPendingIntentCreator(new ChatController.PendingIntentCreator() {
            @Override
            public PendingIntent createPendingIntent(Context context, String appMarker) {
                if (!TextUtils.isEmpty(appMarker)) {
                    //This is an exaple of creating pending intent for multichat app

                    List<Card> clientCards = PrefUtils.getCards(context);
                    Card pushClientCard = null;

                    for (Card clientCard : clientCards) {
                        if (appMarker.equalsIgnoreCase(clientCard.getAppMarker())) {
                            pushClientCard = clientCard;
                        }
                    }

                    if (pushClientCard != null) {

                        ChatBuilderHelper.ChatDesign chatDesign = ChatBuilderHelper.ChatDesign.BLUE;
                        if (appMarker.endsWith("CRG")) {
                            chatDesign = ChatBuilderHelper.ChatDesign.GREEN;
                        }

                        return BottomNavigationActivity.createPendingIntent(context, true,
                                pushClientCard.getUserId(), pushClientCard.getUserName(),
                                pushClientCard.getAppMarker(), chatDesign);
                    }
                } else {
                    //This is an exaple of creating pending intent for single chat app
                    List<Card> clientCards = PrefUtils.getCards(context);

                    if (!clientCards.isEmpty()) {
                        Card pushClientCard = clientCards.get(0);

                        return BottomNavigationActivity.createPendingIntent(context, true,
                                pushClientCard.getUserId(), pushClientCard.getUserName(),
                                pushClientCard.getAppMarker(), ChatBuilderHelper.ChatDesign.GREEN);
                    }
                }

                return null;
            }
        });
    }
}
