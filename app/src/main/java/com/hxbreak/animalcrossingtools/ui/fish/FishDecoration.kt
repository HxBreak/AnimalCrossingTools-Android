package com.hxbreak.animalcrossingtools.ui.fish

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.graphics.Rect
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.view.View
import androidx.core.graphics.withTranslation
import androidx.core.view.isEmpty
import androidx.recyclerview.widget.RecyclerView
import com.hxbreak.animalcrossingtools.character.CharUtil
import com.hxbreak.animalcrossingtools.data.source.entity.FishEntity
import com.hxbreak.animalcrossingtools.utils.ViewUtils

class FishDecoration(
    val context: Context,
    val fish: List<FishEntity>
) : RecyclerView.ItemDecoration() {

    val width = ViewUtils.dp2px(context, 24f)

    val mTextSize = ViewUtils.dp2px(context, 18f)

    val paint = TextPaint(ANTI_ALIAS_FLAG).apply {
        color = Color.RED
        textSize = mTextSize.toFloat()
    }

    val nameSlot = fish.mapIndexed { index: Int, fishEntity: FishEntity ->
        index to CharUtil.toCategory(CharUtil.headPinyin(fishEntity.name.nameCNzh))
    }.distinctBy { it.second }
        .map {
            it.first to createHeader(it.second)
        }
        .toMap()


    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(c, parent, state)
        if (nameSlot.isEmpty() || parent.isEmpty()) return
        val parentPadding = parent.paddingTop

        var earliestPosition = Int.MAX_VALUE
        var previousHeaderPosition = -1
        var previousHasHeader = false
        var earliestChild: View? = null
        for (i in parent.childCount - 1 downTo 0) {
            val child = parent.getChildAt(i) ?: continue
            if (child.y > parent.height || (child.y + child.height) < 0) continue
            val position = parent.getChildAdapterPosition(child)
            if (position < 0) {
                continue
            }
            if (position < earliestPosition) {
                earliestChild = child
                earliestPosition = position
            }
            val header = nameSlot[position]
            if (header != null) {
                drawHeader(c, child, parentPadding, header, child.alpha, previousHasHeader)
                previousHeaderPosition = position
                previousHasHeader = true
            } else {
                previousHasHeader = false
            }
        }
        if (earliestChild != null && earliestPosition != previousHeaderPosition) {
            findHeaderBeforePosition(earliestPosition)?.let { stickyHeader ->
                previousHasHeader = previousHeaderPosition - earliestPosition == 1
                drawHeader(c, earliestChild, parentPadding, stickyHeader, 1f, previousHasHeader)
            }
        }
    }


    private fun findHeaderBeforePosition(position: Int): StaticLayout? {
        for (headerPos in nameSlot.keys.reversed()) {
            if (headerPos < position) {
                return nameSlot[headerPos]
            }
        }
        return null
    }

    private fun drawHeader(
        c: Canvas,
        child: View,
        parentPadding: Int,
        header: StaticLayout,
        alpha: Float,
        previousHasHeader: Boolean
    ) {
        val childTop = child.y.toInt()
        val childBottom = childTop + child.height
        var top = (childTop).coerceAtLeast(parentPadding)
        if (previousHasHeader) {
            top = top.coerceAtMost(childBottom - header.height)
        }
        c.withTranslation(y = top.toFloat()) {
            header.draw(c)
        }
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
    }

    private fun createHeader(s: String): StaticLayout {
        return newStaticLayout(s, paint, width, Layout.Alignment.ALIGN_CENTER, 1f, 0f, false)
    }
}

fun newStaticLayout(
    source: CharSequence,
    paint: TextPaint,
    width: Int,
    alignment: Layout.Alignment,
    spacingmult: Float,
    spacingadd: Float,
    includepad: Boolean
): StaticLayout {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        StaticLayout.Builder.obtain(source, 0, source.length, paint, width).apply {
            setAlignment(alignment)
            setLineSpacing(spacingadd, spacingmult)
            setIncludePad(includepad)
        }.build()
    } else {
        @Suppress("DEPRECATION")
        StaticLayout(source, paint, width, alignment, spacingmult, spacingadd, includepad)
    }
}
