package io.edna.threads.demo.appCode.adapters

import android.content.Context
import android.graphics.Canvas
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import io.edna.threads.demo.R
import io.edna.threads.demo.appCode.extensions.isDarkThemeOn
import io.edna.threads.demo.appCode.views.ItemDecorator

class EccTouchHelperCallBack(
    private val context: Context,
    private val listener: ListItemClickListener,
    dragDirs: Int,
    swipeDirs: Int
) : ItemTouchHelper.SimpleCallback(dragDirs, swipeDirs) {

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean = false

    override fun onChildDraw(
        canvas: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        val defaultWhiteColor = if (context.isDarkThemeOn()) {
            ContextCompat.getColor(context, R.color.white_color_fa)
        } else {
            ContextCompat.getColor(context, R.color.black_color)
        }

        val teal200 = ContextCompat.getColor(context, R.color.blue_color)
        val colorAlert = ContextCompat.getColor(context, R.color.red_color)

        ItemDecorator.Builder(canvas, recyclerView, viewHolder, dX, actionState).set(
            backgroundColorFromStartToEnd = colorAlert,
            backgroundColorFromEndToStart = teal200,
            textFromStartToEnd = context.getString(R.string.remove),
            textFromEndToStart = context.getString(R.string.edit),
            textColorFromStartToEnd = defaultWhiteColor,
            textColorFromEndToStart = defaultWhiteColor,
            iconTintColorFromStartToEnd = defaultWhiteColor,
            iconTintColorFromEndToStart = defaultWhiteColor,
            iconResIdFromStartToEnd = R.drawable.ic_remove,
            iconResIdFromEndToStart = R.drawable.ic_edit
        )

        super.onChildDraw(
            canvas,
            recyclerView,
            viewHolder,
            dX,
            dY,
            actionState,
            isCurrentlyActive
        )
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.adapterPosition
        when (direction) {
            ItemTouchHelper.LEFT -> {
                listener.onEditItem(position)
            }
            ItemTouchHelper.RIGHT -> {
                listener.onRemoveItem(position)
            }
        }
    }
}
