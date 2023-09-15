package io.edna.threads.demo.appCode.push

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import im.threads.ui.ChatCenterPushMessageHelper

class CustomPushFcmIntentService : FirebaseMessagingService() {
    private val chatCenterPushMessageHelper = ChatCenterPushMessageHelper()

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        chatCenterPushMessageHelper.setFcmToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        chatCenterPushMessageHelper.process(message.data)
    }
}
