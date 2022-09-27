package im.threads.android.ui.addServerDialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import im.threads.android.R
import im.threads.android.data.ServerConfig
import im.threads.android.databinding.DialogAddServerBinding
import im.threads.android.useCases.developerOptions.DebugMenuUseCase
import org.koin.android.ext.android.inject

class AddServerDialog(private val callback: AddServerDialogActions) : DialogFragment() {
    private lateinit var binding: DialogAddServerBinding
    private val devOptions: DebugMenuUseCase by inject()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dialog?.window?.apply {
            requestFeature(Window.FEATURE_NO_TITLE)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        binding = DialogAddServerBinding.inflate(inflater)
        with(binding) {
            saveButton.setOnClickListener {
                val name = nameEditText.text.toString()
                val datastoreUrl = datastoreEditText.text.toString()
                val serverBaseUrl = baseUrlEditText.text.toString()
                val threadsGateUrl = threadsGateUrlEditText.text.toString()
                val threadsGateProviderUid = threadsGateProviderUidEditText.text.toString()

                if (name.isNotBlank() && datastoreUrl.isNotBlank() && serverBaseUrl.isNotBlank() &&
                    threadsGateUrl.isNotBlank() && threadsGateProviderUid.isNotBlank()
                ) {
                    val serverConfig = ServerConfig(
                        name,
                        datastoreUrl,
                        serverBaseUrl,
                        threadsGateUrl,
                        threadsGateProviderUid,
                        filesAndMediaMenuItemEnabledSwitch.isChecked,
                        newChatCenterApiSwitch.isChecked,
                        true
                    )
                    devOptions.addServer(serverConfig)
                    callback.onServerAdded()
                    dismiss()
                } else {
                    context?.let {
                        Toast.makeText(
                            it,
                            getString(R.string.fill_all_inputs),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            cancelButton.setOnClickListener {
                dismiss()
            }
        }
        return binding.root
    }

    companion object {
        private const val TAG = "AddServerDialog"
        fun open(activity: AppCompatActivity, callback: AddServerDialogActions) {
            AddServerDialog(callback).show(activity.supportFragmentManager, TAG)
        }
    }
}
