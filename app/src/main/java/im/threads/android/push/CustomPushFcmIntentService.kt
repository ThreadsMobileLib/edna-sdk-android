package im.threads.android.push

import com.edna.android.push_lite.fcm.FcmPushService
import com.google.firebase.messaging.RemoteMessage
import im.threads.business.serviceLocator.core.inject
import im.threads.ui.ChatCenterPushMessageHelper

class CustomPushFcmIntentService : FcmPushService() {
    private val chatCenterPushMessageHelper: ChatCenterPushMessageHelper by inject()

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        chatCenterPushMessageHelper.setFcmToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        chatCenterPushMessageHelper.process(this, message.data)
    }
}
