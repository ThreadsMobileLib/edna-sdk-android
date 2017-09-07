package im.threads.android;

import android.app.Application;

import com.pushserver.android.PushController;

/**
 * Created by Vit on 17.08.2017.
 */

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        PushController.getInstance(this).init();
    }
}
