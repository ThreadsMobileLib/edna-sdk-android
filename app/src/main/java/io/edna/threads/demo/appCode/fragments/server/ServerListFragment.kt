package io.edna.threads.demo.appCode.fragments.server

import android.os.Bundle
import android.view.View
import androidx.fragment.app.clearFragmentResultListener
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import im.threads.ui.utils.ColorsHelper
import io.edna.threads.demo.R
import io.edna.threads.demo.appCode.adapters.EccTouchHelperCallBack
import io.edna.threads.demo.appCode.adapters.ListItemClickListener
import io.edna.threads.demo.appCode.adapters.serverList.ServerListAdapter
import io.edna.threads.demo.appCode.business.UiThemeProvider
import io.edna.threads.demo.appCode.fragments.BaseAppFragment
import io.edna.threads.demo.databinding.FragmentServerListBinding
import io.edna.threads.demo.integrationCode.fragments.launch.LaunchFragment.Companion.SELECTED_SERVER_CONFIG_KEY
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.parceler.Parcels

class ServerListFragment :
    BaseAppFragment<FragmentServerListBinding>(FragmentServerListBinding::inflate),
    ListItemClickListener {

    private val uiThemeProvider: UiThemeProvider by inject()
    private val viewModel: ServerListViewModel by viewModel()
    private var adapter: ServerListAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        setResultListeners()
        createAdapter()
        subscribeForData()
        initAdapter()
        setOnClickListeners()
        viewLifecycleOwner.lifecycle.addObserver(viewModel)
        viewModel.copyServersFromFileIfNeed()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        clearResultListeners()
    }

    override fun navigateUp() {
        viewModel.backToLaunchScreen(activity)
    }

    override fun onClick(position: Int) {
        val item = adapter?.getItem(position)
        val args = Bundle()
        args.putParcelable(SELECTED_SERVER_CONFIG_KEY, Parcels.wrap(item))
        setFragmentResult(SELECTED_SERVER_CONFIG_KEY, args)
        viewModel.backToLaunchScreen(activity)
    }

    override fun onEditItem(position: Int) {
        val item = adapter?.getItem(position)
        val navigationController = activity?.findNavController(R.id.nav_host_fragment_content_main)
        val args = Bundle()
        args.putParcelable(SERVER_CONFIG_KEY, Parcels.wrap(item))
        navigationController?.navigate(R.id.action_ServerListFragment_to_AddServerFragment, args)
    }

    override fun onRemoveItem(position: Int) {
        adapter?.getItem(position)?.let {
            viewModel.removeConfig(it)
        }
    }

    private fun initAdapter() {
        val simpleCallback = EccTouchHelperCallBack(
            requireContext(),
            this,
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        )
        val touchHelper = ItemTouchHelper(simpleCallback)
        touchHelper.attachToRecyclerView(binding.recyclerView)
    }

    private fun setOnClickListeners() = with(binding) {
        backButton.setOnClickListener { viewModel.click(backButton) }
        addServer.setOnClickListener { viewModel.click(addServer) }
    }

    private fun clearResultListeners() {
        clearFragmentResultListener(SERVER_CONFIG_KEY)
    }

    private fun setResultListeners() {
        setFragmentResultListener(SERVER_CONFIG_KEY) { key, bundle ->
            viewModel.callFragmentResultListener(key, bundle)
        }
    }

    private fun initView() {
        binding.addServer.background = null
        binding.addServer.setImageResource(R.drawable.ic_plus)
        if (uiThemeProvider.isDarkThemeOn()) {
            ColorsHelper.setTint(activity, binding.addServer, R.color.black_color)
            binding.addServer.setBackgroundResource(R.drawable.buttons_bg_selector_dark)
        } else {
            ColorsHelper.setTint(activity, binding.addServer, R.color.white_color_fa)
            binding.addServer.setBackgroundResource(R.drawable.buttons_bg_selector)
        }
    }

    private fun createAdapter() = with(binding) {
        adapter = ServerListAdapter(this@ServerListFragment)
        recyclerView.adapter = adapter
    }

    private fun subscribeForData() {
        viewModel.serverConfigLiveData.observe(viewLifecycleOwner) { adapter?.addItems(it) }
    }

    companion object {
        const val SERVER_CONFIG_KEY = "server_config_key"
        const val SRC_SERVER_NAME_KEY = "src_server_name_key"
    }
}
