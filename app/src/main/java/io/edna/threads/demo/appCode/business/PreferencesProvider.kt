package io.edna.threads.demo.appCode.business

import android.content.Context
import com.google.gson.Gson
import io.edna.threads.demo.appCode.models.ServerConfig
import io.edna.threads.demo.appCode.models.UserInfo

class PreferencesProvider(private val context: Context) {
    fun putJsonToPreferences(json: String) {
        context
            .getSharedPreferences(preferenceName, Context.MODE_PRIVATE)
            .edit()
            .putString(jsonPreferenceKey, json)
            .putBoolean(isDemoModeEnabledKey, true)
            .commit()
    }

    fun cleanJsonOnPreferences() {
        context
            .getSharedPreferences(preferenceName, Context.MODE_PRIVATE)
            .edit()
            .putString(jsonPreferenceKey, "")
            .putBoolean(isDemoModeEnabledKey, false)
            .commit()
    }

    fun saveUserList(value: ArrayList<UserInfo>) {
        val prefsEditor = context.getSharedPreferences(PREF_DEMO, Context.MODE_PRIVATE).edit()
        prefsEditor.putString(PREF_USER_LIST, Gson().toJson(value))
        prefsEditor.apply()
    }

    fun getAllUserList(): ArrayList<UserInfo> {
        val userListString = context.getSharedPreferences(PREF_DEMO, Context.MODE_PRIVATE)
            .getString(PREF_USER_LIST, "[]") ?: "[]"
        val userArray: Array<UserInfo> =
            Gson().fromJson(userListString, Array<UserInfo>::class.java)
        val list: ArrayList<UserInfo> = ArrayList()
        list.addAll(userArray)
        return list
    }

    fun saveAppVersion(value: String) {
        val prefsEditor = context.getSharedPreferences(PREF_DEMO, Context.MODE_PRIVATE).edit()
        prefsEditor.putString(PREF_APP_VERSION, value)
        prefsEditor.apply()
    }

    fun getSavedAppVersion(): String {
        return context.getSharedPreferences(PREF_DEMO, Context.MODE_PRIVATE)
            .getString(PREF_APP_VERSION, "1.0.0") ?: "1.0.0"
    }

    fun saveSelectedUser(value: UserInfo) {
        val prefsEditor = context.getSharedPreferences(PREF_DEMO, Context.MODE_PRIVATE).edit()
        prefsEditor.putString(PREF_SELECTED_USER, Gson().toJson(value))
        prefsEditor.apply()
    }

    fun getSelectedUser(): UserInfo? {
        val userString = context.getSharedPreferences(PREF_DEMO, Context.MODE_PRIVATE)
            .getString(PREF_SELECTED_USER, "")
        return Gson().fromJson(userString, UserInfo::class.java)
    }

    fun saveSelectedServer(value: ServerConfig) {
        val prefsEditor = context.getSharedPreferences(PREF_DEMO, Context.MODE_PRIVATE).edit()
        prefsEditor.putString(PREF_SELECTED_SERVER, Gson().toJson(value))
        prefsEditor.apply()
    }

    fun getSelectedServer(): ServerConfig? {
        val serverString = context.getSharedPreferences(PREF_DEMO, Context.MODE_PRIVATE)
            .getString(PREF_SELECTED_SERVER, "")
        return Gson().fromJson(serverString, ServerConfig::class.java)
    }

    fun saveServers(value: ArrayList<ServerConfig>) {
        val prefsEditor = context.getSharedPreferences(PREF_DEMO, Context.MODE_PRIVATE).edit()
        prefsEditor.putString(PREF_SERVERS_LIST, Gson().toJson(value))
        prefsEditor.apply()
    }

    fun getAllServers(): ArrayList<ServerConfig> {
        val configString = context.getSharedPreferences(PREF_DEMO, Context.MODE_PRIVATE)
            .getString(PREF_SERVERS_LIST, "[]") ?: "[]"
        val serverArray: Array<ServerConfig> =
            Gson().fromJson(configString, Array<ServerConfig>::class.java)
        val list: ArrayList<ServerConfig> = ArrayList()
        list.addAll(serverArray)
        return list
    }

    companion object {
        private const val preferenceName = "ecc_demo_json_preference"
        private const val jsonPreferenceKey = "ecc_demo_json_preference_key"
        private const val isDemoModeEnabledKey = "ecc_is_demo_mode_enabled_key"
        private const val PREF_DEMO = "DEMO_PREFS"
        private const val PREF_USER_LIST = "USER_LIST_PREFS"
        private const val PREF_SERVERS_LIST = "SERVERS_LIST_PREFS"
        private const val PREF_APP_VERSION = "APP_VERSION"
        private const val PREF_SELECTED_USER = "SELECTED_USER_PREFS"
        private const val PREF_SELECTED_SERVER = "SELECTED_SERVER_PREFS"
    }
}
