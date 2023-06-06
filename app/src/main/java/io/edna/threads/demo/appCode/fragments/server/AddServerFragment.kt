package io.edna.threads.demo.appCode.fragments.server

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.setFragmentResult
import io.edna.threads.demo.R
import io.edna.threads.demo.appCode.extensions.inflateWithBinding
import io.edna.threads.demo.appCode.fragments.BaseAppFragment
import io.edna.threads.demo.appCode.fragments.server.ServerListFragment.Companion.SERVER_CONFIG_KEY
import io.edna.threads.demo.databinding.FragmentAddServerBinding
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.parceler.Parcels

class AddServerFragment : BaseAppFragment<FragmentAddServerBinding>() {

    private val viewModel: AddServerViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = inflater.inflateWithBinding(container, R.layout.fragment_add_server)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        subscribeToGlobalBackClick()
        subscribeForData()
        viewModel.initData(arguments)
        return binding.root
    }

    private fun subscribeForData() {
        viewModel.finalServerConfigLiveData.observe(viewLifecycleOwner) {
            val args = Bundle()
            args.putParcelable(SERVER_CONFIG_KEY, Parcels.wrap(it))
            setFragmentResult(SERVER_CONFIG_KEY, args)
        }
    }
}
