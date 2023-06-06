package io.edna.threads.demo.appCode.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import im.threads.ui.extensions.isDarkThemeOn
import im.threads.ui.fragments.ChatFragment
import io.edna.threads.demo.R

typealias Inflate<T> = (LayoutInflater, ViewGroup?, Boolean) -> T

abstract class BaseAppFragment<T : ViewDataBinding> : Fragment() {
    protected var fragment: ChatFragment? = null
    protected var _binding: T? = null
    protected val binding by lazy { _binding!! }

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
        if (fragment?.onBackPressed() == true && isAdded) {
            findNavController().navigateUp()
        }
    }

    private fun setToolbarColor() = with(binding) {
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
