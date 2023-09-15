package io.edna.threads.demo.appCode.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import im.threads.ui.core.ThreadsLib
import im.threads.ui.extensions.isDarkThemeOn
import im.threads.ui.fragments.ChatFragment
import io.edna.threads.demo.R
import io.edna.threads.demo.appCode.fragments.demoSamplesList.DemoSamplesListFragment
import io.edna.threads.demo.integrationCode.fragments.chatFragment.ChatAppFragment

abstract class BaseAppFragment<T : ViewBinding>(
    private val bindingInflater: (layoutInflater: LayoutInflater) -> T
) : Fragment() {
    protected var fragment: ChatFragment? = null
    private var _binding: T? = null
    protected val binding by lazy { _binding!! }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = bindingInflater.invoke(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setToolbarColor()
    }

    protected fun subscribeToGlobalBackClick() {
        activity?.onBackPressedDispatcher?.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navigateUp()
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    protected open fun navigateUp() {
        val isDemoListFragment = this is DemoSamplesListFragment
        val chatBackPressed = fragment?.onBackPressed() == true
        if ((chatBackPressed || isDemoListFragment) && isAdded) {
            if (this@BaseAppFragment is ChatAppFragment || this@BaseAppFragment is DemoSamplesListFragment) {
                ThreadsLib.getInstance().logoutClient()
            }
            findNavController().navigateUp()
        }
    }

    protected fun setToolbarColor() = with(binding) {
        if (ThreadsLib.isInitialized()) {
            context?.let { context ->
                val toolbar = try {
                    root.findViewById<Toolbar>(R.id.toolbar)
                } catch (ignored: Exception) {
                    null
                }

                if (context.isDarkThemeOn()) {
                    toolbar?.setBackgroundColor(ContextCompat.getColor(context, R.color.dark_toolbar))
                } else {
                    toolbar?.setBackgroundColor(ContextCompat.getColor(context, R.color.light_toolbar))
                }
            }
        }
    }
}
