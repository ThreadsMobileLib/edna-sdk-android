package im.threads.android.broadcastReceivers;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import im.threads.BuildConfig;
import im.threads.push.ThreadsPushBroadcastReceiver;

public class CustomPushBroadcastReceiver extends ThreadsPushBroadcastReceiver {

    private static final String TAG = CustomPushBroadcastReceiver.class.getSimpleName();

    /**
     * If threads is used in companion with mfms push library,
     * short push message processing can be implemented here.
     */
    @Override
    protected void onNewPushNotification(final Context context, final String alert, final Bundle bundle) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "onNewPushNotification");
        }
        super.onNewPushNotification(context, alert, bundle);
    }
}
