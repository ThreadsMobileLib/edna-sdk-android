package im.threads.android.ui

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import im.threads.android.R
import im.threads.android.data.Card
import im.threads.android.databinding.DialogEditCardBinding
import im.threads.android.network.AuthProvider
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

/**
 * Диалог добавления пользователя
 */
class EditCardDialog : DialogFragment() {
    lateinit var binding: DialogEditCardBinding
    private var signatureDisposable: Disposable? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val card = arguments?.getParcelable<Card>(ARG_CARD)
        dialog?.window?.apply {
            requestFeature(Window.FEATURE_NO_TITLE)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        binding = DialogEditCardBinding.inflate(inflater)
        card?.let {
            binding.clientId.isEnabled = false
            binding.clientId.setText(it.userId)
            binding.clientData.setText(it.clientData)
            binding.appMarker.setText(it.appMarker)
            binding.clientIdSignature.setText(it.clientIdSignature)
            binding.authToken.setText(it.authToken)
            binding.authSchema.setText(it.authSchema)
        }
        isCancelable = false
        updateAddButtonEnabled()
        binding.clientId.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                updateAddButtonEnabled()
            }
        })
        binding.addButton.setOnClickListener {
            val clientId = binding.clientId.text.toString()
            val clientIdSignature = binding.clientIdSignature.text.toString()
            if (TextUtils.isEmpty(clientId)) {
                binding.clientIdInputLayout.isErrorEnabled = true
                binding.clientIdInputLayout.error = "ClientId is empty"
            } else {
                binding.clientIdInputLayout.isErrorEnabled = false
                binding.clientIdInputLayout.error = null
                if (TextUtils.isEmpty(clientIdSignature)) {
                    signatureDisposable?.dispose()
                    signatureDisposable = AuthProvider.getSignature(clientId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                            { signature: String ->
                                val newCard = Card(
                                    binding.clientId.text.toString(),
                                    binding.clientData.text.toString(),
                                    binding.appMarker.text.toString(),
                                    signature,
                                    binding.authToken.text.toString(),
                                    binding.authSchema.text.toString()
                                )
                                saveButtonClicked(newCard)
                                dismiss()
                            }
                        ) {
                            showError(R.string.demo_get_signature_error)
                            val newCard = Card(
                                binding.clientId.text.toString(),
                                binding.clientData.text.toString(),
                                binding.appMarker.text.toString(),
                                "",
                                binding.authToken.text.toString(),
                                binding.authSchema.text.toString()
                            )
                            saveButtonClicked(newCard)
                            dismiss()
                        }
                } else {
                    val newCard = Card(
                        binding.clientId.text.toString(),
                        binding.clientData.text.toString(),
                        binding.appMarker.text.toString(),
                        clientIdSignature,
                        binding.authToken.text.toString(),
                        binding.authSchema.text.toString()
                    )
                    saveButtonClicked(newCard)
                    dismiss()
                }
            }
        }
        binding.cancelButton.setOnClickListener {
            cancelButtonClicked()
            dismiss()
        }
        return binding.root
    }

    private fun updateAddButtonEnabled() {
        binding.addButton.isEnabled = binding.clientId.length() > 0
    }

    private fun saveButtonClicked(newCard: Card) {
        val targetFragment = targetFragment
        if (targetFragment != null) {
            val fragment = getTargetFragment()
            if (fragment is EditCardDialogActionsListener) {
                fragment.onCardSaved(newCard)
            }
        } else {
            val activity: Activity? = activity
            if (activity is EditCardDialogActionsListener) {
                activity.onCardSaved(newCard)
            }
        }
    }

    private fun cancelButtonClicked() {
        val targetFragment = targetFragment
        if (targetFragment != null) {
            val fragment = getTargetFragment()
            if (fragment is EditCardDialogActionsListener) {
                fragment.onCancel()
            }
        } else {
            val activity: Activity? = activity
            if (activity is EditCardDialogActionsListener) {
                activity.onCancel()
            }
        }
    }

    private fun showError(@StringRes errorMessageResId: Int) {
        Toast.makeText(context, errorMessageResId, Toast.LENGTH_SHORT).show()
    }

    interface EditCardDialogActionsListener {
        fun onCardSaved(newCard: Card?)
        fun onCancel()
    }

    companion object {
        private const val TAG = "EditCardDialog"
        private const val ARG_CARD = "ARG_CARD"

        @JvmStatic
        fun open(activity: AppCompatActivity, card: Card? = null) {
            val editCardDialog = EditCardDialog()
            val args = Bundle()
            args.putParcelable(ARG_CARD, card)
            editCardDialog.arguments = args
            editCardDialog.show(activity.supportFragmentManager, TAG)
        }

        @JvmStatic
        fun open(activity: AppCompatActivity) {
            open(activity, null)
        }
    }
}
