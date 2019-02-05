package im.threads.android.utils;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.StringRes;
import im.threads.android.R;
import im.threads.model.ChatStyle;

public class ChatBuilderHelper {

    private static final String LATO_BOLD_FONT_PATH = "fonts/lato-bold.ttf";
    private static final String LATO_LIGHT_FONT_PATH = "fonts/lato-light.ttf";
    private static final String LATO_REGULAR_FONT_PATH = "fonts/lato-regular.ttf";

    public enum ChatDesign {
        GREEN(R.string.design_green_enum),
        BLUE(R.string.design_blue_enum);

        @StringRes
        private int nameResId;

        ChatDesign(@StringRes int nameResId) {
            this.nameResId = nameResId;
        }

        public String getName(Context context) {
            return context.getString(nameResId);
        }

        public static ChatDesign enumOf(Context context, String name) {
            for (ChatDesign design : ChatDesign.values()) {
                if (design.getName(context).equalsIgnoreCase(name)) {
                    return design;
                }
            }
            return GREEN;
        }
    }

    public static ChatStyle buildChatStyle(Activity activity, String appMarker, String clientId, String userName, String data, ChatDesign design) {
        ChatStyle.ChatStyleBuilder builder = ChatStyle.ChatStyleBuilder
                .getBuilder(activity, clientId, userName, data) // в последнем параметре в виде строки можно передать любую дополнительную информацию, напр. "{balance:"1000.00", fio:"Vasya Pupkin"}"
                .setAppMarker(appMarker)
                .setDefaultFontBold(LATO_BOLD_FONT_PATH)
                .setDefaultFontLight(LATO_LIGHT_FONT_PATH)
                .setDefaultFontRegular(LATO_REGULAR_FONT_PATH)
                .showChatBackButton(true)// показывать кнопку назад
                .setShowConsultSearching(true) //показывать загрузку при поиске консультанта
                .setUseExternalCameraApp(true)
                .setScrollChatToEndIfUserTyping(false) // не прокручивать чат к последнему сообщению, если пользователь начал ввод
                .setDebugLoggingEnabled(true);

        switch (design) {
            case GREEN: {
                configureGreenDesign(builder);
                break;
            }
            case BLUE: {
                configureBlueDesign(builder);
                break;
            }
        }

        return builder.build();
    }

    private static void configureGreenDesign(ChatStyle.ChatStyleBuilder builder) {
        //Do nothing, using default threads design
    }

    private static void configureBlueDesign(ChatStyle.ChatStyleBuilder builder) {

        builder
                .setWelcomeScreenStyle(R.drawable.alt_threads_welcome_logo,
                        R.string.alt_threads_welcome_screen_title_text,
                        R.string.alt_threads_welcome_screen_subtitle_text,
                        R.color.alt_threads_welcome_screen_title,
                        R.color.alt_threads_welcome_screen_subtitle,
                        R.dimen.alt_threads_welcome_screen_title,
                        R.dimen.alt_threads_welcome_screen_subtitle)
                .setChatBodyStyle(R.color.alt_threads_chat_background,
                        R.color.alt_threads_chat_highlighting,
                        R.color.alt_threads_chat_incoming_message_bubble,
                        R.color.alt_threads_chat_outgoing_message_bubble,
                        R.drawable.alt_thread_incoming_bubble,
                        R.drawable.alt_thread_outgoing_bubble,
                        R.color.alt_threads_incoming_message_text,
                        R.color.alt_threads_outgoing_message_text,
                        R.color.alt_threads_operator_message_timestamp,
                        R.color.alt_threads_user_message_timestamp,
                        R.color.alt_threads_outgoing_message_time,
                        R.color.alt_threads_outgoing_time_underlay,
                        R.color.alt_threads_incoming_message_time,
                        R.color.alt_threads_incoming_time_underlay,
                        R.color.alt_threads_incoming_message_link,
                        R.color.alt_threads_outgoing_message_link,
                        R.color.alt_threads_chat_icons_tint,
                        R.color.alt_threads_chat_connection_message,
                        R.color.alt_threads_files_medias_screen_background,
                        R.color.alt_threads_files_list,
                        R.color.alt_threads_icon_and_separators_color,
                        R.drawable.alt_threads_operator_avatar_placeholder,
                        R.dimen.alt_threads_operator_photo_size,
                        R.dimen.alt_threads_system_operator_photo_size,
                        R.drawable.alt_threads_image_placeholder,
                        R.style.AltFileDialogStyleTransparent,
                        false,
                        false,
                        R.drawable.alt_threads_scroll_down_btn_back,
                        R.color.alt_threads_chat_unread_msg_sticker_background,
                        R.color.alt_threads_chat_unread_msg_count_text)
                .setChatInputStyle(R.color.alt_threads_input_hint,
                        R.color.alt_threads_input_background,
                        R.color.alt_threads_input_text,
                        null,
                        R.drawable.alt_threads_ic_attachment_button,
                        R.drawable.alt_threads_ic_send_button,
                        R.string.alt_threads_input_hint,
                        R.dimen.alt_threads_input_height,
                        R.drawable.alt_threads_chat_input_background)
                .setChatTitleStyle(R.string.alt_threads_contact_center,
                        R.string.alt_threads_operator_subtitle,
                        R.color.alt_threads_chat_toolbar,
                        R.color.alt_threads_chat_toolbar_text,
                        R.color.alt_threads_chat_status_bar,
                        R.color.alt_threads_chat_toolbar_menu_item,
                        R.color.alt_threads_chat_toolbar_hint,
                        false)
                .setPushNotificationStyle(R.drawable.alt_default_push_icon,
                        R.string.alt_threads_push_title,
                        R.color.alt_threads_push_background,
                        R.color.alt_threads_nougat_push_accent,
                        R.color.alt_threads_quick_reply_message_background,
                        R.color.alt_threads_quick_reply_message_text_color)
                .setImagesGalleryStyle(R.color.alt_threads_attachments_toolbar,
                        R.color.alt_threads_attachments_background,
                        R.color.alt_threads_attachments_author_text_color,
                        R.color.alt_threads_attachments_date_text_color,
                        R.dimen.alt_threads_attachments_author_text_size,
                        R.dimen.alt_threads_attachments_date_text_size)
                .setRequestResolveThreadStyle(R.string.alt_threads_request_to_resolve_thread,
                        R.string.alt_threads_request_to_resolve_thread_close,
                        R.string.alt_threads_request_to_resolve_thread_open)
                .setScheduleMessageStyle(R.drawable.alt_threads_schedule_icon,
                        R.color.alt_threads_schedule_text)
                .setSurveyStyle(R.drawable.alt_threads_binary_survey_like_unselected,
                        R.drawable.alt_threads_binary_survey_like_selected,
                        R.drawable.alt_threads_binary_survey_dislike_unselected,
                        R.drawable.alt_threads_binary_survey_dislike_selected,
                        R.drawable.alt_threads_options_survey_unselected,
                        R.drawable.alt_threads_options_survey_selected,
                        R.color.alt_threads_survey_selected_icon_tint,
                        R.color.alt_threads_survey_unselected_icon_tint,
                        R.color.alt_threads_chat_system_message,
                        R.color.alt_threads_survey_choices_text);

    }
}