package io.edna.threads.demo.appCode.adapters.demoSamplesList

import androidx.recyclerview.widget.DiffUtil
import io.edna.threads.demo.appCode.models.DemoSamplesListItem

class DemoSamplesDiffCallback(
    private val oldList: List<DemoSamplesListItem>,
    private val newList: List<DemoSamplesListItem>
) : DiffUtil.Callback() {

    override fun getOldListSize() = oldList.size

    override fun getNewListSize() = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldList[oldItemPosition]
        val newItem = newList[newItemPosition]

        if (oldItem is DemoSamplesListItem.DIVIDER && newItem is DemoSamplesListItem.DIVIDER) {
            return true
        }

        return oldItem.toString() == newItem.toString()
    }
}
