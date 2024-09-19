package io.edna.threads.demo.appCode.fragments.demoSamplesList

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import im.threads.business.UserInfoBuilder
import im.threads.business.core.ContextHolder
import im.threads.ui.config.ConfigBuilder
import im.threads.ui.core.ThreadsLib
import io.edna.threads.demo.R
import io.edna.threads.demo.appCode.business.StringsProvider
import io.edna.threads.demo.appCode.business.VolatileLiveData
import io.edna.threads.demo.appCode.business.mockJsonProvider.CurrentJsonProvider
import io.edna.threads.demo.appCode.business.mockJsonProvider.SamplesJsonProvider
import io.edna.threads.demo.appCode.models.DemoSamplesListItem
import io.edna.threads.demo.appCode.models.DemoSamplesListItem.TEXT
import io.edna.threads.demo.appCode.models.ServerConfig
import io.edna.threads.demo.appCode.themes.ChatThemes
import io.edna.threads.demo.integrationCode.ednaMockThreadsGateProviderUid
import io.edna.threads.demo.integrationCode.ednaMockThreadsGateUrl
import io.edna.threads.demo.integrationCode.ednaMockUrl

class DemoSamplesListViewModel(
    private val stringsProvider: StringsProvider,
    private val samplesJsonProvider: SamplesJsonProvider,
    private val currentJsonProvider: CurrentJsonProvider
) : ViewModel(), DefaultLifecycleObserver {
    private val mutableDemoSamplesLiveData = MutableLiveData<List<DemoSamplesListItem>>()
    val demoSamplesLiveData: LiveData<List<DemoSamplesListItem>> = mutableDemoSamplesLiveData
    val navigationLiveData = VolatileLiveData<Int>()

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        checkSdkInit()
        createData()
    }

    private fun checkSdkInit() {
        if (!ThreadsLib.isInitialized()) {
            val demoServerConfig = getDefaultServerConfig()
            val config = ConfigBuilder(ContextHolder.context).apply {
                threadsGateUrl(demoServerConfig.threadsGateUrl)
                datastoreUrl(demoServerConfig.datastoreUrl)
                serverBaseUrl(demoServerConfig.serverBaseUrl)
                threadsGateProviderUid(demoServerConfig.threadsGateProviderUid)
            }
            ThreadsLib.init(config)
            ThreadsLib.getInstance().apply {
                // Кастомизация внешнего вида. Поддержка темной темы
                val themes = ChatThemes()
                applyLightTheme(themes.getLightChatTheme())
                applyDarkTheme(themes.getDarkChatTheme())
            }
        }
    }

    fun onItemClick(item: DemoSamplesListItem) {
        if (item is TEXT) {
            currentJsonProvider.saveCurrentJson(item.json)
            ThreadsLib.getInstance().initUser(UserInfoBuilder("333"))
            navigationLiveData.setValue(R.id.action_DemoSamplesListFragment_to_DemoSamplesFragment)
        }
    }

    private fun createData() {
        mutableDemoSamplesLiveData.postValue(
            listOf(
                TEXT(stringsProvider.textMessages, samplesJsonProvider.getTextChatJson()),
                TEXT(stringsProvider.connectionErrors, samplesJsonProvider.getConnectionErrorJson()),
                TEXT(stringsProvider.voiceMessages, samplesJsonProvider.getVoicesChatJson()),
                TEXT(stringsProvider.images, samplesJsonProvider.getImagesChatJson()),
                TEXT(stringsProvider.files, samplesJsonProvider.getFilesChatJson()),
                TEXT(stringsProvider.systemMessages, samplesJsonProvider.getSystemChatJson()),
                TEXT(stringsProvider.chatWithBot, samplesJsonProvider.getChatBotJson()),
                TEXT(stringsProvider.chatWithEditAndDeletedMessages, samplesJsonProvider.getChatWithEditAndDeletedMessages())
            )
        )
    }

    private fun getDefaultServerConfig() = ServerConfig(
        name = "TestServer",
        threadsGateProviderUid = ednaMockThreadsGateProviderUid,
        datastoreUrl = ednaMockUrl,
        serverBaseUrl = ednaMockUrl,
        threadsGateUrl = ednaMockThreadsGateUrl,
        isShowMenu = true
    )
}
