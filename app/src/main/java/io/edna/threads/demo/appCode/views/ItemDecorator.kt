package io.edna.threads.demo.appCode.views

import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.TextPaint
import android.util.TypedValue
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

data class ItemDecorator(
    var canvas: Canvas,
    var recyclerView: RecyclerView,
    var viewHolder: RecyclerView.ViewHolder,
    var dX: Float,
    var actionState: Int
) {
    @ColorInt
    private var bgColorFromStartToEnd = ContextCompat.getColor(recyclerView.context, android.R.color.transparent)

    @ColorInt
    private var bgColorFromEndToStart = ContextCompat.getColor(recyclerView.context, android.R.color.transparent)

    @ColorInt
    private var iconTintFromStartToEnd: Int = Color.DKGRAY

    @ColorInt
    private var iconTintFromEndToStart: Int = Color.DKGRAY

    @ColorInt
    private var textColorFromStartToEnd = Color.DKGRAY

    @ColorInt
    private var textColorFromEndToStart = Color.DKGRAY

    @DrawableRes
    private var iconResIdFromStartToEnd = 0

    @DrawableRes
    private var iconResIdFromEndToStart = 0

    private var textFromStartToEnd: String? = null
    private var textFromEndToStart: String? = null
    private var typefaceFromStartToEnd = Typeface.SANS_SERIF
    private var typefaceFromEndToStart = Typeface.SANS_SERIF

    private var textSizeFromStartToEnd = 14f
    private var textSizeFromEndToStart = 14f

    private var iconHorizontalMargin = 16f

    private var iconMarginUnit = TypedValue.COMPLEX_UNIT_DIP
    private var defaultTextUnit = TypedValue.COMPLEX_UNIT_SP

    private var calculatedHorizontalMargin = TypedValue.applyDimension(
        iconMarginUnit,
        iconHorizontalMargin,
        recyclerView.context.resources.displayMetrics
    )

    class Builder(
        canvas: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        actionState: Int
    ) {
        private val decorator = ItemDecorator(canvas, recyclerView, viewHolder, dX, actionState)

        fun set(
            @ColorInt backgroundColorFromStartToEnd: Int = decorator.bgColorFromStartToEnd,
            @ColorInt backgroundColorFromEndToStart: Int = decorator.bgColorFromEndToStart,
            @DrawableRes iconResIdFromStartToEnd: Int = decorator.iconResIdFromStartToEnd,
            @DrawableRes iconResIdFromEndToStart: Int = decorator.iconResIdFromEndToStart,
            @ColorInt iconTintColorFromStartToEnd: Int = decorator.iconTintFromStartToEnd,
            @ColorInt iconTintColorFromEndToStart: Int = decorator.iconTintFromEndToStart,
            textFromStartToEnd: String,
            textFromEndToStart: String,
            @ColorInt textColorFromStartToEnd: Int = decorator.textColorFromStartToEnd,
            @ColorInt textColorFromEndToStart: Int = decorator.textColorFromEndToStart,
            textSizeFromStartToEnd: Float = decorator.textSizeFromStartToEnd,
            textSizeFromEndToStart: Float = decorator.textSizeFromEndToStart,
            typeFaceFromStartToEnd: Typeface = decorator.typefaceFromStartToEnd,
            typeFaceFromEndToStart: Typeface = decorator.typefaceFromEndToStart,
            defaultTextUnit: Int = TypedValue.COMPLEX_UNIT_SP,
            defaultIconMarginUnit: Int = decorator.iconMarginUnit,
            iconHorizontalMargin: Float = decorator.iconHorizontalMargin
        ): Builder {
            decorator.bgColorFromStartToEnd = backgroundColorFromStartToEnd
            decorator.bgColorFromEndToStart = backgroundColorFromEndToStart
            decorator.iconResIdFromStartToEnd = iconResIdFromStartToEnd
            decorator.iconResIdFromEndToStart = iconResIdFromEndToStart
            decorator.iconTintFromStartToEnd = iconTintColorFromStartToEnd
            decorator.iconTintFromEndToStart = iconTintColorFromEndToStart
            decorator.textFromStartToEnd = textFromStartToEnd
            decorator.textFromEndToStart = textFromEndToStart
            decorator.defaultTextUnit = defaultTextUnit
            decorator.textSizeFromStartToEnd = textSizeFromStartToEnd
            decorator.textSizeFromEndToStart = textSizeFromEndToStart
            decorator.textColorFromStartToEnd = textColorFromStartToEnd
            decorator.textColorFromEndToStart = textColorFromEndToStart
            decorator.typefaceFromStartToEnd = typeFaceFromStartToEnd
            decorator.typefaceFromEndToStart = typeFaceFromEndToStart
            decorator.iconMarginUnit = defaultIconMarginUnit
            decorator.iconHorizontalMargin = iconHorizontalMargin
            create().decorate()
            return this
        }

        private fun create(): ItemDecorator = decorator
    }

    @Suppress("DEPRECATION")
    private fun Drawable.colorFilter(@ColorInt tintColor: Int) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            this.setColorFilter(tintColor, PorterDuff.Mode.MULTIPLY)
        } else {
            this.colorFilter = BlendModeColorFilter(tintColor, BlendMode.SRC_ATOP)
        }
    }

    fun decorate() {
        if (actionState != ItemTouchHelper.ACTION_STATE_SWIPE) return
        if (dX > 0) fromStartToEndBehavior() else fromEndToStartBehavior()
    }

    private fun fromStartToEndBehavior() {
        canvas.clipRect(
            viewHolder.itemView.left,
            viewHolder.itemView.top,
            viewHolder.itemView.left + dX.toInt(),
            viewHolder.itemView.bottom
        )
        // Draws a color drawable on the canvas, with the same size as the canvas
        if (bgColorFromStartToEnd != 0) {
            val cvBackgroundColor = ColorDrawable(bgColorFromStartToEnd)
            cvBackgroundColor.bounds = canvas.clipBounds
            cvBackgroundColor.draw(canvas)
        }
        // Draws the icon contextualizing the swipe action
        var iconSize = 0
        if (iconResIdFromStartToEnd != 0 && dX > calculatedHorizontalMargin) {
            val icon = ContextCompat.getDrawable(
                recyclerView.context,
                iconResIdFromStartToEnd
            )
            icon?.let {
                iconSize = it.intrinsicHeight
                val halfIcon = iconSize / 2
                val top =
                    viewHolder.itemView.top + ((viewHolder.itemView.bottom - viewHolder.itemView.top) / 2 - halfIcon)
                it.setBounds(
                    (viewHolder.itemView.left + calculatedHorizontalMargin).toInt(),
                    top,
                    (viewHolder.itemView.left + calculatedHorizontalMargin + it.intrinsicWidth).toInt(),
                    top + iconSize
                )
                it.colorFilter(iconTintFromStartToEnd)
                it.draw(canvas)
            }
        }
        // Draws the descriptive text contextualizing the swipe action
        textFromStartToEnd?.let {
            if (dX > calculatedHorizontalMargin + iconSize) {
                val textPaint = TextPaint()
                textPaint.isAntiAlias = true
                textPaint.textSize = TypedValue.applyDimension(
                    defaultTextUnit,
                    textSizeFromStartToEnd,
                    recyclerView.context.resources.displayMetrics
                )
                textPaint.color = textColorFromStartToEnd
                textPaint.typeface = typefaceFromStartToEnd
                val textTop =
                    (viewHolder.itemView.top + (viewHolder.itemView.bottom - viewHolder.itemView.top) / 2.0 + textPaint.textSize / 2)
                canvas.drawText(
                    it,
                    viewHolder.itemView.left + calculatedHorizontalMargin + iconSize + (if (iconSize > 0) calculatedHorizontalMargin / 2 else 0).toFloat(),
                    textTop.toFloat(),
                    textPaint
                )
            }
        }
    }

    /**
     * if [dX] < 0, that means that the user is swiping from the end of the
     * screen to the start of the screen (right to left).
     *
     * The [canvas] takes the size of the [viewHolder].
     * */
    private fun fromEndToStartBehavior() {
        canvas.clipRect(
            viewHolder.itemView.right + dX.toInt(),
            viewHolder.itemView.top,
            viewHolder.itemView.right,
            viewHolder.itemView.bottom
        )
        // Draws a color drawable on the canvas, with the same size as the canvas
        if (bgColorFromEndToStart != 0) {
            val cvBackgroundColor = ColorDrawable(bgColorFromEndToStart)
            cvBackgroundColor.bounds = canvas.clipBounds
            cvBackgroundColor.draw(canvas)
        }
        // Draws the icon contextualizing the swipe action
        var imgEnd = viewHolder.itemView.right
        var iconSize = 0
        if (iconResIdFromEndToStart != 0 && dX < -calculatedHorizontalMargin) {
            val icon = ContextCompat.getDrawable(
                recyclerView.context,
                iconResIdFromEndToStart
            )
            icon?.let {
                iconSize = it.intrinsicHeight
                val halfIcon = iconSize / 2
                val top =
                    viewHolder.itemView.top + ((viewHolder.itemView.bottom - viewHolder.itemView.top) / 2 - halfIcon)
                imgEnd =
                    (viewHolder.itemView.right - calculatedHorizontalMargin - halfIcon * 2).toInt()
                it.setBounds(
                    imgEnd,
                    top,
                    (viewHolder.itemView.right - calculatedHorizontalMargin).toInt(),
                    top + it.intrinsicHeight
                )
                it.colorFilter(iconTintFromEndToStart)
                it.draw(canvas)
            }
        }
        // Draws the descriptive text contextualizing the swipe action
        textFromEndToStart?.let {
            if (dX < -calculatedHorizontalMargin - iconSize) {
                val textPaint = TextPaint()
                textPaint.isAntiAlias = true
                textPaint.textSize = TypedValue.applyDimension(
                    defaultTextUnit,
                    textSizeFromEndToStart,
                    recyclerView.context.resources.displayMetrics
                )
                textPaint.color = textColorFromEndToStart
                textPaint.typeface = typefaceFromEndToStart
                val width = textPaint.measureText(it)
                val textTop =
                    (viewHolder.itemView.top + (viewHolder.itemView.bottom - viewHolder.itemView.top) / 2.0 + textPaint.textSize / 2.0)

                canvas.drawText(
                    it,
                    imgEnd - width - if (imgEnd == viewHolder.itemView.right) calculatedHorizontalMargin else calculatedHorizontalMargin / 2,
                    textTop.toFloat(),
                    textPaint
                )
            }
        }
    }
}
