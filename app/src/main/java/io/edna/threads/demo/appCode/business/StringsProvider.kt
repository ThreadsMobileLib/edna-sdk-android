package io.edna.threads.demo.appCode.business

import android.content.Context
import io.edna.threads.demo.R

class StringsProvider(private val context: Context) {
    val textMessages = context.getString(R.string.text_messages)
    val connectionErrors = context.getString(R.string.connection_errors)
    val voiceMessages = context.getString(R.string.voice_messages)
    val images = context.getString(R.string.images)
    val files = context.getString(R.string.files)
    val systemMessages = context.getString(R.string.system_messages)
    val chatWithBot = context.getString(R.string.chat_with_bot)
    val chatWithEditAndDeletedMessages = context.getString(R.string.deleted_and_edited_messages)
    val selectTheme = context.getString(R.string.select_theme)
    val defaultTheme = context.getString(R.string.default_theme)
    val darkTheme = context.getString(R.string.dark_theme)
    val lightTheme = context.getString(R.string.light_theme)
    val ok = context.getString(R.string.ok)
    val requiredField = context.getString(R.string.required_field)
}
