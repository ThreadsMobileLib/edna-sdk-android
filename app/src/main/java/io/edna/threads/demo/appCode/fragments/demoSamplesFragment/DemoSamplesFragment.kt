package io.edna.threads.demo.appCode.fragments.demoSamplesFragment

import android.os.Bundle
import android.view.View
import io.edna.threads.demo.R
import io.edna.threads.demo.appCode.fragments.BaseAppFragment
import io.edna.threads.demo.databinding.FragmentChatBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class DemoSamplesFragment : BaseAppFragment<FragmentChatBinding>(FragmentChatBinding::inflate) {
    private val viewModel: DemoSamplesViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeToData()
        subscribeToGlobalBackClick()
        viewLifecycleOwner.lifecycle.addObserver(viewModel)
    }

    private fun subscribeToData() {
        viewModel.chatFragmentLiveData.observe(viewLifecycleOwner) {
            fragment = it
            childFragmentManager
                .beginTransaction()
                .add(R.id.chatFragmentContainer, it)
                .commit()
        }
    }
}
