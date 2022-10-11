package im.threads.android.push

import com.edna.android.push_lite.huawei.HcmPushService
import com.edna.android.push_lite.utils.CommonUtils
import com.huawei.hms.push.RemoteMessage
import im.threads.ui.ChatCenterPushMessageHelper

class CustomPushHcmIntentService : HcmPushService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        ChatCenterPushMessageHelper.setHcmToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        ChatCenterPushMessageHelper.process(
            this,
            CommonUtils.base64JsonStringToBundle(message.data)
        )
    }
}
