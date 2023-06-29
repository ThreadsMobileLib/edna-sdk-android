package io.edna.threads.demo.integrationCode.fragments.launch

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.fragment.app.clearFragmentResultListener
import androidx.fragment.app.setFragmentResultListener
import im.threads.business.models.enums.CurrentUiTheme
import im.threads.ui.core.ThreadsLib
import io.edna.threads.demo.BuildConfig
import io.edna.threads.demo.R
import io.edna.threads.demo.appCode.business.StringsProvider
import io.edna.threads.demo.appCode.extensions.inflateWithBinding
import io.edna.threads.demo.appCode.fragments.BaseAppFragment
import io.edna.threads.demo.appCode.models.UiTheme
import io.edna.threads.demo.databinding.FragmentLaunchBinding
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class LaunchFragment : BaseAppFragment<FragmentLaunchBinding>() {
    private val viewModel: LaunchViewModel by viewModel()
    private val stringsProvider: StringsProvider by inject()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreate(savedInstanceState)
        _binding = inflater.inflateWithBinding(container, R.layout.fragment_launch)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        initObservers()
        setResultListeners()
        initView()
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        clearResultListeners()
    }

    private fun initView() = with(binding) {
        login.isEnabled = false
        about.text = generateAboutText()
    }

    private fun initObservers() {
        viewModel.currentUiThemeLiveData.observe(viewLifecycleOwner) { setUiThemeDependentViews(it) }
        viewModel.themeSelectorLiveData.observe(viewLifecycleOwner) { showUiThemesSelector(it) }
        viewLifecycleOwner.lifecycle.addObserver(viewModel)
        viewModel.subscribeForData(viewLifecycleOwner)
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

    private fun setUiThemeDependentViews(theme: UiTheme) = with(binding) {
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

    companion object {
        const val SELECTED_USER_KEY = "selected_user_key"
        const val SELECTED_SERVER_CONFIG_KEY = "selected_server_key"
    }
}
