package im.threads.android.ui

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.databinding.DataBindingUtil
import im.threads.android.R
import im.threads.android.databinding.ActivityWebviewBinding
import im.threads.internal.Config
import im.threads.internal.activities.BaseActivity
import im.threads.internal.utils.ColorsHelper

/**
 * Активность для открытия ссылок вида : webview://www.mail.ru
 */
class WebViewActivity : BaseActivity() {

    private lateinit var binding: ActivityWebviewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()

        val url = extractLink(intent.data)
        if (url != null) {
            binding.webView.loadUrl(url)
            binding.title.text = url
        }
    }

    private fun initView() {
        val chatStyle = Config.instance.chatStyle
        binding = DataBindingUtil.setContentView(this, R.layout.activity_webview)
        binding.backButton.setOnClickListener { finish() }

        binding.backButton.setImageResource(R.drawable.ic_arrow_back_white_24dp)
        ColorsHelper.setTint(this, binding.backButton, chatStyle.chatToolbarTextColorResId)

        ColorsHelper.setBackgroundColor(
            this,
            binding.toolbar,
            Config.instance.chatStyle.chatToolbarColorResId
        )

        ColorsHelper.setBackgroundColor(
            this,
            binding.webView,
            chatStyle.chatBackgroundColor
        )
        ColorsHelper.setTextColor(
            this,
            binding.title,
            chatStyle.chatToolbarTextColorResId
        )

        ColorsHelper.setStatusBarColor(
            this,
            chatStyle.chatStatusBarColorResId,
            chatStyle.windowLightStatusBarResId
        )

        binding.webView.settings.javaScriptEnabled = true
        binding.webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                view.loadUrl(url)
                return true
            }
        }
    }

    private fun extractLink(url: Uri?): String? {
        return if (url == null) {
            url
        } else {
            url.toString().replaceFirst(Regex("webview://"), "https://")
        }
    }

    override fun onBackPressed() {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q && isTaskRoot) {
            finishAfterTransition()
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        binding.webView.stopLoading()
        binding.webView.onPause()
        binding.webView.removeAllViews()
        binding.webView.clearCache(true)
        binding.webView.clearHistory()
        super.onDestroy()
    }
}