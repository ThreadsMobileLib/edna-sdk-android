package io.edna.threads.demo.appCode.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ServerConfig(
    var name: String? = null,
    var threadsGateProviderUid: String? = null,
    var datastoreUrl: String? = null,
    var serverBaseUrl: String? = null,
    var threadsGateUrl: String? = null,
    var isFromApp: Boolean = false,
    var isShowMenu: Boolean = false,
    var filesAndMediaMenuItemEnabled: Boolean = true,
    var trustedSSLCertificates: List<Int>? = null,
    var allowUntrustedSSLCertificate: Boolean = false
) : Parcelable {

    override fun toString() = "Server config:\n = $name, " +
        "\nthreadsGateProviderUid = $threadsGateProviderUid, " +
        "\ndatastoreUrl = $datastoreUrl, " +
        "\nserverBaseUrl = $serverBaseUrl, " +
        "\nthreadsGateUrl = $threadsGateUrl, " +
        "\nisFromApp = $isFromApp, " +
        "\nisShowMenu = $isShowMenu, " +
        "\nfilesAndMediaMenuItemEnabled = $filesAndMediaMenuItemEnabled, " +
        "\ntrustedSSLCertificates = $trustedSSLCertificates, " +
        "\nallowUntrustedSSLCertificate = $allowUntrustedSSLCertificate"

    fun isAllFieldsFilled(): Boolean {
        return !name.isNullOrEmpty() &&
            !threadsGateProviderUid.isNullOrEmpty() &&
            !datastoreUrl.isNullOrEmpty() &&
            !serverBaseUrl.isNullOrEmpty() &&
            !threadsGateUrl.isNullOrEmpty()
    }

    fun copy(): ServerConfig {
        return ServerConfig(
            name,
            threadsGateProviderUid,
            datastoreUrl,
            serverBaseUrl,
            threadsGateUrl,
            isFromApp,
            isShowMenu,
            filesAndMediaMenuItemEnabled,
            trustedSSLCertificates,
            allowUntrustedSSLCertificate
        )
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other is ServerConfig) {
            return other.name == name &&
                other.threadsGateProviderUid == threadsGateProviderUid &&
                other.datastoreUrl == datastoreUrl &&
                other.serverBaseUrl == serverBaseUrl &&
                other.threadsGateUrl == threadsGateUrl
        }
        return false
    }
}
