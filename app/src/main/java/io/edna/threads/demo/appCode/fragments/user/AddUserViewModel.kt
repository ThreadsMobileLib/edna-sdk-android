package io.edna.threads.demo.appCode.fragments.user

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.text.Editable
import android.view.View
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import androidx.navigation.findNavController
import io.edna.threads.demo.R
import io.edna.threads.demo.appCode.business.AfterTextChangedTextWatcher
import io.edna.threads.demo.appCode.business.StringsProvider
import io.edna.threads.demo.appCode.models.UserInfo
import org.parceler.Parcels

class AddUserViewModel(
    private val stringsProvider: StringsProvider
) : ViewModel(), DefaultLifecycleObserver {

    var srcUser: UserInfo? = null
    var finalUserLiveData = MutableLiveData<UserInfo>(null)
    private var _userLiveData = MutableLiveData(UserInfo())
    var userLiveData: LiveData<UserInfo> = _userLiveData

    private var _errorStringForUserIdFieldLiveData = MutableLiveData<String?>(null)
    var errorStringForUserIdFieldLiveData: LiveData<String?> = _errorStringForUserIdFieldLiveData

    fun initData(arguments: Bundle?) {
        if (arguments != null && arguments.containsKey(UserListFragment.USER_KEY)) {
            val user: UserInfo? = if (Build.VERSION.SDK_INT >= 33) {
                Parcels.unwrap(arguments.getParcelable(UserListFragment.USER_KEY, Parcelable::class.java))
            } else {
                Parcels.unwrap(arguments.getParcelable(UserListFragment.USER_KEY))
            }
            if (user != null) {
                srcUser = user
                _userLiveData.value = user.clone()
            }
        }
    }

    fun click(view: View) {
        val navigationController: NavController =
            (view.context as Activity).findNavController(R.id.nav_host_fragment_content_main)
        when (view.id) {
            R.id.backButton -> navigationController.navigate(R.id.action_AddUserFragment_to_UserListFragment)
            R.id.okButton -> {
                if (userLiveData.value?.isAllFieldsFilled() == true) {
                    finalUserLiveData.postValue(userLiveData.value)
                    navigationController.navigate(R.id.action_AddUserFragment_to_UserListFragment)
                } else {
                    setupErrorFields(userLiveData.value)
                }
            }
        }
    }

    private fun setupErrorFields(user: UserInfo?) {
        if (user == null) {
            _errorStringForUserIdFieldLiveData.postValue(stringsProvider.requiredField)
        } else {
            if (user.userId.isNullOrEmpty()) {
                _errorStringForUserIdFieldLiveData.postValue(stringsProvider.requiredField)
            }
        }
    }

    val userIdTextWatcher = object : AfterTextChangedTextWatcher {
        override fun afterTextChanged(s: Editable?) {
            if (s != null) {
                if (userLiveData.value?.userId != s.toString()) {
                    userLiveData.value?.userId = s.toString()
                    _userLiveData.postValue(userLiveData.value)
                }
                if (s.isNotEmpty()) {
                    _errorStringForUserIdFieldLiveData.postValue(null)
                }
            }
        }
    }

    val userDataTextWatcher = object : AfterTextChangedTextWatcher {
        override fun afterTextChanged(s: Editable?) {
            if (s != null) {
                if (userLiveData.value?.userData != s.toString()) {
                    userLiveData.value?.userData = s.toString()
                    _userLiveData.postValue(userLiveData.value)
                }
            }
        }
    }

    val appMarkerTextWatcher = object : AfterTextChangedTextWatcher {
        override fun afterTextChanged(s: Editable?) {
            if (s != null) {
                if (userLiveData.value?.appMarker != s.toString()) {
                    userLiveData.value?.appMarker = s.toString()
                    _userLiveData.postValue(userLiveData.value)
                }
            }
        }
    }

    val signatureTextWatcher = object : AfterTextChangedTextWatcher {
        override fun afterTextChanged(s: Editable?) {
            if (s != null) {
                if (userLiveData.value?.signature != s.toString()) {
                    userLiveData.value?.signature = s.toString()
                    _userLiveData.postValue(userLiveData.value)
                }
            }
        }
    }

    val authorizationHeaderTextWatcher = object : AfterTextChangedTextWatcher {
        override fun afterTextChanged(s: Editable?) {
            if (s != null) {
                if (userLiveData.value?.authorizationHeader != s.toString()) {
                    userLiveData.value?.authorizationHeader = s.toString()
                    _userLiveData.postValue(userLiveData.value)
                }
            }
        }
    }

    val xAuthSchemaHeaderTextWatcher = object : AfterTextChangedTextWatcher {
        override fun afterTextChanged(s: Editable?) {
            if (s != null) {
                if (userLiveData.value?.xAuthSchemaHeader != s.toString()) {
                    userLiveData.value?.xAuthSchemaHeader = s.toString()
                    _userLiveData.postValue(userLiveData.value)
                }
            }
        }
    }
}
