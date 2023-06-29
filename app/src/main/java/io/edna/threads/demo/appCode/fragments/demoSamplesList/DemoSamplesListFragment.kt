package io.edna.threads.demo.appCode.fragments.demoSamplesList

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import im.threads.ui.core.ThreadsLib
import io.edna.threads.demo.R
import io.edna.threads.demo.appCode.adapters.demoSamplesList.DemoSamplesAdapter
import io.edna.threads.demo.appCode.adapters.demoSamplesList.SampleListItemOnClick
import io.edna.threads.demo.appCode.fragments.BaseAppFragment
import io.edna.threads.demo.appCode.models.DemoSamplesListItem
import io.edna.threads.demo.databinding.FragmentSamplesListBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class DemoSamplesListFragment : BaseAppFragment<FragmentSamplesListBinding>(), SampleListItemOnClick {
    private val viewModel: DemoSamplesListViewModel by viewModel()
    private var adapter: DemoSamplesAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSamplesListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        createAdapter()
        setNavigationIcon()
        subscribeForData()
        subscribeToGlobalBackClick()
        viewLifecycleOwner.lifecycle.addObserver(viewModel)
    }

    override fun onClick(item: DemoSamplesListItem) {
        viewModel.onItemClick(item)
    }

    private fun createAdapter() = with(binding) {
        adapter = DemoSamplesAdapter(this@DemoSamplesListFragment)
        recyclerView.adapter = adapter
    }

    private fun setNavigationIcon() = with(binding) {
        toolbar.navigationIcon?.setTint(ContextCompat.getColor(requireContext(), R.color.white_color_ec))
        toolbar.setNavigationOnClickListener {
            ThreadsLib.getInstance().logoutClient()
            findNavController().navigateUp()
        }
    }

    private fun subscribeForData() {
        viewModel.demoSamplesLiveData.observe(viewLifecycleOwner) { adapter?.addItems(it) }
        viewModel.navigationLiveData.observe(viewLifecycleOwner) { findNavController().navigate(it) }
    }
}
