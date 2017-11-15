package im.threads.android.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

import im.threads.android.data.Card;

public final class PrefUtils {
    private static final String TAG = "DemoAppPrefUtils ";
    public static final String PREF_CARDS_LIST = "PREF_CARDS_LIST";

    private PrefUtils() {
    }

    public static void storeCards(Context ctx, List<Card> cards) {
        if (ctx == null || cards == null) {
            Log.i(TAG, "storeCards: ctx or bundle is null");
            return;
        }
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(ctx).edit();
        editor.putString(PREF_CARDS_LIST, new Gson().toJson(cards));
        editor.commit();
    }

    public static List<Card> getCards(Context ctx) {
        List<Card> cards = null;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(ctx);
        if (sharedPreferences.getString(PREF_CARDS_LIST, null) != null) {
            String sharedPreferencesString = sharedPreferences.getString(PREF_CARDS_LIST, null);
            cards = new Gson().fromJson(sharedPreferencesString, new TypeToken<List<Card>>(){}.getType());
        }
        return cards == null ? new ArrayList<Card>() : cards;
    }
}
