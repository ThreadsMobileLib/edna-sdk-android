package io.edna.threads.demo.appCode.fragments.user

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.clearFragmentResultListener
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import im.threads.ui.utils.ColorsHelper
import io.edna.threads.demo.R
import io.edna.threads.demo.appCode.adapters.EccTouchHelperCallBack
import io.edna.threads.demo.appCode.adapters.ListItemClickListener
import io.edna.threads.demo.appCode.adapters.userList.UserListAdapter
import io.edna.threads.demo.appCode.business.UiThemeProvider
import io.edna.threads.demo.appCode.fragments.BaseAppFragment
import io.edna.threads.demo.databinding.FragmentUserListBinding
import io.edna.threads.demo.integrationCode.fragments.launch.LaunchFragment.Companion.SELECTED_USER_KEY
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.parceler.Parcels

class UserListFragment :
    BaseAppFragment<FragmentUserListBinding>(FragmentUserListBinding::inflate),
    ListItemClickListener {

    private val uiThemeProvider: UiThemeProvider by inject()
    private val viewModel: UserListViewModel by viewModel()
    private var adapter: UserListAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        setResultListeners()
        subscribeToGlobalBackClick()
        createAdapter()
        subscribeForData()
        initAdapter()
        setOnClickListeners()
        viewLifecycleOwner.lifecycle.addObserver(viewModel)
        viewModel.loadUserList()
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
        args.putParcelable(SELECTED_USER_KEY, Parcels.wrap(item))
        setFragmentResult(SELECTED_USER_KEY, args)
        viewModel.backToLaunchScreen(activity)
    }

    override fun onEditItem(position: Int) {
        val item = adapter?.getItem(position)
        val navigationController = activity?.findNavController(R.id.nav_host_fragment_content_main)
        val args = Bundle()
        args.putParcelable(USER_KEY, Parcels.wrap(item))
        navigationController?.navigate(R.id.action_UserListFragment_to_AddUserFragment, args)
    }

    override fun onRemoveItem(position: Int) {
        adapter?.getItem(position)?.let { viewModel.removeUser(it) }
    }

    private fun initView() {
        binding.addUser.background = null
        binding.addUser.setImageResource(R.drawable.ic_plus)
        if (uiThemeProvider.isDarkThemeOn()) {
            ColorsHelper.setTint(activity, binding.addUser, R.color.black_color)
            binding.addUser.setBackgroundResource(R.drawable.buttons_bg_selector_dark)
        } else {
            ColorsHelper.setTint(activity, binding.addUser, R.color.white_color_fa)
            binding.addUser.setBackgroundResource(R.drawable.buttons_bg_selector)
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
        addUser.setOnClickListener { viewModel.click(addUser) }
    }

    private fun clearResultListeners() {
        clearFragmentResultListener(USER_KEY)
    }

    private fun setResultListeners() {
        setFragmentResultListener(USER_KEY) { key, bundle ->
            viewModel.callFragmentResultListener(key, bundle)
        }
    }

    private fun createAdapter() = with(binding) {
        adapter = UserListAdapter(this@UserListFragment)
        recyclerView.adapter = adapter
    }

    private fun subscribeForData() {
        viewModel.userListLiveData.observe(viewLifecycleOwner) {
            adapter?.addItems(it)
            if (adapter?.itemCount == 0) {
                binding.emptyView.isVisible = true
                binding.recyclerView.isVisible = false
            } else {
                binding.emptyView.isVisible = false
                binding.recyclerView.isVisible = true
            }
        }
        viewModel.backButtonClickedLiveData.observe(viewLifecycleOwner) {
            if (it) {
                navigateUp()
            }
        }
    }

    companion object {
        const val USER_KEY = "user_key"
        const val SRC_USER_ID_KEY = "src_user_id_key"
    }
}
