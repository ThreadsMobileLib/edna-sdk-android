package im.threads.android.utils

import android.content.Context
import android.util.Log
import androidx.preference.PreferenceManager
import com.google.gson.reflect.TypeToken
import im.threads.android.data.Card
import im.threads.android.data.TransportConfig
import im.threads.internal.Config

object PrefUtils {
    private const val TAG = "DemoAppPrefUtils "
    private const val PREF_CARDS_LIST = "PREF_CARDS_LIST"
    private const val PREF_SERVER_BASE_URL = "PREF_SERVER_BASE_URL"
    private const val PREF_THREADS_GATE_URL = "PREF_THREADS_GATE_URL"
    private const val PREF_THREADS_GATE_PROVIDER_UID = "PREF_THREADS_GATE_PROVIDER_UID"
    private const val PREF_THREADS_GATE_HCM_PROVIDER_UID = "PREF_THREADS_GATE_HCM_PROVIDER_UID"
    private const val PREF_THEME = "PREF_THEME"

    @JvmStatic
    fun storeCards(ctx: Context?, cards: List<Card?>?) {
        if (ctx == null || cards == null) {
            Log.i(TAG, "storeCards: ctx or bundle is null")
            return
        }
        val editor = PreferenceManager.getDefaultSharedPreferences(ctx).edit()
        editor.putString(PREF_CARDS_LIST, Config.instance.gson.toJson(cards))
        editor.commit()
    }

    @JvmStatic
    fun getCards(ctx: Context?): List<Card> {
        var cards: List<Card>? = null
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(ctx)
        if (sharedPreferences.getString(PREF_CARDS_LIST, null) != null) {
            val sharedPreferencesString = sharedPreferences.getString(PREF_CARDS_LIST, null)
            cards = Config.instance.gson.fromJson(
                sharedPreferencesString,
                object : TypeToken<List<Card>?>() {}.type
            )
        }
        return cards ?: ArrayList()
    }

    fun saveTransportConfig(ctx: Context, transportConfig: TransportConfig) {
        val editor = PreferenceManager.getDefaultSharedPreferences(ctx).edit()
        editor.putString(PREF_SERVER_BASE_URL, transportConfig.baseUrl)
        editor.putString(PREF_THREADS_GATE_URL, transportConfig.threadsGateUrl)
        editor.putString(PREF_THREADS_GATE_PROVIDER_UID, transportConfig.threadsGateProviderUid)
        editor.putString(
            PREF_THREADS_GATE_HCM_PROVIDER_UID,
            transportConfig.threadsGateHCMProviderUid
        )
        editor.commit()
    }

    @JvmStatic
    fun getTransportConfig(ctx: Context?): TransportConfig? {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(ctx)
        val baseUrl = sharedPreferences.getString(PREF_SERVER_BASE_URL, null)
            ?: return null
        val threadsGateUrl = sharedPreferences.getString(PREF_THREADS_GATE_URL, null)
            ?: return null
        val threadsGateProviderUid =
            sharedPreferences.getString(PREF_THREADS_GATE_PROVIDER_UID, null)
                ?: return null
        val threadsGateHCMProviderUid =
            sharedPreferences.getString(PREF_THREADS_GATE_HCM_PROVIDER_UID, null)
                ?: return null
        return TransportConfig(
            baseUrl = baseUrl,
            threadsGateUrl = threadsGateUrl,
            threadsGateProviderUid = threadsGateProviderUid,
            threadsGateHCMProviderUid = threadsGateHCMProviderUid
        )
    }

    @JvmStatic
    fun storeTheme(ctx: Context, theme: ChatDesign) {
        val editor = PreferenceManager.getDefaultSharedPreferences(ctx).edit()
        editor.putString(PREF_THEME, theme.getName(ctx))
        editor.commit()
    }

    @JvmStatic
    fun getTheme(ctx: Context): ChatDesign {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(ctx)
        val theme = sharedPreferences.getString(PREF_THEME, null) ?: ""
        return ChatDesign.enumOf(ctx, theme)
    }
}
