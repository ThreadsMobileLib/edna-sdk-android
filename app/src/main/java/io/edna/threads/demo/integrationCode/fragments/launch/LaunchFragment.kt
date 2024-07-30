package io.edna.threads.demo.integrationCode.fragments.launch

import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.clearFragmentResultListener
import androidx.fragment.app.setFragmentResultListener
import im.threads.business.models.enums.ApiVersionEnum
import im.threads.business.models.enums.CurrentUiTheme
import im.threads.ui.core.ThreadsLib
import io.edna.threads.demo.BuildConfig
import io.edna.threads.demo.R
import io.edna.threads.demo.appCode.business.StringsProvider
import io.edna.threads.demo.appCode.fragments.BaseAppFragment
import io.edna.threads.demo.appCode.models.UiTheme
import io.edna.threads.demo.databinding.FragmentLaunchBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class LaunchFragment : BaseAppFragment<FragmentLaunchBinding>(FragmentLaunchBinding::inflate) {
    private val viewModel: LaunchViewModel by viewModel()
    private val stringsProvider: StringsProvider by inject()

    private var initLibReceiver: InitThreadsLibReceiver? = null
    private var unreadCountReceiver: InitUnreadCountReceiver? = null
    private val initLibFilter = IntentFilter(APP_INIT_THREADS_LIB_ACTION)
    private val unreadCountFilter = IntentFilter(APP_UNREAD_COUNT_BROADCAST)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeForData()
        initReceivers()
        initObservers()
        initPreregisterCheckBox()
        setResultListeners()
        initView()
        setOnClickListeners()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        clearResultListeners()
        unregisterReceivers()
    }

    private fun unregisterReceivers() {
        unregisterInitLibReceivers()
        unreadCountReceiver?.let {
            requireActivity().unregisterReceiver(it)
            unreadCountReceiver = null
        }
    }

    private fun unregisterInitLibReceivers() {
        initLibReceiver?.let {
            requireActivity().unregisterReceiver(it)
            initLibReceiver = null
        }
    }

    private fun initView() = getBinding()?.apply {
        login.isEnabled = false
        about.text = generateAboutText()
    }

    private fun setOnClickListeners() = getBinding()?.apply {
        uiTheme.setOnClickListener { viewModel.click(uiTheme) }
        serverButton.setOnClickListener { viewModel.click(serverButton) }
        userButton.setOnClickListener { viewModel.click(userButton) }
        demonstrations.setOnClickListener { viewModel.click(demonstrations) }
        apiSelector.setOnClickListener { showSelectApiVersionMenu() }
        uiTheme.setOnClickListener { viewModel.click(uiTheme) }
        login.setOnClickListener {
            viewModel.click(login)
            setUnreadCount(0)
        }
    }

    private fun subscribeForData() = getBinding()?.apply {
        viewModel.selectedApiVersionLiveData.observe(viewLifecycleOwner) {
            val apiVersionText = "${getString(R.string.api_version)}: $it"
            apiSelector.text = apiVersionText
        }
        viewModel.selectedServerConfigLiveData.observe(viewLifecycleOwner) {
            serverButton.text = it?.name
        }
        viewModel.selectedUserLiveData.observe(viewLifecycleOwner) {
            userButton.text = it?.userId
            if (LaunchViewModel.isPreregisterEnabled && it != null && !it.userId.isNullOrBlank()) {
                viewModel.callInitUser(it)
            }
        }
        viewModel.enabledLoginButtonLiveData.observe(viewLifecycleOwner) {
            login.isEnabled = it == true
        }
    }

    private fun initObservers() {
        viewModel.currentUiThemeLiveData.observe(viewLifecycleOwner) { setUiThemeDependentViews(it) }
        viewModel.themeSelectorLiveData.observe(viewLifecycleOwner) { showUiThemesSelector(it) }
        viewModel.preregisterLiveData.observe(viewLifecycleOwner) { setValueToPreregisterCheckBox(it) }
        viewModel.restartAppLiveData.observe(viewLifecycleOwner) { restartApp(it) }
        viewLifecycleOwner.lifecycle.addObserver(viewModel)
        viewModel.subscribeForData(viewLifecycleOwner)
    }

    private fun initPreregisterCheckBox() = getBinding()?.apply {
        preRegisterCheckBox.setOnClickListener {
            val isChecked = preRegisterCheckBox.isChecked
            viewModel.onPreregisterCheckedChange(isChecked)

            if (isChecked) {
                Toast
                    .makeText(context, getString(R.string.pre_register_selected_hint), Toast.LENGTH_LONG)
                    .show()
            }
        }
    }

    private fun setValueToPreregisterCheckBox(isChecked: Boolean) = getBinding()?.apply {
        preRegisterCheckBox.isChecked = isChecked
    }

    private fun setResultListeners() {
        setFragmentResultListener(SELECTED_USER_KEY) { key, bundle ->
            viewModel.callFragmentResultListener(key, bundle)
        }
        setFragmentResultListener(SELECTED_SERVER_CONFIG_KEY) { key, bundle ->
            viewModel.callFragmentResultListener(key, bundle)
        }
    }

    private fun clearResultListeners() {
        clearFragmentResultListener(SELECTED_USER_KEY)
        clearFragmentResultListener(SELECTED_SERVER_CONFIG_KEY)
    }

    private fun setUiThemeDependentViews(theme: UiTheme) = getBinding()?.apply {
        context?.let { context ->
            when (theme) {
                UiTheme.LIGHT -> {
                    title.setTextColor(ContextCompat.getColor(context, R.color.black_color))
                    about.setTextColor(ContextCompat.getColor(context, R.color.info_text_color))
                    uiTheme.setImageDrawable(
                        AppCompatResources.getDrawable(
                            context,
                            R.drawable.dark_theme
                        )
                    )
                }
                UiTheme.DARK -> {
                    title.setTextColor(ContextCompat.getColor(context, R.color.white_color_fa))
                    about.setTextColor(ContextCompat.getColor(context, R.color.gray_color_b7))
                    uiTheme.setImageDrawable(
                        AppCompatResources.getDrawable(
                            context,
                            R.drawable.light_theme
                        )
                    )
                }
            }
        }
    }

    private fun showSelectApiVersionMenu() {
        getBinding()?.apiSelector?.let { apiSelector ->
            val menu = PopupMenu(requireActivity(), apiSelector)
            ApiVersionEnum.values().forEach {
                menu.menu.add(Menu.NONE, 0, 0, it.toString())
            }
            menu.setOnMenuItemClickListener {
                viewModel.setSelectedApiVersion(it.title.toString())
                true
            }
            menu.show()
        }
    }

    fun setUnreadCount(count: Int) {
        getBinding()?.count?.isVisible = count > 0
        getBinding()?.count?.text = count.toString()
    }

    private fun initReceivers() {
        if (!ThreadsLib.isInitialized()) {
            initLibReceiver = InitThreadsLibReceiver(this)
            ContextCompat.registerReceiver(
                requireContext(),
                initLibReceiver,
                initLibFilter,
                ContextCompat.RECEIVER_VISIBLE_TO_INSTANT_APPS
            )
        }
        unreadCountReceiver = InitUnreadCountReceiver(this)
        ContextCompat.registerReceiver(
            requireContext(),
            unreadCountReceiver,
            unreadCountFilter,
            ContextCompat.RECEIVER_VISIBLE_TO_INSTANT_APPS
        )
    }

    class InitUnreadCountReceiver(val fragment: LaunchFragment) : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == APP_UNREAD_COUNT_BROADCAST) {
                val count = intent.getIntExtra(UNREAD_COUNT_KEY, 0)
                fragment.setUnreadCount(count)
            }
        }
    }

    private fun showUiThemesSelector(theme: CurrentUiTheme) {
        context?.let { context ->
            val alertDialogBuilder: AlertDialog.Builder = AlertDialog.Builder(context)
            var alertDialog: AlertDialog? = null
            alertDialogBuilder.setTitle(stringsProvider.selectTheme)
            val items = arrayOf(
                stringsProvider.defaultTheme,
                stringsProvider.lightTheme,
                stringsProvider.darkTheme
            )
            val checkedItem = theme.value
            alertDialogBuilder.setSingleChoiceItems(
                items,
                checkedItem
            ) { _, selectedIndex ->
                viewModel.saveUserSelectedUiTheme(CurrentUiTheme.fromInt(selectedIndex))
                alertDialog?.dismiss()
            }
            alertDialog = alertDialogBuilder.create()
            alertDialog?.show()
        }
    }

    private fun generateAboutText(): String {
        return "${getString(R.string.app_name)}  " +
            "v${BuildConfig.VERSION_NAME} " +
            "(${BuildConfig.VERSION_CODE})" +
            "/ ChatCenter SDK ${ThreadsLib.getLibVersion()}"
    }

    private fun restartApp(isRestart: Boolean) {
        if (isRestart) {
            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(
                    context,
                    context?.getString(R.string.app_will_be_restarted),
                    Toast.LENGTH_SHORT
                ).show()
                delay(2000)

                context?.apply {
                    val intent = packageManager.getLaunchIntentForPackage(packageName)
                    intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    android.os.Process.killProcess(android.os.Process.myPid())
                }
            }
        }
    }

    fun onThreadsLibInitialized() {
        setToolbarColor()
        viewModel.checkUiTheme()
        unregisterInitLibReceivers()
    }

    class InitThreadsLibReceiver(val fragment: LaunchFragment) : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == APP_INIT_THREADS_LIB_ACTION) {
                fragment.onThreadsLibInitialized()
            }
        }
    }

    companion object {
        const val SELECTED_USER_KEY = "selected_user_key"
        const val SELECTED_SERVER_CONFIG_KEY = "selected_server_key"
        const val APP_INIT_THREADS_LIB_ACTION = "APP_INIT_THREADS_LIB_BROADCAST"
        const val UNREAD_COUNT_KEY = "unread_cont_key"
        const val APP_UNREAD_COUNT_BROADCAST = "unread_count_broadcast"
    }
}
