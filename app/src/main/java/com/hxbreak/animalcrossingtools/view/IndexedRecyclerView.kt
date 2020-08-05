package com.hxbreak.animalcrossingtools.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
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
import com.hxbreak.animalcrossingtools.R
import com.hxbreak.animalcrossingtools.ui.fish.FishAdapter
import com.hxbreak.animalcrossingtools.view.canvas.FastScrollPopup
import timber.log.Timber
import java.lang.IllegalStateException
import kotlin.math.max

class IndexedRecyclerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {

    private val mTouchSlop: Int = ViewConfiguration.get(context).scaledTouchSlop
    private var currentSelectedIndex: Int? = null
    private var currentYPosition: Int? = null

    private val fontSize: Int
    private val textPaint: TextPaint
    private val list = ('A'..'Z').toList()

    private val pairs: List<Pair<Char, StaticLayout>>
    private val blockHeight: Int
    private val boxWidth: Int

    private val indicator: FastScrollPopup

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
        val typedValue =
            context.obtainStyledAttributes(attrs, R.styleable.IndexedRecyclerView, defStyleAttr, 0)

        val alphabetTextColor =
            typedValue.getColor(R.styleable.IndexedRecyclerView_alphabetTextColor, Color.BLACK)
        val alphabetTextSize = typedValue.getDimensionPixelSize(
            R.styleable.IndexedRecyclerView_alphabetTextSize,
            FontUtils.sp2px(context, 14f)
        )
        val indicatorTextColor =
            typedValue.getColor(R.styleable.IndexedRecyclerView_indicatorTextColor, Color.BLACK)
        val indicatorBgColor =
            typedValue.getColor(R.styleable.IndexedRecyclerView_indicatorBgColor, Color.WHITE)

        typedValue.recycle()
        fontSize = alphabetTextColor
        textPaint = TextPaint().apply {
            textSize = alphabetTextSize.toFloat()
            color = alphabetTextColor
        }

        pairs = list.map { c: Char ->
            val layout = createStaticLayout(
                String.format("%c", c),
                textPaint,
                Layout.Alignment.ALIGN_NORMAL,
                true
            )
            c to layout
        }
        blockHeight = pairs.sumBy { it.second.height }
        boxWidth = pairs.map { it.second.wrapWidth() }.maxBy { it }!! + ViewUtils.dp2px(context, 8f)
        indicator = FastScrollPopup(context.resources, this).apply {
            setBgColor(indicatorBgColor)
            setTextColor(indicatorTextColor)
        }
    }

    private val boxPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 1f
        color = Color.RED
    }


    private fun indexStart() = (height - blockHeight) / 2

    override fun onDrawForeground(canvas: Canvas?) {
        super.onDrawForeground(canvas)
        val yStart = indexStart()
        indicator.draw(canvas)

        canvas?.withTranslation(x = (width - boxWidth).toFloat(), y = yStart.toFloat()) {
            pairs.forEach {
//                drawRect(Rect(1, 0, boxWidth, it.second.height), boxPaint)
                val bias = (boxWidth - it.second.wrapWidth()) / 2
                canvas.withTranslation(x = bias.toFloat()) {
                    it.second.draw(this)
                }
                translate(0f, it.second.height.toFloat())
            }
        }
    }

    override fun canScrollVertically(direction: Int): Boolean {
        if (isDragging) return true
        return super.canScrollVertically(direction);
    }

    private var isDragging: Boolean = false

    override fun onInterceptTouchEvent(e: MotionEvent?): Boolean {
        val result = super.onInterceptTouchEvent(e);
        if (e?.action == MotionEvent.ACTION_DOWN) {
            val rect = getAlphabetRect()
            if (rect.contains(e.x.toInt(), e.y.toInt())) {
                isDragging = true
                calcCurrentSelect(e)
                return true
            }
        }
        return result
    }

    fun getScrollBarThumbHeight() = ViewUtils.dp2px(context, 32f)

    fun getScrollBarWidth() = ViewUtils.dp2px(context, 16f)

    private fun calcCurrentSelect(e: MotionEvent) {
        var current = (height - blockHeight) / 2
        val first = pairs.firstOrNull {
            val after = current + it.second.height
            val result = e.y.toInt() in current..after
            current = after
            result
        }
        val index = if (first == null) null else pairs.indexOf(first)
        currentYPosition = if (index != null) e.y.toInt() else null
        if (index != currentSelectedIndex) {
            currentSelectedIndex = index
            onChangeIndex()
        }
    }

    private fun onChangeIndex() {
        if (currentSelectedIndex != null) {
            val list = pairs.take(currentSelectedIndex!! + 1)
            val half = list.last().second.height / 2
            val bestStart = list.sumBy { it.second.height } - half + indexStart()
            indicator.setSectionName("${list[currentSelectedIndex!!].first}")
            indicator.updateFastScrollerBounds(this, bestStart)
        } else {
            indicator.setSectionName("")
        }
        if (currentSelectedIndex != null) {
            val adapter = adapter
            if (adapter is FishAdapter) {
                val index = adapter.findFirstChildIndex(list[currentSelectedIndex!!])
                if (index != -1) {
                    smoothScrollToPosition(index)
                }
            }
        }
        invalidate()
    }

    private fun getAlphabetRect() = Rect(width - boxWidth, 0, width, blockHeight).apply {
        offset(0, (height - blockHeight) / 2)
    }

    override fun onTouchEvent(e: MotionEvent?): Boolean {
        when (e?.action) {
            MotionEvent.ACTION_DOWN -> {
                val rect = getAlphabetRect()
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
                    onChangeIndex()
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

    private fun StaticLayout.wrapWidth(): Int {
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