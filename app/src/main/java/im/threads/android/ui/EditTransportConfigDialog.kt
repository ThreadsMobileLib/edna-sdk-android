package im.threads.android.ui

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.jakewharton.processphoenix.ProcessPhoenix
import im.threads.android.data.TransportConfig
import im.threads.android.databinding.DialogEditTransportConfigBinding
import im.threads.android.utils.PrefUtils
import im.threads.internal.utils.MetaDataUtils

/**
 * Диалог редактирования настроек транспорта
 */
class EditTransportConfigDialog : DialogFragment() {
    var binding: DialogEditTransportConfigBinding? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog?.window?.apply {
            requestFeature(Window.FEATURE_NO_TITLE)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        binding = DialogEditTransportConfigBinding.inflate(inflater)
        binding?.apply {
            saveButton.setOnClickListener {
                PrefUtils.saveTransportConfig(
                    requireContext(),
                    TransportConfig(
                        baseUrl = baseUrl.text.toString(),
                        threadsGateUrl = threadsGateUrl.text.toString(),
                        threadsGateProviderUid = threadsGateProviderUid.text.toString(),
                        threadsGateHCMProviderUid = threadsGateHcmProviderUid.text.toString()
                    )
                )
                ProcessPhoenix.triggerRebirth(
                    requireContext(),
                    Intent(requireContext(), MainActivity::class.java)
                )
            }
            cancelButton.setOnClickListener {
                dismiss()
            }
            val context = requireContext()
            val transportConfig = PrefUtils.getTransportConfig(context)
            if (transportConfig != null) {
                baseUrl.setText(transportConfig.baseUrl)
                threadsGateUrl.setText(transportConfig.threadsGateUrl)
                threadsGateProviderUid.setText(transportConfig.threadsGateProviderUid)
                threadsGateHcmProviderUid.setText(transportConfig.threadsGateHCMProviderUid)
            } else {
                baseUrl.setText(MetaDataUtils.getDatastoreUrl(context))
                threadsGateUrl.setText(MetaDataUtils.getThreadsGateUrl(context))
                threadsGateProviderUid.setText(MetaDataUtils.getThreadsGateProviderUid(context))
                val gateHcmProviderUid = MetaDataUtils.getThreadsGateHCMProviderUid(context)
                threadsGateHcmProviderUid.setText(gateHcmProviderUid)
            }
        }
        return binding?.root
    }

    companion object {
        private const val TAG = "EditTransportConfigDialog"
        fun open(activity: AppCompatActivity) {
            EditTransportConfigDialog().show(activity.supportFragmentManager, TAG)
        }
    }
}
