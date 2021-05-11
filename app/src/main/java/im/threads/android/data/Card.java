package im.threads.android.data;

import java.io.Serializable;

import androidx.core.util.ObjectsCompat;

public class Card implements Serializable {
    private final String userId;
    private final String clientData;
    private final String appMarker;
    private final String clientIdSignature;
    private final String authToken;
    private final String authSchema;

    public Card(final String userId, String clientData, final String appMarker, final String clientIdSignature, final String authToken, final String authSchema) {
        this.userId = userId;
        this.clientData = clientData;
        this.appMarker = appMarker;
        this.clientIdSignature = clientIdSignature;
        this.authToken = authToken;
        this.authSchema = authSchema;
    }

    public String getUserId() {
        return userId;
    }

    public String getClientData() {
        return clientData;
    }

    public String getAppMarker() {
        return appMarker;
    }

    public String getClientIdSignature() {
        return clientIdSignature;
    }

    public String getAuthToken() {
        return authToken;
    }

    public String getAuthSchema() {
        return authSchema;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Card card = (Card) o;
        return ObjectsCompat.equals(userId, card.userId);
    }

    @Override
    public int hashCode() {
        return userId != null ? userId.hashCode() : 0;
    }
}
