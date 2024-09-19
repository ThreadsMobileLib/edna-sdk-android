package io.edna.threads.demo.appCode.themes

import im.threads.business.markdown.MarkdownConfig
import im.threads.ui.ChatStyle
import io.edna.threads.demo.R

class ChatThemes {

    fun getLightChatTheme() = getMainChatTheme().apply {
        setConsultSearchingProgressColor(R.color.light_toolbar)
        setChatBodyIconsTint(R.color.light_toolbar)
        enableLinkPreview()
        setChatTitleStyle(
            R.string.app_name,
            R.string.demo_alt_threads_operator_subtitle,
            R.color.light_toolbar,
            R.color.alt_threads_chat_context_menu,
            R.color.alt_threads_chat_toolbar_text,
            R.color.light_statusbar,
            R.bool.alt_threads_chat_is_light_status_bar,
            R.color.light_toolbar,
            R.color.alt_threads_chat_toolbar_hint,
            true
        )
        setOutgoingMessageBubbleColor(R.color.light_outgoing_bubble)
        setScrollDownButtonIcon(R.drawable.alt_threads_scroll_down_icon_light)
        setRecordButtonBackgroundColor(R.color.light_toolbar)
        setOutgoingMessageTextColor(R.color.black_color)
        setOutgoingImageTimeBackgroundColor(R.color.light_outgoing_image_time_background)
        setOutgoingMessageTimeColor(R.color.light_outgoing_time_text)
        setMessageSendingResources(null, R.color.light_icons)
        setMessageSentResources(null, R.color.light_icons)
        setMessageDeliveredResources(null, R.color.light_icons)
        setMessageReadResources(null, R.color.light_icons)
        setMessageFailedResources(null, R.color.light_icons)
        setChatHighlightingColor(R.color.light_highlighting)
        setIncomingMessageLinkColor(R.color.light_links)
        setOutgoingMessageLinkColor(R.color.light_links)
    }

    fun getDarkChatTheme() = getMainChatTheme().apply {
        setConsultSearchingProgressColor(R.color.dark_toolbar)
        setChatBodyIconsTint(R.color.dark_toolbar)
        enableLinkPreview()
        setChatTitleStyle(
            R.string.demo_alt_threads_contact_center,
            R.string.demo_alt_threads_operator_subtitle,
            R.color.dark_toolbar,
            R.color.alt_threads_chat_context_menu,
            R.color.alt_threads_chat_toolbar_text,
            R.color.alt_threads_chat_status_bar,
            R.bool.alt_threads_chat_is_light_status_bar,
            R.color.alt_threads_chat_toolbar_menu_item_black,
            R.color.alt_threads_chat_toolbar_hint,
            true
        )
        setOutgoingMessageBubbleColor(R.color.dark_outgoing_bubble)
        setScrollDownButtonIcon(R.drawable.alt_threads_scroll_down_icon_black)
        setRecordButtonBackgroundColor(R.color.dark_toolbar)
        setOutgoingMessageTextColor(R.color.white_color_fa)
        setOutgoingImageTimeBackgroundColor(R.color.dark_outgoing_image_time_background)
        setOutgoingMessageTimeColor(R.color.dark_outgoing_time_text)
        setMessageSendingResources(null, R.color.dark_icons)
        setMessageSentResources(null, R.color.dark_icons)
        setMessageDeliveredResources(null, R.color.dark_icons)
        setMessageReadResources(null, R.color.dark_icons)
        setMessageFailedResources(null, R.color.dark_icons)
        setChatHighlightingColor(R.color.dark_highlighting)
        setIncomingMessageLinkColor(R.color.dark_links)
        setOutgoingMessageLinkColor(R.color.dark_links)
        setDelimitersColors(
            im.threads.R.color.ecc_error_red_df0000,
            R.color.dark_links
        )
        setChatBackgroundColor(R.color.dark_chat_background)
        setSystemMessageStyle(
            null,
            null,
            R.color.dark_system_text,
            null,
            null,
            R.color.dark_links
        )
        setSurveyStyle(
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            R.color.dark_system_text,
            R.color.dark_system_text
        )
        setDownloadButtonTintResId(im.threads.R.color.ecc_white)
        setDownloadButtonBackgroundTintResId(R.color.dark_links)
        setChatErrorScreenStyle(
            null,
            null,
            R.color.white_color,
            null,
            null,
            null,
            null
        )
        setOutgoingQuoteViewStyle(
            R.color.dark_links,
            R.color.black_color,
            R.color.alt_threads_chat_toolbar_hint,
            R.color.white_color_fa
        )
        setIncomingQuoteViewStyle(
            R.color.dark_links,
            R.color.black_color,
            R.color.alt_threads_chat_toolbar_hint,
            R.color.white_color_fa
        )
    }

    private fun getMainChatTheme(): ChatStyle {
        val chatStyle = ChatStyle()
            .setDefaultFontBold(LATO_BOLD_FONT_PATH)
            .setDefaultFontLight(LATO_LIGHT_FONT_PATH)
            .setDefaultFontRegular(LATO_REGULAR_FONT_PATH)
            .setScrollChatToEndIfUserTyping(false)

        val markdownConfig = MarkdownConfig()
        markdownConfig.isLinkUnderlined = true
        chatStyle
            .setChatSubtitleShowConsultOrgUnit(true)
            .setIncomingMarkdownConfiguration(markdownConfig)
            .setOutgoingMarkdownConfiguration(markdownConfig)
            .setVisibleChatTitleShadow(R.bool.alt_threads_chat_title_shadow_is_visible)
            .setShowConsultSearching(true)
            .setVoiceMessageEnabled(true)
            .showChatBackButton(true)
            .setIngoingPadding(
                R.dimen.alt_greenBubbleIncomingPaddingLeft,
                R.dimen.alt_greenBubbleIncomingPaddingTop,
                R.dimen.alt_greenBubbleIncomingPaddingRight,
                R.dimen.alt_greenBubbleIncomingPaddingBottom
            )
            .setIncomingImageBordersSize(
                R.dimen.alt_incomingImageLeftBorderSize,
                R.dimen.alt_incomingImageTopBorderSize,
                R.dimen.alt_incomingImageRightBorderSize,
                R.dimen.alt_incomingImageBottomBorderSize
            )
            .setOutgoingImageBordersSize(
                R.dimen.alt_outgoingImageLeftBorderSize,
                R.dimen.alt_outgoingImageTopBorderSize,
                R.dimen.alt_outgoingImageRightBorderSize,
                R.dimen.alt_outgoingImageBottomBorderSize
            )
            .setIncomingImageMask(R.drawable.alt_thread_incoming_image_mask)
            .setOutgoingImageMask(R.drawable.alt_thread_outgoing_image_mask)
            .setOutgoingBubbleMask(R.drawable.alt_thread_outgoing_bubble)
            .setIncomingBubbleMask(R.drawable.alt_thread_incoming_bubble)

        return chatStyle
    }
}

private const val LATO_BOLD_FONT_PATH = "fonts/lato-bold.ttf"
private const val LATO_LIGHT_FONT_PATH = "fonts/lato-light.ttf"
private const val LATO_REGULAR_FONT_PATH = "fonts/lato-regular.ttf"
