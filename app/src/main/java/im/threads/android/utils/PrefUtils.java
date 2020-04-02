package im.threads.android.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

import im.threads.android.data.Card;
import im.threads.internal.Config;

public final class PrefUtils {
    private static final String TAG = "DemoAppPrefUtils ";
    private static final String PREF_CARDS_LIST = "PREF_CARDS_LIST";

    private PrefUtils() {
    }

    public static void storeCards(Context ctx, List<Card> cards) {
        if (ctx == null || cards == null) {
            Log.i(TAG, "storeCards: ctx or bundle is null");
            return;
        }
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(ctx).edit();
        editor.putString(PREF_CARDS_LIST, Config.instance.gson.toJson(cards));
        editor.commit();
    }

    public static List<Card> getCards(Context ctx) {
        List<Card> cards = null;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(ctx);
        if (sharedPreferences.getString(PREF_CARDS_LIST, null) != null) {
            String sharedPreferencesString = sharedPreferences.getString(PREF_CARDS_LIST, null);
            cards = Config.instance.gson.fromJson(sharedPreferencesString, new TypeToken<List<Card>>() {
            }.getType());
        }
        return cards == null ? new ArrayList<>() : cards;
    }
}
