package io.edna.threads.demo.appCode.push

import android.content.Context
import com.huawei.agconnect.config.AGConnectServicesConfig
import com.huawei.hms.aaid.HmsInstanceId
import com.huawei.hms.common.ApiException
import im.threads.ui.ChatCenterPushMessageHelper
import java.io.IOException

object HCMTokenRefresher {

    fun requestToken(context: Context) {
        val hcmAppId = AGConnectServicesConfig.fromContext(context).getString("client/app_id")
        if (hcmAppId != null) {
            try {
                val hmsInstanceId = HmsInstanceId.getInstance(context)
                val token = hmsInstanceId.getToken(hcmAppId, "HCM")
                ChatCenterPushMessageHelper().setHcmToken(token)
            } catch (e: IOException) {
            } catch (e: ApiException) {
            }
        }
    }
}
