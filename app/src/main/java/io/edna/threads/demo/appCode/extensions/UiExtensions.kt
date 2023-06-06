package io.edna.threads.demo.appCode.extensions

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding

fun <T : ViewDataBinding> LayoutInflater.inflateWithBinding(
    viewGroup: ViewGroup?,
    layoutRes: Int
): T {
    return DataBindingUtil.inflate(this, layoutRes, viewGroup, false)
}

fun <T : ViewDataBinding> Activity.inflateWithBinding(layoutRes: Int): T {
    return DataBindingUtil.setContentView(this, layoutRes)
}
