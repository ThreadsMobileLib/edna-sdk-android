package io.edna.threads.demo.appCode.adapters.serverList

import androidx.recyclerview.widget.DiffUtil
import io.edna.threads.demo.appCode.models.ServerConfig

class ServerListDiffCallback(
    private val oldList: List<ServerConfig>,
    private val newList: List<ServerConfig>
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
