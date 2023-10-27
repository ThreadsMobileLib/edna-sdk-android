package io.edna.threads.demo.appCode.fragments.server

import android.os.Bundle
import android.view.View
import androidx.fragment.app.setFragmentResult
import io.edna.threads.demo.appCode.fragments.BaseAppFragment
import io.edna.threads.demo.appCode.fragments.server.ServerListFragment.Companion.SERVER_CONFIG_KEY
import io.edna.threads.demo.appCode.fragments.server.ServerListFragment.Companion.SRC_SERVER_NAME_KEY
import io.edna.threads.demo.databinding.FragmentAddServerBinding
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.parceler.Parcels

class AddServerFragment : BaseAppFragment<FragmentAddServerBinding>(FragmentAddServerBinding::inflate) {

    private val viewModel: AddServerViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeToGlobalBackClick()
        subscribeForTextWatchers()
        subscribeForData()
        setOnClickListeners()
        viewModel.initData(arguments)
    }

    private fun subscribeForTextWatchers() = getBinding()?.apply {
        name.setTextChangedListener(viewModel.nameTextWatcher)
        providerId.setTextChangedListener(viewModel.providerIdTextWatcher)
        baseUrl.setTextChangedListener(viewModel.baseUrlTextWatcher)
        datastoreUrl.setTextChangedListener(viewModel.datastoreUrlTextWatcher)
        threadsGateUrl.setTextChangedListener(viewModel.threadsGateUrlTextWatcher)
    }

    private fun subscribeForData() = getBinding()?.apply {
        viewModel.finalServerConfigLiveData.observe(viewLifecycleOwner) {
            val args = Bundle()
            args.putParcelable(SERVER_CONFIG_KEY, Parcels.wrap(it))
            viewModel.srcServerConfig?.name?.let { name ->
                args.putString(SRC_SERVER_NAME_KEY, name)
            }
            setFragmentResult(SERVER_CONFIG_KEY, args)
        }
        viewModel.serverConfigLiveData.observe(viewLifecycleOwner) {
            name.text = it.name
            providerId.text = it.threadsGateProviderUid
            baseUrl.text = it.serverBaseUrl
            datastoreUrl.text = it.datastoreUrl
            threadsGateUrl.text = it.threadsGateUrl
        }
        viewModel.errorStringForServerNameFieldLiveData.observe(viewLifecycleOwner) {
            name.error = it
        }
        viewModel.errorStringForProviderIdFieldLiveData.observe(viewLifecycleOwner) {
            providerId.error = it
        }
        viewModel.errorStringForBaseUrlFieldLiveData.observe(viewLifecycleOwner) {
            baseUrl.error = it
        }
        viewModel.errorStringForDatastoreUrlFieldLiveData.observe(viewLifecycleOwner) {
            datastoreUrl.error = it
        }
        viewModel.errorStringForThreadsGateUrlFieldLiveData.observe(viewLifecycleOwner) {
            datastoreUrl.error = it
        }
    }

    private fun setOnClickListeners() = getBinding()?.apply {
        backButton.setOnClickListener { viewModel.click(backButton) }
        okButton.setOnClickListener { viewModel.click(okButton) }
    }
}
