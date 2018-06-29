package im.threads.android.data;

import java.io.Serializable;

public class Card implements Serializable {
    private String userId;
    private String userName;
    private String appMarker;

    public Card(){}

    public Card(final String userId, final String userName, String appMarker) {
        this.userId = userId;
        this.userName = userName;
        this.appMarker = appMarker;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public String getAppMarker() {
        return appMarker;
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

        return userId != null ? userId.equals(card.userId) : card.userId == null;

    }

    @Override
    public int hashCode() {
        return userId != null ? userId.hashCode() : 0;
    }
}
