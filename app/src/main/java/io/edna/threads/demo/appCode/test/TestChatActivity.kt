package io.edna.threads.demo.appCode.test

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import im.threads.business.annotation.OpenWay
import im.threads.ui.fragments.ChatFragment
import io.edna.threads.demo.R

class TestChatActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_chat)

        saveJsonMock(intent.extras?.getString(jsonMockExtraKey))

        supportFragmentManager.beginTransaction()
            .replace(R.id.chatFragmentContainer, ChatFragment.newInstance(OpenWay.DEFAULT))
            .commitNow()
    }

    override fun onStop() {
        saveJsonMock(null)
        super.onStop()
    }

    private fun saveJsonMock(jsonMock: String?) {
        applicationContext
            .getSharedPreferences(jsonMockPreferencesKey, Context.MODE_PRIVATE)
            .edit()
            .putString(jsonMockPreferencesValueKey, jsonMock)
            .commit()
    }

    companion object {
        const val jsonMockExtraKey = "jsonMockExtraKey"
        private const val jsonMockPreferencesKey = "ecc_demo_json_preference"
        private const val jsonMockPreferencesValueKey = "ecc_demo_json_preference_key"
    }
}
