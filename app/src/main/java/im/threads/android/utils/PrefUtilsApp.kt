package im.threads.android.utils

import android.content.Context
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import im.threads.android.data.Card
import im.threads.android.data.ServerConfig
import im.threads.android.data.TransportConfig
import im.threads.internal.Config
import im.threads.internal.domain.logger.LoggerEdna
import java.io.File

object PrefUtilsApp {
    private const val PREF_CARDS_LIST = "PREF_CARDS_LIST"
    private const val PREF_SERVER_BASE_URL = "PREF_SERVER_BASE_URL"
    private const val PREF_DATASTORE_URL = "PREF_DATASTORE_URL"
    private const val PREF_THREADS_GATE_URL = "PREF_THREADS_GATE_URL"
    private const val PREF_THREADS_GATE_PROVIDER_UID = "PREF_THREADS_GATE_PROVIDER_UID"
    private const val PREF_THREADS_GATE_HCM_PROVIDER_UID = "PREF_THREADS_GATE_HCM_PROVIDER_UID"
    private const val PREF_IS_NEW_CHAT_CENTER_API = "PREF_IS_NEW_CHAT_CENTER_API"
    private const val PREF_THEME = "PREF_THEME"
    private const val PREF_SERVERS_LIST = "SERVERS_LIST_PREFS"
    private const val PREF_SELECTED_SERVER_NAME = "SELECTED_SERVER_NAME_PREFS"
    private const val PREF_IMPORTED_FILE_SERVERS_NAME = "servers_config"
    private const val PREF_CURRENT_SERVER = "PREF_CURRENT_SERVER"
    private const val PREF_IS_SERVER_CHANGED = "PREF_IS_SERVER_CHANGED"

    @JvmStatic
    fun storeCards(ctx: Context?, cards: List<Card?>?) {
        if (ctx == null || cards == null) {
            LoggerEdna.info("storeCards: ctx or bundle is null")
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
        editor.putString(PREF_DATASTORE_URL, transportConfig.datastoreUrl)
        editor.putString(PREF_THREADS_GATE_URL, transportConfig.threadsGateUrl)
        editor.putString(PREF_THREADS_GATE_PROVIDER_UID, transportConfig.threadsGateProviderUid)
        editor.putString(
            PREF_THREADS_GATE_HCM_PROVIDER_UID,
            transportConfig.threadsGateHCMProviderUid
        )
        editor.putBoolean(PREF_IS_NEW_CHAT_CENTER_API, transportConfig.isNewChatCenterApi)
        editor.commit()
    }

    @JvmStatic
    fun getTransportConfig(ctx: Context?): TransportConfig? {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(ctx)
        val baseUrl = sharedPreferences.getString(PREF_SERVER_BASE_URL, null)
            ?: return null
        val datastoreUrl = sharedPreferences.getString(PREF_DATASTORE_URL, null)
            ?: return null
        val threadsGateUrl = sharedPreferences.getString(PREF_THREADS_GATE_URL, null)
            ?: return null
        val threadsGateProviderUid =
            sharedPreferences.getString(PREF_THREADS_GATE_PROVIDER_UID, null)
                ?: return null
        val threadsGateHCMProviderUid =
            sharedPreferences.getString(PREF_THREADS_GATE_HCM_PROVIDER_UID, null)
        val isNewChatCenterApi = sharedPreferences.getBoolean(PREF_IS_NEW_CHAT_CENTER_API, false)
        return TransportConfig(
            baseUrl = baseUrl,
            datastoreUrl = datastoreUrl,
            threadsGateUrl = threadsGateUrl,
            threadsGateProviderUid = threadsGateProviderUid,
            threadsGateHCMProviderUid = threadsGateHCMProviderUid,
            isNewChatCenterApi
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

    @JvmStatic
    fun addServers(context: Context, servers: Map<String, String>, clearExisting: Boolean = false) {
        val prefsEditor = context.getSharedPreferences(PREF_SERVERS_LIST, Context.MODE_PRIVATE).edit()
        if (clearExisting) prefsEditor.clear()
        servers.forEach { prefsEditor.putString(it.key, it.value) }
        prefsEditor.commit()
    }

    fun getAllServers(context: Context): Map<String, String> {
        return getServersFrom(context, PREF_SERVERS_LIST)
    }

    @JvmStatic
    fun setCurrentServer(context: Context, currentServerName: String) {
        context.getSharedPreferences(PREF_SELECTED_SERVER_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(PREF_CURRENT_SERVER, currentServerName)
            .commit()
    }

    @JvmStatic
    fun getCurrentServer(context: Context): String {
        return context.getSharedPreferences(PREF_SELECTED_SERVER_NAME, Context.MODE_PRIVATE)
            .getString(PREF_CURRENT_SERVER, "") ?: ""
    }

    @JvmStatic
    fun setIsServerChanged(context: Context, isChanged: Boolean) {
        context.getSharedPreferences(PREF_SELECTED_SERVER_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(PREF_IS_SERVER_CHANGED, isChanged)
            .commit()
    }

    @JvmStatic
    fun getIsServerChanged(context: Context): Boolean {
        return context.getSharedPreferences(PREF_SELECTED_SERVER_NAME, Context.MODE_PRIVATE)
            .getBoolean(PREF_IS_SERVER_CHANGED, false)
    }

    @JvmStatic
    fun applyServersFromFile(context: Context) {
        val serversFromApp = getAllServers(context)
            .map { Gson().fromJson<ServerConfig>(it.value) }
            .filter { it.isFromApp }
        val servers = getServersFrom(context, PREF_IMPORTED_FILE_SERVERS_NAME)
            .map { Gson().fromJson<ServerConfig>(it.value) }
            .toMutableList()
        servers.addAll(serversFromApp)
        val serversToSave = servers.associate { it.name to it.toJson() }
        addServers(context, serversToSave, true)
        deletePreferenceWithNameContains(context, PREF_IMPORTED_FILE_SERVERS_NAME)
    }

    @Suppress("UNCHECKED_CAST")
    private fun getServersFrom(context: Context, prefsName: String): Map<String, String> {
        return context
            .getSharedPreferences(prefsName, Context.MODE_PRIVATE)
            .all as? Map<String, String> ?: HashMap()
    }

    private fun deletePreferenceWithNameContains(context: Context, nameContains: String) {
        try {
            val dir = File(context.filesDir.parent + "/shared_prefs/")
            val children = dir.list()
            if (children != null) {
                for (child in children) {
                    if (child.contains(nameContains)) {
                        File(dir, child).delete()
                    }
                }
            }
        } catch (exception: Exception) {
            LoggerEdna.error("Error when deleting preference file", exception)
        }
    }
}
