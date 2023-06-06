package io.edna.threads.demo.appCode.business.mockJsonProvider

import android.annotation.SuppressLint
import android.content.Context

class CurrentJsonProvider(private val context: Context) {
    private val preferences = context.getSharedPreferences("JsonPreferences", Context.MODE_PRIVATE)

    @SuppressLint("ApplySharedPref")
    fun saveCurrentJson(json: String) {
        preferences
            .edit()
            .putString(JSON_KEY, json)
            .commit()
    }

    fun getCurrentJson() = preferences.getString(JSON_KEY, "") ?: ""
}

private const val JSON_KEY = "JSON_KEY"
