package io.edna.threads.demo.appCode.adapters.userList

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import im.threads.ui.utils.ColorsHelper
import io.edna.threads.demo.R
import io.edna.threads.demo.appCode.adapters.ListItemClickListener
import io.edna.threads.demo.appCode.extensions.isDarkThemeOn
import io.edna.threads.demo.appCode.models.UserInfo
import io.edna.threads.demo.databinding.UserListItemBinding
import java.lang.ref.WeakReference

class UserListAdapter(private val onItemClickListener: WeakReference<ListItemClickListener>) :
    RecyclerView.Adapter<UserListAdapter.UserItemHolder>() {

    private val list: MutableList<UserInfo> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserItemHolder {
        val inflater = LayoutInflater.from(parent.context)
        return UserItemHolder(UserListItemBinding.inflate(inflater))
    }

    override fun onBindViewHolder(holder: UserItemHolder, position: Int) {
        holder.onBind(position)
    }

    override fun getItemCount() = list.count()

    fun addItems(newItems: List<UserInfo>) {
        notifyDatasetChangedWithDiffUtil(newItems)
    }

    fun getItem(position: Int): UserInfo {
        return list[position]
    }

    private fun notifyDatasetChangedWithDiffUtil(newList: List<UserInfo>) {
        val diffResult = DiffUtil.calculateDiff(UserListDiffCallback(list, newList))
        list.clear()
        list.addAll(newList)
        diffResult.dispatchUpdatesTo(this)
    }

    inner class UserItemHolder(private val binding: UserListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun onBind(position: Int) {
            (list[position] as? UserInfo)?.let { item ->
                binding.userId.text = item.userId
                if (binding.root.context.isDarkThemeOn()) {
                    binding.userId.setTextColor(ContextCompat.getColor(binding.root.context, R.color.white_color_fa))
                    ColorsHelper.setTint(binding.root.context, binding.image, R.color.white_color_fa)
                } else {
                    binding.userId.setTextColor(ContextCompat.getColor(binding.root.context, R.color.black_color))
                    ColorsHelper.setTint(binding.root.context, binding.image, R.color.black_color)
                }
                binding.rootLayout.setOnClickListener { onItemClickListener.get()?.onClick(position) }
            }
        }
    }
}
