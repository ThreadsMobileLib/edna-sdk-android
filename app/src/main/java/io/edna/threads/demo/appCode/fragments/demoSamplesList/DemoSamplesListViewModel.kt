package io.edna.threads.demo.appCode.fragments.demoSamplesList

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import im.threads.business.UserInfoBuilder
import im.threads.ui.core.ThreadsLib
import io.edna.threads.demo.R
import io.edna.threads.demo.appCode.business.StringsProvider
import io.edna.threads.demo.appCode.business.VolatileLiveData
import io.edna.threads.demo.appCode.business.mockJsonProvider.CurrentJsonProvider
import io.edna.threads.demo.appCode.business.mockJsonProvider.SamplesJsonProvider
import io.edna.threads.demo.appCode.models.DemoSamplesListItem
import io.edna.threads.demo.appCode.models.DemoSamplesListItem.TEXT

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
        createData()
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
}
