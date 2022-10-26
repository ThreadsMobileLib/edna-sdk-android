package im.threads.android.push

import com.edna.android.push_lite.huawei.HcmPushService
import com.edna.android.push_lite.utils.CommonUtils
import com.huawei.hms.push.RemoteMessage
import im.threads.business.serviceLocator.core.inject
import im.threads.ui.ChatCenterPushMessageHelper

class CustomPushHcmIntentService : HcmPushService() {
    private val chatCenterPushMessageHelper: ChatCenterPushMessageHelper by inject()

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        chatCenterPushMessageHelper.setHcmToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        chatCenterPushMessageHelper.process(
            this,
            CommonUtils.base64JsonStringToBundle(message.data)
        )
    }
}
