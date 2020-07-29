package com.hxbreak.animalcrossingtools.view

import android.content.Context
import android.graphics.*
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ViewConfiguration
import androidx.core.graphics.withTranslation
import androidx.core.text.PrecomputedTextCompat
import androidx.core.view.ViewConfigurationCompat
import androidx.recyclerview.widget.RecyclerView
import timber.log.Timber
import kotlin.math.absoluteValue
import kotlin.math.max

class IndexedRecyclerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {

    private val mTouchSlop: Int

    init {
        mTouchSlop = ViewConfiguration.get(context).scaledTouchSlop
    }

    private val fontSize = FontUtils.sp2px(context, 14f)
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

    private val blockHeight = map.values.sumBy { it.height }

    private val boxWidth = map.values.map { it.wrapWidth() }.maxBy { it }

    override fun onDrawForeground(canvas: Canvas?) {
        super.onDrawForeground(canvas)
        val yStart = (height - blockHeight) / 2
        canvas?.withTranslation(x = 0f, y = yStart.toFloat()) {
            map.forEach {
                drawRect(Rect(1, 0, boxWidth!!, it.value.height), boxPaint)
                val bias = (boxWidth - it.value.wrapWidth()) / 2
                canvas.withTranslation(x = bias.toFloat()) {
                    it.value.draw(this)
                }
                translate(0f, it.value.height.toFloat())
            }
        }
    }

    override fun canScrollVertically(direction: Int): Boolean {
        return true;
    }

    private var startX: Float? = null
    private var startY: Float? = null


    //    private var isIntercept = false
//
//    override fun onInterceptTouchEvent(e: MotionEvent?): Boolean {
//        isIntercept = false
//        e?.let {
//            if (it.action == MotionEvent.ACTION_DOWN || it.action == MotionEvent.ACTION_MOVE){
//                val rect = Rect(0, 0, boxWidth!!, blockHeight).apply {
//                    offset(0, (height - blockHeight) / 2)
//                }
//                if(rect.contains(it.x.toInt(), it.y.toInt())){
//                    requestDisallowInterceptTouchEvent(true)
//                    isIntercept = true
//                    return true
//                }
//            }
//        }
//        return super.onInterceptTouchEvent(e)
//    }
    override fun performClick(): Boolean {
        return super.performClick()
    }

    var isDragging: Boolean = false

    override fun onTouchEvent(e: MotionEvent?): Boolean {
        when (e?.action) {
            MotionEvent.ACTION_DOWN -> {
                val rect = Rect(0, 0, boxWidth!!, blockHeight).apply {
                    offset(0, (height - blockHeight) / 2)
                }
                if (rect.contains(e.x.toInt(), e.y.toInt())) {
                    isDragging = true
                    return true
                }
            }
            MotionEvent.ACTION_MOVE -> {

            }
            MotionEvent.ACTION_UP -> {
                isDragging = false
            }
        }
        if (isDragging) {
            return true
        }
        return super.onTouchEvent(e)
    }

    //
//    override fun onTouchEvent(e: MotionEvent?): Boolean {
//        if (isIntercept){
//            return true
//        }
//        return super.onTouchEvent(e)
//    }

    private fun createStaticLayout(
        source: CharSequence,
        paint: TextPaint,
        alignment: Layout.Alignment,
        includepad: Boolean
    ): StaticLayout {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            StaticLayout.Builder.obtain(source, 0, source.length, paint, 1024).apply {
                setAlignment(alignment)
                setIncludePad(includepad)
            }.build()
        } else {
            @Suppress("DEPRECATION")
            StaticLayout(source, paint, width, alignment, 0f, 0f, includepad)
        }
    }

    fun StaticLayout.wrapWidth(): Int {
        var lastValue = 0
        for (i in 0 until this.lineCount) {
            lastValue = max(getLineWidth(i).toInt(), lastValue)
        }
        return lastValue
    }
}