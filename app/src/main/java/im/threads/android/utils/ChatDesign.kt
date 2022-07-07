package im.threads.android.utils

import android.content.Context
import androidx.annotation.StringRes
import im.threads.android.R
import im.threads.android.utils.PrefUtilsApp.storeTheme

/**
 * Варианты дизайна демо-приложения чата.
 *
 * @param nameResId id ресурса названия варианта дизайна
 */
enum class ChatDesign(
    @StringRes private val nameResId: Int
) {
    GREEN(R.string.demo_design_green_style),
    BLUE(R.string.demo_design_blue_style);

    fun getName(context: Context): String {
        return context.getString(nameResId)
    }

    companion object {
        @JvmStatic
        fun enumOf(context: Context, name: String): ChatDesign {
            for (design in values()) {
                if (design.getName(context).equals(name, ignoreCase = true)) {
                    return design
                }
            }
            return GREEN
        }

        @JvmStatic
        fun setTheme(context: Context, theme: ChatDesign) {
            storeTheme(context, theme)
        }
    }
}
