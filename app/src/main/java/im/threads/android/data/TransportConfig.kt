package im.threads.android.data

import im.threads.ConfigBuilder

data class TransportConfig(
    val baseUrl: String,
    @Deprecated("only THREADS_GATE transport is supported")
    val transportType: ConfigBuilder.TransportType = ConfigBuilder.TransportType.THREADS_GATE,
    val threadsGateUrl: String,
    val threadsGateProviderUid: String,
    val threadsGateHCMProviderUid: String? = null
)
