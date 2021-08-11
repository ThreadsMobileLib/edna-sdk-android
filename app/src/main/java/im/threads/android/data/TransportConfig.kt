package im.threads.android.data

import im.threads.ConfigBuilder

data class TransportConfig(val baseUrl: String, val transportType: ConfigBuilder.TransportType, val threadsGateUrl: String, val threadsGateProviderUid: String)
