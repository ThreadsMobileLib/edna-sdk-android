package io.edna.threads.demo.appCode.business.mockJsonProvider

import android.content.Context
import io.edna.threads.demo.R
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader

class SamplesJsonProvider(private val context: Context) {
    fun getTextChatJson() = readTextFileFromRawResourceId(R.raw.history_text_response)

    fun getConnectionErrorJson() = readTextFileFromRawResourceId(R.raw.history_errors_response)

    fun getVoicesChatJson() = readTextFileFromRawResourceId(R.raw.history_voice_response)

    fun getImagesChatJson() = readTextFileFromRawResourceId(R.raw.history_images_response)

    fun getFilesChatJson() = readTextFileFromRawResourceId(R.raw.history_files_response)

    fun getSystemChatJson() = readTextFileFromRawResourceId(R.raw.history_system_response)

    fun getChatBotJson() = readTextFileFromRawResourceId(R.raw.history_bot_response)

    fun getChatWithEditAndDeletedMessages() = readTextFileFromRawResourceId(R.raw.history_deleted_and_edited_messages)

    private fun readTextFileFromRawResourceId(resourceId: Int): String {
        var string: String? = ""
        val stringBuilder = StringBuilder()
        val inputStream: InputStream = context.resources.openRawResource(resourceId)
        val reader = BufferedReader(InputStreamReader(inputStream))
        while (true) {
            try {
                if (reader.readLine().also { string = it } == null) break
            } catch (e: IOException) {
                e.printStackTrace()
            }
            stringBuilder.append(string).append("\n")
        }
        inputStream.close()
        return stringBuilder.toString()
    }
}
