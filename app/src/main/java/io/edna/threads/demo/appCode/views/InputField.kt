package io.edna.threads.demo.appCode.views

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.google.android.material.textfield.TextInputEditText
import io.edna.threads.demo.R
import io.edna.threads.demo.R.styleable.InputField
import io.edna.threads.demo.appCode.business.UiThemeProvider
import org.koin.java.KoinJavaComponent

class InputField : FrameLayout, View.OnFocusChangeListener, TextWatcher {

    private val uiThemeProvider: UiThemeProvider by KoinJavaComponent.inject(UiThemeProvider::class.java)
    private var isInFocus: Boolean = false
    private var textInputField: TextInputEditText
    private var hintText: AppCompatTextView
    private var errorText: AppCompatTextView
    private var commonLayout: FrameLayout

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attr: AttributeSet?) : this(context, attr, 0)
    constructor(context: Context, attr: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attr,
        defStyleAttr
    ) {
        if (attr != null) {
            val attributes = context.obtainStyledAttributes(attr, InputField)
            attributes.getString(R.styleable.InputField_hint)?.let {
                hint = it
            }
            attributes.getString(R.styleable.InputField_text)?.let {
                text = it
            }
            updateHintView()
            onFocusChange(this, isInFocus)
            attributes.recycle()
        }
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.input_field, this, true)
        textInputField = findViewById(R.id.textInputField)
        hintText = findViewById(R.id.hintText)
        errorText = findViewById(R.id.errorText)
        commonLayout = findViewById(R.id.commonLayout)
        textInputField.onFocusChangeListener = this
        textInputField.addTextChangedListener(this)
        commonLayout.setOnClickListener {
            textInputField.requestFocus()
            showKeyboard(textInputField)
        }
        if (uiThemeProvider.isDarkThemeOn()) {
            commonLayout.setBackgroundResource(R.color.black_color_2d)
            textInputField.setTextColor(ContextCompat.getColor(context, R.color.white_color_fa))
        } else {
            commonLayout.setBackgroundResource(R.color.gray_color_f4)
            textInputField.setTextColor(ContextCompat.getColor(context, R.color.black_color))
        }
    }

    override fun onFocusChange(v: View?, hasFocus: Boolean) {
        isInFocus = hasFocus
        updateHintView()
    }

    /** Переносит текст подсказски в центр, на место поля ввода */
    private fun hintToCenter() {
        val params: LayoutParams = hintText.layoutParams as LayoutParams
        params.apply {
            gravity = Gravity.CENTER and Gravity.CENTER_VERTICAL
        }
        hintText.layoutParams = params
        hintText.setTextSize(
            TypedValue.COMPLEX_UNIT_PX,
            resources.getDimension(R.dimen.text_size_16)
        )
        visibility = View.VISIBLE
        if (!hint.isNullOrEmpty()) {
            hintText.text = hint
        }
    }

    /**
     * Переносит текст подсказски в верх, когда в поле ввода есть какой-то
     * текст
     */
    private fun hintToTop() {
        val params: LayoutParams = hintText.layoutParams as LayoutParams
        params.apply {
            gravity = Gravity.TOP
        }
        hintText.layoutParams = params
        hintText.setTextSize(
            TypedValue.COMPLEX_UNIT_PX,
            resources.getDimension(R.dimen.text_size_12)
        )
        visibility = View.VISIBLE
    }

    private fun updateHintView() {
        if (!hint.isNullOrEmpty()) {
            if (!text.isNullOrEmpty() || isInFocus) {
                hintToTop()
            } else {
                hintToCenter()
            }
        }
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        if (textInputField.text.isNullOrEmpty() && !isInFocus) {
            hintToCenter()
        } else {
            hintToTop()
        }
        textInputField.setSelection(textInputField.text?.length ?: 0)
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun afterTextChanged(s: Editable?) {}

    private fun showKeyboard(view: View) {
        view.requestFocus()
        val inputMethodManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(view, 0)
        view.requestFocus()
    }

    var error: String?
        get() = errorText.text.toString()
        set(value) {
            errorText.isVisible = !value.isNullOrEmpty()
            errorText.text = value
        }

    var hint: String?
        get() = hintText.text.toString()
        set(value) {
            hintText.text = value
        }

    var text: String?
        get() = textInputField.text.toString()
        set(value) = textInputField.setText(value)

    fun setTextChangedListener(listener: TextWatcher) {
        textInputField.addTextChangedListener(listener)
    }
}
