package io.edna.threads.demo.appCode.business

import android.content.Context
import im.threads.ui.extensions.isDarkThemeOn

class UiThemeProvider(private val context: Context) {
    fun isDarkThemeOn() = context.isDarkThemeOn()
}
