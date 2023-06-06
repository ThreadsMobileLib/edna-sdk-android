package io.edna.threads.demo.appCode.adapters.userList

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import im.threads.ui.utils.ColorsHelper
import im.threads.ui.utils.gone
import im.threads.ui.utils.visible
import io.edna.threads.demo.R
import io.edna.threads.demo.appCode.business.UiThemeProvider
import io.edna.threads.demo.appCode.extensions.inflateWithBinding
import io.edna.threads.demo.appCode.models.UserInfo
import io.edna.threads.demo.databinding.UserListItemBinding
import org.koin.java.KoinJavaComponent.inject

class UserListAdapter(private val onItemClickListener: UserListItemOnClickListener) :
    RecyclerView.Adapter<UserListAdapter.UserItemHolder>() {

    private val uiThemeProvider: UiThemeProvider by inject(UiThemeProvider::class.java)
    private val list: MutableList<UserInfo> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserItemHolder {
        val inflater = LayoutInflater.from(parent.context)
        return UserItemHolder(inflater.inflateWithBinding(parent, R.layout.user_list_item))
    }

    fun showMenu(position: Int) {
        for (i in list.indices) {
            if (list[i].isShowMenu) {
                list[i].isShowMenu = false
                notifyItemChanged(i)
            }
        }
        list[position].isShowMenu = true
        notifyItemChanged(position)
    }

    fun closeMenu() {
        for (i in list.indices) {
            if (list[i].isShowMenu) {
                list[i].isShowMenu = false
                notifyItemChanged(i)
            }
        }
    }

    fun isMenuShown(): Boolean {
        for (i in list.indices) {
            if (list[i].isShowMenu) {
                return true
            }
        }
        return false
    }

    override fun onBindViewHolder(holder: UserItemHolder, position: Int) {
        holder.onBind(position)
    }

    override fun getItemCount() = list.count()

    fun addItems(newItems: List<UserInfo>) {
        notifyDatasetChangedWithDiffUtil(newItems)
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
                binding.name.text = item.nickName
                binding.userId.text = item.userId
                if (uiThemeProvider.isDarkThemeOn()) {
                    binding.name.setTextColor(ContextCompat.getColor(binding.root.context, R.color.white_color_fa))
                    binding.userId.setTextColor(ContextCompat.getColor(binding.root.context, R.color.white_color_fa))
                    ColorsHelper.setTint(binding.root.context, binding.image, R.color.white_color_fa)
                } else {
                    binding.name.setTextColor(ContextCompat.getColor(binding.root.context, R.color.black_color))
                    binding.userId.setTextColor(ContextCompat.getColor(binding.root.context, R.color.black_color))
                    ColorsHelper.setTint(binding.root.context, binding.image, R.color.black_color)
                }
                if (item.isShowMenu) {
                    binding.menuLayout.visible()
                    binding.itemLayout.gone()
                    binding.editButton.setOnClickListener { onItemClickListener.onEditItem(item) }
                    binding.deleteButton.setOnClickListener { onItemClickListener.onRemoveItem(item) }
                } else {
                    binding.menuLayout.gone()
                    binding.itemLayout.visible()
                    binding.rootLayout.setOnClickListener { onItemClickListener.onClick(item) }
                }
            }
        }
    }
}
