package im.threads.android.network;

import im.threads.android.R;
import im.threads.android.core.ThreadsDemoApplication;
import io.reactivex.Observable;

public class AuthProvider {

    public static Observable<String> getSignature(String clientId) {

        if (ServerAPI.getAPI() == null) {
            return Observable.error(new IllegalStateException(
                    ThreadsDemoApplication.getAppContext().getString(R.string.get_signature_error)));
        } else {
            return ServerAPI.getAPI().getSignature(clientId).map(signatureResponse -> signatureResponse.signature);
        }
    }

}
