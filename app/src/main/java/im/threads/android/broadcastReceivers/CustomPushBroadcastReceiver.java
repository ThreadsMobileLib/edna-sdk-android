package im.threads.android.broadcastReceivers;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.edna.android.push_lite.repo.push.remote.model.PushMessage;

import java.util.List;

import androidx.annotation.Nullable;
import im.threads.BuildConfig;
import im.threads.push.ThreadsPushBroadcastReceiver;

public class CustomPushBroadcastReceiver extends ThreadsPushBroadcastReceiver {

    private static final String TAG = CustomPushBroadcastReceiver.class.getSimpleName();

    /**
     * If threads is used in companion with mfms push library,
     * short push message processing can be implemented here.
     */
    @Override
    protected void onShortPushReceived(Context context, @Nullable String messageId, @Nullable String alert, Bundle bundle) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "onNewPushNotification");
        }
        super.onShortPushReceived(context, messageId, alert, bundle);
    }


    @Override
    protected boolean onLongPushReceived(Context context, List<PushMessage> pushMessages) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "saveMessages");
        }
        return super.onLongPushReceived(context, pushMessages);
    }
}
