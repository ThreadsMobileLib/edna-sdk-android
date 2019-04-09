package im.threads.android.network;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface IServerAPI {

    public static String API = "/api";

    @GET(API + "/auth/getSignature")
    Observable<SignatureResponse> getSignature(
            @Query("clientId") String clientId
    );

}
