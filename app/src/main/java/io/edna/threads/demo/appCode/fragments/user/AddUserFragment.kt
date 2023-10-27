package io.edna.threads.demo.appCode.fragments.user

import android.os.Bundle
import android.view.View
import androidx.fragment.app.setFragmentResult
import io.edna.threads.demo.appCode.fragments.BaseAppFragment
import io.edna.threads.demo.appCode.fragments.user.UserListFragment.Companion.SRC_USER_ID_KEY
import io.edna.threads.demo.appCode.fragments.user.UserListFragment.Companion.USER_KEY
import io.edna.threads.demo.databinding.FragmentAddUserBinding
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.parceler.Parcels

class AddUserFragment : BaseAppFragment<FragmentAddUserBinding>(FragmentAddUserBinding::inflate) {

    private val viewModel: AddUserViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeToGlobalBackClick()
        subscribeForClickListeners()
        subscribeForTextWatchers()
        subscribeForData()
        initData()
    }

    private fun subscribeForTextWatchers() = getBinding()?.apply {
        userId.setTextChangedListener(viewModel.userIdTextWatcher)
        userData.setTextChangedListener(viewModel.userDataTextWatcher)
        appMarker.setTextChangedListener(viewModel.appMarkerTextWatcher)
        signature.setTextChangedListener(viewModel.signatureTextWatcher)
        authorizationHeader.setTextChangedListener(viewModel.authorizationHeaderTextWatcher)
        xAuthSchemaHeader.setTextChangedListener(viewModel.xAuthSchemaHeaderTextWatcher)
    }

    private fun subscribeForClickListeners() = getBinding()?.apply {
        backButton.setOnClickListener { viewModel.click(backButton) }
        okButton.setOnClickListener { viewModel.click(okButton) }
    }

    private fun subscribeForData() = getBinding()?.apply {
        viewModel.finalUserLiveData.observe(viewLifecycleOwner) {
            val args = Bundle()
            args.putParcelable(USER_KEY, Parcels.wrap(it))
            viewModel.srcUser?.let { user ->
                args.putString(SRC_USER_ID_KEY, user.userId)
            }
            setFragmentResult(USER_KEY, args)
        }
        viewModel.userLiveData.observe(viewLifecycleOwner) {
            userId.text = it.userId
            userData.text = it.userData
            appMarker.text = it.appMarker
            signature.text = it.signature
            authorizationHeader.text = it.authorizationHeader
            xAuthSchemaHeader.text = it.xAuthSchemaHeader
        }
        viewModel.errorStringForUserIdFieldLiveData.observe(viewLifecycleOwner) {
            userId.error = it
        }
    }

    private fun initData() {
        viewModel.initData(arguments)
    }
}
