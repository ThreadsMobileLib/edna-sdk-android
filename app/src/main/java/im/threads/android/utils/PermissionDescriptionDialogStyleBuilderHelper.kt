package im.threads.android.utils

import im.threads.android.R
import im.threads.styles.permissions.ContentGravity
import im.threads.styles.permissions.PermissionDescriptionDialogStyle
import im.threads.styles.permissions.PermissionDescriptionType

/**
 *  Вспомогательный класс для настройки стиля диалога описания запроса доступов.
 */
object PermissionDescriptionDialogStyleBuilderHelper {

    private const val LATO_BOLD_FONT_PATH = "fonts/lato-bold.ttf"
    private const val LATO_LIGHT_FONT_PATH = "fonts/lato-light.ttf"
    private const val LATO_REGULAR_FONT_PATH = "fonts/lato-regular.ttf"

    @JvmStatic
    fun getDialogStyle(
        design: ChatDesign,
        type: PermissionDescriptionType
    ) = PermissionDescriptionDialogStyle.getDefaultDialogStyle(type).also { dialogStyle ->
        configureDialogStyleByChatDesign(design, dialogStyle)
    }

    private fun configureDialogStyleByChatDesign(
        design: ChatDesign,
        dialogStyle: PermissionDescriptionDialogStyle
    ) {
        when (design) {
            ChatDesign.GREEN -> {
                /* Используем дефолтные настройки
                 * PermissionDescriptionDialogStyle.getDefaultDialogStyle(type). */
            }
            ChatDesign.BLUE -> configureBlueDesign(dialogStyle)
        }
    }

    private fun configureBlueDesign(dialogStyle: PermissionDescriptionDialogStyle) {
        dialogStyle.apply {
            imageStyle.apply {
                marginTopDpResId = R.dimen.margin_big
                layoutGravity = ContentGravity.LEFT
            }
            titleStyle.apply {
                fontPath = LATO_BOLD_FONT_PATH
                textSizeSpResId = R.dimen.text_big
                textColorResId = R.color.threads_blue
                marginTopDpResId = R.dimen.margin_big
                gravity = ContentGravity.LEFT
            }
            messageStyle.apply {
                fontPath = LATO_LIGHT_FONT_PATH
                textSizeSpResId = R.dimen.text_medium
                textColorResId = R.color.threads_blue
                marginTopDpResId = R.dimen.margin_material
                gravity = ContentGravity.LEFT
            }
            positiveButtonStyle.apply {
                textResId = R.string.demo_continue
                fontPath = LATO_REGULAR_FONT_PATH
                textSizeSpResId = R.dimen.text_big
                textColorResId = R.color.threads_white
                marginTopDpResId = R.dimen.margin_material
                cornerRadiusDpResId = R.dimen.demo_radius_medium
                backgroundColorResId = R.color.threads_blue_0F87FF
            }
            negativeButtonStyle.apply {
                textResId = R.string.demo_skip
                fontPath = LATO_REGULAR_FONT_PATH
                textSizeSpResId = R.dimen.text_big
                textColorResId = R.color.threads_white
                marginTopDpResId = R.dimen.margin_quarter
                marginBottomDpResId = R.dimen.margin_big
                cornerRadiusDpResId = R.dimen.demo_radius_medium
                backgroundColorResId = R.color.threads_blue_transparent
                strokeColorResId = R.color.threads_blue
            }
            backgroundStyle.apply {
                cornerRadiusDpResId = R.dimen.demo_radius_medium
                backgroundColorResId = R.color.lighter_blue
                strokeColorResId = R.color.threads_blue
                strokeWidthDpResId = R.dimen.demo_stroke_width_medium
            }
        }
    }
}