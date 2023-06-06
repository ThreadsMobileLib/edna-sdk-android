package io.edna.threads.demo.appCode.business

import android.text.TextWatcher

interface AfterTextChangedTextWatcher : TextWatcher {
    override fun beforeTextChanged(str: CharSequence?, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(str: CharSequence?, start: Int, before: Int, count: Int) {}
}
