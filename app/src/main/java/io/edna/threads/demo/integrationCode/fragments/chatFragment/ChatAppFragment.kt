package io.edna.threads.demo.integrationCode.fragments.chatFragment

import android.os.Bundle
import android.view.View
import im.threads.business.annotation.OpenWay
import im.threads.ui.core.ThreadsLib
import im.threads.ui.fragments.ChatFragment
import io.edna.threads.demo.R
import io.edna.threads.demo.appCode.fragments.BaseAppFragment
import io.edna.threads.demo.databinding.FragmentChatBinding

class ChatAppFragment : BaseAppFragment<FragmentChatBinding>(FragmentChatBinding::inflate) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeToGlobalBackClick()
        ChatFragment.newInstance(OpenWay.FROM_PUSH).let {
            fragment = it
            childFragmentManager
                .beginTransaction()
                .add(R.id.chatFragmentContainer, it)
                .commit()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ThreadsLib.getInstance().logoutClient()
    }
}
