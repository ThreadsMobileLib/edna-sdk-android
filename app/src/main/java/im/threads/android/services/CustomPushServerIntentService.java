package im.threads.android.services;

import android.util.Log;

import com.mfms.android.push_lite.repo.push.remote.model.PushMessage;

import java.util.List;

import im.threads.BuildConfig;
import im.threads.push.ThreadsPushServerIntentService;

public class CustomPushServerIntentService extends ThreadsPushServerIntentService {

    private static final String TAG = CustomPushServerIntentService.class.getSimpleName();

    /**
     * If threads is used in companion with mfms push library,
     * full push message processing can be implemented here
     */
    @Override
    protected boolean saveMessages(List<PushMessage> list) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "saveMessages");
        }
        return super.saveMessages(list);
    }
}
