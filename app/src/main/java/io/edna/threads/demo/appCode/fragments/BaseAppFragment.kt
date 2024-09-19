package io.edna.threads.demo.appCode.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

abstract class BaseAppFragment<T : ViewBinding>(
    private val bindingInflater: (layoutInflater: LayoutInflater) -> T
) : Fragment() {
    protected var fragment: WeakReference<ChatFragment>? = null
    private var binding: WeakReference<T>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = WeakReference(bindingInflater.invoke(inflater))
        return getBinding()?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setToolbarColor()
    }

    override fun onResume() {
        super.onResume()
        checkIsAppShouldBeRestarted()
    }

    protected fun subscribeToGlobalBackClick() {
        activity?.onBackPressedDispatcher?.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    navigateUp()
                }
            }
        )
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

    override fun onDestroy() {
        if (this is DemoSamplesListFragment) {
            isAppShouldBeRestarted = true
        }
        super.onDestroy()
    }

    protected open fun navigateUp() {
        val isDemoListFragment = this is DemoSamplesListFragment
        val chatBackPressed = fragment?.get()?.onBackPressed() == true
        if ((chatBackPressed || isDemoListFragment) && isAdded) {
            if (this@BaseAppFragment is ChatAppFragment || this@BaseAppFragment is DemoSamplesListFragment) {
                ThreadsLib.getInstance().logoutClient()
            }
            findNavController().navigateUp()
        }
    }

    protected fun setToolbarColor() = getBinding()?.apply {
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

    protected fun restartApp() {
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

    protected fun getBinding() = binding?.get()

    private fun checkIsAppShouldBeRestarted() {
        if (isAppShouldBeRestarted) {
            restartApp()
        }
    }

    companion object {
        var isAppShouldBeRestarted = false
    }
}
