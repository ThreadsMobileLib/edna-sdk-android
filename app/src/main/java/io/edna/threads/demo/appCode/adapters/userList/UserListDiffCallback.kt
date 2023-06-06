package io.edna.threads.demo.appCode.adapters.userList

import androidx.recyclerview.widget.DiffUtil
import io.edna.threads.demo.appCode.models.UserInfo

class UserListDiffCallback(
    private val oldList: List<UserInfo>,
    private val newList: List<UserInfo>
) : DiffUtil.Callback() {

    override fun getOldListSize() = oldList.size

    override fun getNewListSize() = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldList[oldItemPosition]
        val newItem = newList[newItemPosition]
        return oldItem == newItem
    }
}
