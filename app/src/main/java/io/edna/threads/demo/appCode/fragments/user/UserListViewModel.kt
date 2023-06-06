package io.edna.threads.demo.appCode.fragments.user

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import androidx.navigation.findNavController
import io.edna.threads.demo.R
import io.edna.threads.demo.appCode.business.PreferencesProvider
import io.edna.threads.demo.appCode.models.UserInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.parceler.Parcels

class UserListViewModel(
    private val preferencesProvider: PreferencesProvider
) : ViewModel(), DefaultLifecycleObserver {

    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private var _userListLiveData = MutableLiveData(ArrayList<UserInfo>())
    var userListLiveData: LiveData<ArrayList<UserInfo>> = _userListLiveData

    private var _backButtonClickedLiveData = MutableLiveData(false)
    var backButtonClickedLiveData: LiveData<Boolean> = _backButtonClickedLiveData

    fun click(view: View) {
        val navigationController: NavController =
            (view.context as Activity).findNavController(R.id.nav_host_fragment_content_main)
        when (view.id) {
            R.id.backButton -> _backButtonClickedLiveData.postValue(true)
            R.id.addUser -> {
                navigationController.navigate(R.id.action_UserListFragment_to_AddUserFragment)
            }
        }
    }

    fun backToLaunchScreen(context: Context?) {
        if (context != null) {
            val navigationController: NavController =
                (context as Activity).findNavController(R.id.nav_host_fragment_content_main)
            navigationController.navigate(R.id.action_UserListFragment_to_LaunchFragment)
        }
    }

    private fun addUser(user: UserInfo) {
        coroutineScope.launch {
            val userList = preferencesProvider.getAllUserList()
            val newUserList = updateUserList(userList, user)
            _userListLiveData.postValue(newUserList)
            preferencesProvider.saveUserList(newUserList)
        }
    }

    fun removeUser(user: UserInfo) {
        coroutineScope.launch {
            val srcUserList = preferencesProvider.getAllUserList()
            val finalUserList = removeUser(srcUserList, user)
            _userListLiveData.postValue(finalUserList)
            preferencesProvider.saveUserList(finalUserList)
        }
    }

    fun loadUserList() {
        _userListLiveData.postValue(preferencesProvider.getAllUserList())
    }

    fun callFragmentResultListener(key: String, bundle: Bundle) {
        if (key == UserListFragment.USER_KEY && bundle.containsKey(UserListFragment.USER_KEY)) {
            val user: UserInfo? = if (Build.VERSION.SDK_INT >= 33) {
                Parcels.unwrap(
                    bundle.getParcelable(
                        UserListFragment.USER_KEY,
                        Parcelable::class.java
                    )
                )
            } else {
                Parcels.unwrap(bundle.getParcelable(UserListFragment.USER_KEY))
            }
            if (user != null) {
                addUser(user)
            }
        }
    }

    private fun removeUser(
        userList: ArrayList<UserInfo>,
        user: UserInfo
    ): ArrayList<UserInfo> {
        userList.remove(user)
        return userList
    }

    private fun updateUserList(
        userList: ArrayList<UserInfo>,
        newUser: UserInfo
    ): ArrayList<UserInfo> {
        val serversMap = HashMap<String?, UserInfo>()
        userList.forEach {
            serversMap[it.nickName] = it
        }
        serversMap[newUser.nickName] = newUser
        return ArrayList(serversMap.values)
    }
}
