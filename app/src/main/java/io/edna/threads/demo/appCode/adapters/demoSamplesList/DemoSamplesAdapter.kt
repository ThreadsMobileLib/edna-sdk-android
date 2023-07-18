package io.edna.threads.demo.appCode.adapters.demoSamplesList

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import io.edna.threads.demo.R
import io.edna.threads.demo.appCode.business.UiThemeProvider
import io.edna.threads.demo.appCode.business.ordinal
import io.edna.threads.demo.appCode.models.DemoSamplesListItem
import io.edna.threads.demo.appCode.models.DemoSamplesListItem.DIVIDER
import io.edna.threads.demo.appCode.models.DemoSamplesListItem.TEXT
import io.edna.threads.demo.appCode.models.DemoSamplesListItem.TITLE
import io.edna.threads.demo.databinding.HolderDemoSamplesTextBinding
import io.edna.threads.demo.databinding.HolderDemoSamplesTitleBinding
import io.edna.threads.demo.databinding.HolderHorizontalLineBinding
import org.koin.java.KoinJavaComponent.inject

class DemoSamplesAdapter(private val onItemClickListener: SampleListItemOnClick) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val list: MutableList<DemoSamplesListItem> = mutableListOf()
    private val uiThemeProvider: UiThemeProvider by inject(UiThemeProvider::class.java)
    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        context = parent.context
        return when (viewType) {
            DIVIDER.ordinal() -> {
                LineDividerHolder(HolderHorizontalLineBinding.inflate(inflater))
            }
            TITLE.ordinal() -> {
                TitleHolder(HolderDemoSamplesTitleBinding.inflate(inflater))
            }
            TEXT.ordinal() -> {
                TextHolder(HolderDemoSamplesTextBinding.inflate(inflater))
            }
            else -> throw IllegalStateException()
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as? DemoSamplesHolder)?.onBind(position)
    }

    override fun getItemCount() = list.count()

    override fun getItemViewType(position: Int) = list[position].ordinal()

    fun addItems(newItems: List<DemoSamplesListItem>) {
        notifyDatasetChangedWithDiffUtil(newItems)
    }

    private fun notifyDatasetChangedWithDiffUtil(newList: List<DemoSamplesListItem>) {
        val diffResult = DiffUtil.calculateDiff(DemoSamplesDiffCallback(list, newList))
        list.clear()
        list.addAll(newList)
        diffResult.dispatchUpdatesTo(this)
    }

    private inner class LineDividerHolder(binding: HolderHorizontalLineBinding) :
        RecyclerView.ViewHolder(binding.root), DemoSamplesHolder

    private inner class TitleHolder(val binding: HolderDemoSamplesTitleBinding) :
        RecyclerView.ViewHolder(binding.root), DemoSamplesHolder {

        override fun onBind(position: Int) {
            (list[position] as? TITLE)?.let { binding.titleTextView.text = it.text }
        }
    }

    private inner class TextHolder(val binding: HolderDemoSamplesTextBinding) :
        RecyclerView.ViewHolder(binding.root), DemoSamplesHolder {

        override fun onBind(position: Int) {
            (list[position] as? TEXT)?.let { item ->
                binding.textTextView.apply {
                    text = item.text
                    if (uiThemeProvider.isDarkThemeOn()) {
                        setTextColor(ContextCompat.getColor(context, R.color.white_color_fa))
                    } else {
                        setTextColor(ContextCompat.getColor(context, R.color.black_color))
                    }
                    setOnClickListener { onItemClickListener.onClick(item) }
                }
            }
        }
    }

    private interface DemoSamplesHolder {
        fun onBind(position: Int) {}
    }
}
