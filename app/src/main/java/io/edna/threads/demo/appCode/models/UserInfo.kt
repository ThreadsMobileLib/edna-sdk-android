package io.edna.threads.demo.appCode.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserInfo(
    var nickName: String? = null,
    var userId: String? = null,
    var userData: String? = null,
    var appMarker: String? = null,
    var signature: String? = null,
    var authorizationHeader: String? = null,
    var xAuthSchemaHeader: String? = null,
    var isShowMenu: Boolean = false
) : Parcelable {

    override fun toString() = "$nickName," +
        "$userId," +
        "$userData," +
        "$appMarker," +
        "$signature," +
        "$authorizationHeader," +
        "$xAuthSchemaHeader," +
        "$isShowMenu"

    fun isAllFieldsFilled(): Boolean {
        return !nickName.isNullOrEmpty() && !userId.isNullOrEmpty()
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other is UserInfo) {
            return other.nickName == nickName &&
                other.userId == userId &&
                other.appMarker == appMarker &&
                other.signature == signature &&
                other.authorizationHeader == authorizationHeader &&
                other.xAuthSchemaHeader == xAuthSchemaHeader &&
                other.userData == userData
        }
        return false
    }

    fun clone(): UserInfo {
        return UserInfo(
            nickName,
            userId,
            userData,
            appMarker,
            signature,
            authorizationHeader,
            xAuthSchemaHeader,
            isShowMenu
        )
    }
}
