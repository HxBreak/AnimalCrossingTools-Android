package com.hxbreak.animalcrossingtools.view

import android.content.Context
import android.graphics.*
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.AttributeSet
import android.util.Log
import androidx.core.graphics.withTranslation
import androidx.core.text.PrecomputedTextCompat
import androidx.recyclerview.widget.RecyclerView
import timber.log.Timber

class IndexedRecyclerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {

    private val fontSize = FontUtils.sp2px(context, 14f)
    private val boxSize = ViewUtils.dp2px(context, 14f)

    private val textPaint: TextPaint = TextPaint()
        .apply {
            textSize = fontSize.toFloat()
            color = Color.WHITE
        }

    private val boxPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 1f
        color = Color.RED
    }

    private val map = ('A'..'Z').mapIndexed { index: Int, c: Char ->
        val layout = createStaticLayout(
            String.format("%c", c),
            textPaint,
            Layout.Alignment.ALIGN_NORMAL,
            true
        )
        c to layout
    }.toMap()

    private val widths = ('A'..'Z').mapIndexed { index: Int, c: Char ->
        val w = textPaint.measureText(String.format("%c", c))
        c to w
    }.toMap()

    private val blockHeight = map.values.sumBy { it.height }

    override fun onDrawForeground(canvas: Canvas?) {
        super.onDrawForeground(canvas)
        val yStart = (height - blockHeight) / 2
        canvas?.withTranslation(x = 0f, y = yStart.toFloat()) {
            map.forEach {
                drawRect(Rect(0, 0, boxSize, boxSize), boxPaint)
                it.value.draw(this)
//                Timber.e("HxBreak ${it.value.width}")
                translate(0f, boxSize.toFloat())
            }
        }
    }

    private fun createStaticLayout(
        source: CharSequence,
        paint: TextPaint,
        alignment: Layout.Alignment,
        includepad: Boolean
    ): StaticLayout {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            StaticLayout.Builder.obtain(source, 0, source.length, paint, width).apply {
                setAlignment(alignment)
                setIncludePad(includepad)
            }.build()
        } else {
            @Suppress("DEPRECATION")
            StaticLayout(source, paint, width, alignment, 0f, 0f, includepad)
        }
    }
}