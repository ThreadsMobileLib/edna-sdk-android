package im.threads.android.data

data class ServerConfig(
    val name: String,
    val datastoreUrl: String,
    val serverBaseUrl: String,
    val threadsGateUrl: String,
    val threadsGateProviderUid: String,
    val filesAndMediaMenuItemEnabled: Boolean,
    val newChatCenterApi: Boolean = false,
    val isFromApp: Boolean = false,
    val isSSLPinningDisabled: Boolean = false
)
