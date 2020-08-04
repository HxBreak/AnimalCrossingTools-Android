package com.hxbreak.animalcrossingtools.view

import android.content.Context
import android.graphics.*
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewConfiguration
import androidx.core.graphics.withTranslation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.hxbreak.animalcrossingtools.ui.fish.FishAdapter
import com.hxbreak.animalcrossingtools.view.canvas.FastScrollPopup
import kotlin.math.max

class IndexedRecyclerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {

    private val mTouchSlop: Int = ViewConfiguration.get(context).scaledTouchSlop
    private var currentSelectedIndex: Int? = null
    private var currentYPosition: Int? = null

    private val fontSize = FontUtils.sp2px(context, 14f)
    private val textPaint: TextPaint = TextPaint()
        .apply {
            textSize = fontSize.toFloat()
            color = Color.WHITE
        }

    private val selectedTextPaint = TextPaint()
        .apply {
            textSize = fontSize.toFloat()
            color = Color.RED
        }

    private val boxPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 1f
        color = Color.RED
    }

    private val list = ('A'..'Z').toList()

    private val map = list.mapIndexed { index: Int, c: Char ->
        val layout = createStaticLayout(
            String.format("%c", c),
            textPaint,
            Layout.Alignment.ALIGN_NORMAL,
            true
        )
        c to layout
    }.toMap()

    private val blockHeight = map.values.sumBy { it.height }

    private val boxWidth =
        map.values.map { it.wrapWidth() }.maxBy { it }!! + ViewUtils.dp2px(context, 8f)

    private val indicator = FastScrollPopup(context.resources, this)

    init {
        layoutManager = object : LinearLayoutManager(context, RecyclerView.VERTICAL, false) {
            override fun startSmoothScroll(smoothScroller: SmoothScroller?) {
                smoothScroller?.let {
                    val scroller = AlwaysAtStartScroller(context)
                    scroller.targetPosition = smoothScroller.targetPosition
                    super.startSmoothScroll(scroller)
                }
            }
        }
    }

    override fun onDrawForeground(canvas: Canvas?) {
        super.onDrawForeground(canvas)
        val yStart = (height - blockHeight) / 2
        canvas?.withTranslation(x = 0f, y = yStart.toFloat()) {
            map.forEach {
                drawRect(Rect(1, 0, boxWidth, it.value.height), boxPaint)
                val bias = (boxWidth - it.value.wrapWidth()) / 2
                canvas.withTranslation(x = bias.toFloat()) {
                    it.value.draw(this)
                }
                translate(0f, it.value.height.toFloat())
            }
        }
        canvas?.drawText("$currentSelectedIndex $currentYPosition", 100f, 100f, textPaint)
    }

    override fun canScrollVertically(direction: Int): Boolean {
        if (isDragging) return true
        return super.canScrollVertically(direction);
    }

    private var isDragging: Boolean = false

    override fun onInterceptTouchEvent(e: MotionEvent?): Boolean {
        val result = super.onInterceptTouchEvent(e);
        if (e?.action == MotionEvent.ACTION_DOWN) {
            val rect = Rect(0, 0, boxWidth!!, blockHeight).apply {
                offset(0, (height - blockHeight) / 2)
            }
            if (rect.contains(e.x.toInt(), e.y.toInt())) {
                isDragging = true
                calcCurrentSelect(e)
                return true
            }
        }
        return result
    }

    fun getScrollBarThumbHeight() = ViewUtils.dp2px(context, 32f)

    fun getScrollBarWidth() = ViewUtils.dp2px(context, 32f)

    private fun calcCurrentSelect(e: MotionEvent) {
        var current = (height - blockHeight) / 2
        val first = map.values.firstOrNull {
            val after = current + it.height
            val result = e.y.toInt() in current..after
            current = after
            result
        }
        val index = if (first == null) null else map.values.indexOf(first)
        currentYPosition = if (index != null) e.y.toInt() else null
        if (index != currentSelectedIndex) {
            currentSelectedIndex = index
            onChangeIndex()
        }
    }

    private fun onChangeIndex() {
        postInvalidate()
        if (currentSelectedIndex != null) {
            val adapter = adapter
            if (adapter is FishAdapter) {
                val index = adapter.findFirstChildIndex(list[currentSelectedIndex!!])
                if (index != -1) {
                    smoothScrollToPosition(index)
                }
            }
        }
    }

    override fun onTouchEvent(e: MotionEvent?): Boolean {
        when (e?.action) {
            MotionEvent.ACTION_DOWN -> {
                val rect = Rect(0, 0, boxWidth!!, blockHeight).apply {
                    offset(0, (height - blockHeight) / 2)
                }
                if (rect.contains(e.x.toInt(), e.y.toInt())) {
                    isDragging = true
                    calcCurrentSelect(e)
                    return true
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (isDragging) calcCurrentSelect(e)
            }
            MotionEvent.ACTION_UP -> {
                if (isDragging) {
                    currentSelectedIndex = null
                    isDragging = false
                    postInvalidate()
                    return true
                }
            }
        }
        if (isDragging) {
            return true
        }
        return super.onTouchEvent(e)
    }

    val observer = object : RecyclerView.AdapterDataObserver() {

    }

    /**
     * register listener before setAdapter
     */
    override fun setAdapter(adapter: Adapter<*>?) {
        val old = getAdapter()
        old?.let {
            old.unregisterAdapterDataObserver(observer)
        }
        adapter?.registerAdapterDataObserver(observer)
        super.setAdapter(adapter)
    }

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

    internal inner class AlwaysAtStartScroller(val context: Context) :
        LinearSmoothScroller(context) {
        override fun getVerticalSnapPreference(): Int {
            return SNAP_TO_START
        }
    }
}