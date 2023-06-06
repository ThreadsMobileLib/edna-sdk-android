package io.edna.threads.demo.appCode.adapters.userList

import io.edna.threads.demo.appCode.models.UserInfo

interface UserListItemOnClickListener {
    fun onClick(item: UserInfo)
    fun onEditItem(item: UserInfo)
    fun onRemoveItem(item: UserInfo)
}
