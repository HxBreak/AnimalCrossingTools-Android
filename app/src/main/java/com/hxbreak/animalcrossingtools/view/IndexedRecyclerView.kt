package com.hxbreak.animalcrossingtools.view

import android.animation.ObjectAnimator
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
import androidx.core.animation.doOnEnd
import androidx.core.graphics.withTranslation
import androidx.dynamicanimation.animation.FloatValueHolder
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.hxbreak.animalcrossingtools.R
import com.hxbreak.animalcrossingtools.utils.FontUtils
import com.hxbreak.animalcrossingtools.utils.ViewUtils
import com.hxbreak.animalcrossingtools.view.canvas.FastScrollPopup
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
    private val heightLightList: List<Pair<Char, StaticLayout>>
    private val blockHeight: Int
    private val boxWidth: Int

    private val indicator: FastScrollPopup

    private val heightLightTextPaint: TextPaint

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
        textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
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

        heightLightTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = alphabetTextSize.toFloat()
            color = indicatorBgColor
        }
        heightLightList = list.map {
            val layout = createStaticLayout(
                String.format("%c", it),
                heightLightTextPaint,
                Layout.Alignment.ALIGN_NORMAL,
                true
            )
            it to layout
        }
    }

    private val boxPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 1f
        color = Color.RED
    }


    private fun indexStart() = (height - blockHeight) / 2

    var mEnableAlphabet = true
        set(value) {
        field = value
        invalidate()
    }

    override fun draw(c: Canvas?) {
        super.draw(c)
        if (mEnableAlphabet){
            val yStart = indexStart()
            indicator.draw(c)

            c?.withTranslation(x = (width - boxWidth).toFloat(), y = yStart.toFloat()) {
                pairs.forEachIndexed { index, pair ->
//                drawRect(Rect(1, 0, boxWidth, pair.second.height), boxPaint)
                    val bias = (boxWidth - pair.second.wrapWidth()) / 2
                    c.withTranslation(x = bias.toFloat()) {
                        (if (index == currentSelectedIndex) heightLightList[index] else pair).second.draw(
                            this
                        )
                    }
                    translate(0f, pair.second.height.toFloat())
                }
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
        if (e?.action == MotionEvent.ACTION_DOWN && mEnableAlphabet) {
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

    private var lastY = 0f

    /**
     * 指示器进入动画
     */
    var animation: SpringAnimation? = null

    /**
     * 隐藏显示动画
     */
    var visibleAnimation: ObjectAnimator? = null

    private fun onChangeIndex() {
        if (currentSelectedIndex != null) {
            visibleAnimation?.cancel()
            visibleAnimation = ObjectAnimator.ofFloat(indicator, "alpha", 1f).apply {
                duration = 200
                doOnEnd {
                    visibleAnimation = null
                }
            }
            visibleAnimation?.start()
            val list = pairs.take(currentSelectedIndex!! + 1)
            val half = list.last().second.height / 2
            val bestStart = list.sumBy { it.second.height } - half + indexStart()
            indicator.setSectionName("${list[currentSelectedIndex!!].first}")

            animation?.cancel()
            animation = SpringAnimation(FloatValueHolder(lastY))
                .addUpdateListener { animation, value, velocity ->
                    lastY = value
                    indicator.updateFastScrollerBounds(this, value.toInt())
                    invalidate()
                }
                .addEndListener { anim, canceled, value, velocity ->
                    animation = null
                    if (visibleAnimation?.isStarted == false) {
                        visibleAnimation?.start()
                    }
                }
                .apply {
                    animateToFinalPosition(bestStart.toFloat())
                }
        } else {
            visibleAnimation?.cancel()
            visibleAnimation = ObjectAnimator.ofFloat(indicator, "alpha", 0f).apply {
                duration = 150
                doOnEnd {
                    visibleAnimation = null
                }
            }
            if (animation == null) {
                visibleAnimation!!.start()
            }
        }
        if (currentSelectedIndex != null) {
            val adapter = adapter
            if (adapter is IndexableAdpater) {
                val index = adapter.findFirstChildIndex(list[currentSelectedIndex!!].toString())
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
        if (mEnableAlphabet){
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

    interface IndexableAdpater {
        fun findFirstChildIndex(s: String): Int
    }
}