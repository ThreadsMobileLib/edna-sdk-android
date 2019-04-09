package im.threads.android.network;

import io.reactivex.Observable;

public class AuthProvider {

    public static Observable<String> getSignature(String clientId) {
        return ServerAPI.getAPI().getSignature(clientId).map(signatureResponse -> signatureResponse.signature);
    }

}
