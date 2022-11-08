package im.threads.android.data

data class TransportConfig(
    val baseUrl: String,
    val datastoreUrl: String,
    val threadsGateUrl: String,
    val threadsGateProviderUid: String,
    val isNewChatCenterApi: Boolean
)
