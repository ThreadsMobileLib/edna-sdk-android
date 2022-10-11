package im.threads.android.push

import com.edna.android.push_lite.fcm.FcmPushService
import com.google.firebase.messaging.RemoteMessage
import im.threads.ui.ChatCenterPushMessageHelper

class CustomPushFcmIntentService : FcmPushService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        ChatCenterPushMessageHelper.setFcmToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        ChatCenterPushMessageHelper.process(this, message.data)
    }
}
