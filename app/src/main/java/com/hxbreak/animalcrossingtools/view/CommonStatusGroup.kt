package com.hxbreak.animalcrossingtools.view

import android.content.Context
import android.util.AttributeSet
import android.view.*
import android.widget.Button
import android.widget.Scroller
import android.widget.TextView
import androidx.annotation.IntDef
import androidx.core.content.res.getResourceIdOrThrow
import androidx.core.view.*
import androidx.navigation.findNavController
import com.hxbreak.animalcrossingtools.R
import timber.log.Timber
import kotlin.experimental.and
import kotlin.math.abs

class CommonStatusGroup @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr), NestedScrollingParent3, NestedScrollingChild3 {

    companion object {
        const val STATE_HIDE = 0
        const val STATE_SHOW_EMPTY = 1
        const val STATE_SHOW_ERROR = 2
    }

    @IntDef(value = [STATE_HIDE, STATE_SHOW_EMPTY, STATE_SHOW_ERROR])
    @Retention(AnnotationRetention.SOURCE)
    annotation class State{}

    private val mScrollParentHelper = NestedScrollingParentHelper(this)
    private val mScrollChildHelper = NestedScrollingChildHelper(this)
    private val mScroller = Scroller(context)
    private val errorViewScroller = Scroller(context)
    private var errorLayoutValue: Int = -1
    private val emptyView: View
    private var mAxes = -1
    private val errorView = lazy {
        if (errorLayoutValue < 0) error("errorLayout value is not set or incorrect.")
        Timber.e("Create Error View")
        val view = LayoutInflater.from(context).inflate(errorLayoutValue, this, false)
        addView(view)
        requestLayout()
        return@lazy view
    }

    private val mTouchSlop = ViewConfiguration.get(context).scaledTouchSlop

    private fun runIfErrorViewInitialized(block: View.() -> Unit){
        if (errorView.isInitialized()) errorView.value.block()
    }

    lateinit var content: View
    @State
    private var  mState: Int = STATE_HIDE

    init {
        mScrollChildHelper.isNestedScrollingEnabled = true
        val typedValue = context.obtainStyledAttributes(attrs, R.styleable.CommonStatusGroup, defStyleAttr, 0)
        errorLayoutValue = typedValue.getResourceIdOrThrow(R.styleable.CommonStatusGroup_errorLayout)
        val emptyLayoutValue = typedValue.getResourceIdOrThrow(R.styleable.CommonStatusGroup_emptyLayout)
        emptyView = LayoutInflater.from(context).inflate(emptyLayoutValue, this, false)
        typedValue.recycle()
    }

    fun setEmpty(){
        setState(STATE_SHOW_EMPTY)
    }

    fun clearState(){
        setState(STATE_HIDE)
    }

    fun setException(exception: Throwable, retry: Runnable){
        /**
         * BindErrorInfo To UI
         */
        errorView.value.findViewById<TextView>(R.id.error_information)?.let {
            it.text = "出错了"
        }
        errorView.value.findViewById<Button>(R.id.retry_area)?.let {
            it.setOnClickListener{ retry.run() }
        }
        setState(STATE_SHOW_ERROR)
    }
    private var fromState: Int = 0

    private fun setState(@State newState: Int){
        if (!isLaidOut) return

        if (mState != newState){
            when(mState){
                STATE_SHOW_ERROR, STATE_SHOW_EMPTY -> {
                    if (!errorViewScroller.isFinished) errorViewScroller.forceFinished(true)
                    errorViewScroller.startScroll(errorViewScroller.finalX, 0, -errorViewScroller.finalX, 0)
                    invalidate()
                }
            }
            when(newState){
                STATE_SHOW_ERROR, STATE_SHOW_EMPTY -> {
                    if (!errorViewScroller.isFinished) errorViewScroller.forceFinished(true)
                    errorViewScroller.startScroll(0, 0, width, 0)
                    invalidate()
                }
            }
            fromState = mState
            mState = newState
        }
    }

    override fun generateDefaultLayoutParams(): ViewGroup.LayoutParams {
        return LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun checkLayoutParams(p: ViewGroup.LayoutParams?): Boolean {
        return super.checkLayoutParams(p) && p is LayoutParams
    }

    override fun generateLayoutParams(attrs: AttributeSet?): ViewGroup.LayoutParams {
        return LayoutParams(context, attrs)
    }

    override fun generateLayoutParams(p: ViewGroup.LayoutParams?): ViewGroup.LayoutParams {
        return LayoutParams(p)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        content.measure(widthMeasureSpec, heightMeasureSpec)
        emptyView.measure(MeasureSpec.makeMeasureSpec(measuredWidth, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(measuredHeight, MeasureSpec.EXACTLY))
        runIfErrorViewInitialized {
            measure(MeasureSpec.makeMeasureSpec(this@CommonStatusGroup.measuredWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(this@CommonStatusGroup.measuredHeight, MeasureSpec.EXACTLY))
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        updateLayout()
    }

    class LayoutParams : MarginLayoutParams {

        constructor(c: Context?, attrs: AttributeSet?) : super(c, attrs)

        constructor(width: Int, height: Int) : super(width, height)

        constructor(source: ViewGroup.LayoutParams?) : super(source) {}
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        if (childCount != 1) error("childCount Must Be 1")
        content = get(0)
        addView(emptyView)
    }

    override fun canScrollHorizontally(direction: Int): Boolean {
        return direction < 0
    }

    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
        if ((mAxes and ViewCompat.SCROLL_AXIS_HORIZONTAL) == ViewCompat.SCROLL_AXIS_HORIZONTAL){
            dispatchNestedPreScroll(dx, dy, consumed, null, type)
        }else{
            dispatchNestedPreScroll(dx, dy, consumed, null, type)
        }
//        if (dy > 0){//Scroll Down
//            val scrollView = content
//            if (scrollView is RecyclerView){
//                val currentBottom = scrollView.computeVerticalScrollOffset() + scrollView.computeVerticalScrollExtent()
//
//                /**
//                 * 滚动组件剩余可滚动距离
//                 */
//                val currentLeft = scrollView.computeVerticalScrollRange() - currentBottom
//                if (currentLeft >= 0 || mScroller.finalY >= emptyView.measuredHeight){
//                    if (dy > currentLeft){
//                        //scrollSome
//                        val hideAreaWantScrollY = dy - currentLeft
//                        val hideAreaMaxScrollY = min(emptyView.measuredHeight - mScroller.finalY, hideAreaWantScrollY)
//                        val parentScrollY = dy - hideAreaMaxScrollY
//                        val parentConsumed = intArrayOf(0, 0)
//                        dispatchNestedPreScroll(dx, parentScrollY - 1, parentConsumed, null, type)
//                        mScroller.finalY += hideAreaMaxScrollY
//                        consumed[1] = hideAreaMaxScrollY + 1
//                        updateLayout()
//                        Timber.e("sp1 currentLeft: $currentLeft childScrollY: $parentScrollY, Scroller: ${mScroller.finalY}")
//                        Timber.e("sp1 $dy $currentLeft $hideAreaWantScrollY $hideAreaMaxScrollY $parentScrollY ${consumed[1]}")
//                    }else{
//                        Timber.e("sp2")
//                        dispatchNestedPreScroll(dx, dy, consumed, null, type)
//                    }
//                }else{
//                    Timber.e("sp4")
//                    mScroller.finalY += dy
//                    dispatchNestedPreScroll(dx, 0, consumed, null, type)
//                    consumed[1] = dy
//                    updateLayout()
//                }
//            }else{
//                if (!content.canScrollVertically(1) && mScroller.finalY < emptyView.measuredHeight){
//                    mScroller.finalY += dy
//                    dispatchNestedPreScroll(dx, dy, consumed, null, type)
//                    consumed[1] = dy
//                    updateLayout()
//                }else{
//                    dispatchNestedPreScroll(dx, dy, consumed, null, type)
//                }
//            }
//        } else {
//            if (mScroller.finalY > 0){
//                val willConsume = min(mScroller.finalY, abs(dy))
//                mScroller.finalY -= willConsume
//                dispatchNestedPreScroll(dx, dy + willConsume, consumed, null, type)
//                consumed[1] = -willConsume
//                updateLayout()
//            }else{
//                dispatchNestedPreScroll(dx, dy, consumed, null, type)
//            }
//        }
    }

    var moveX = 0f
    var moveY = 0f
    var lastX = 0f
    var lastY = 0f

    var isGestureProcessingMode = false

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        when(ev?.action){
            MotionEvent.ACTION_DOWN -> {
                isGestureProcessingMode = false
                moveX = 0f
                moveY = 0f
                lastX = ev.rawX
                lastY = ev.rawY
            }
            MotionEvent.ACTION_UP -> {

            }
            MotionEvent.ACTION_MOVE -> {
                moveX += ev.rawX - lastX
                moveY += ev.rawY - lastY
                if (abs(moveX) > mTouchSlop && abs(moveX) > abs(moveY)){
                    if (!isGestureProcessingMode){
                        isGestureProcessingMode = true
                        val upEvent = MotionEvent.obtain(ev)
                        upEvent.action = MotionEvent.ACTION_CANCEL
                        super.dispatchTouchEvent(upEvent)
                        upEvent.recycle()
                        requestDisallowInterceptTouchEvent(true)
                    }
                }
                lastX = ev.rawX
                lastY = ev.rawY
            }
        }
        return if (!isGestureProcessingMode) super.dispatchTouchEvent(ev) else true
    }

    private fun updateLayout(){
        val contentBottom = height - mScroller.finalY
        val contentTop = contentBottom - height
        content.layout(0, contentTop, width, contentBottom)
        var layoutEmptyViews = false
        when(mState){
            STATE_HIDE -> {
                content.isGone = false
                if (errorViewScroller.isFinished){
                    runIfErrorViewInitialized { isGone = true }
                    emptyView.isGone = true
                }else{
                    layoutEmptyViews = true
                }
            }
            STATE_SHOW_EMPTY, STATE_SHOW_ERROR -> {
                layoutEmptyViews = true
                if (errorViewScroller.isFinished){
                    content.isGone = true
                }
            }
        }
        if (layoutEmptyViews){
            val r = width
            val l = width - (if (errorViewScroller.isFinished) errorViewScroller.finalX else errorViewScroller.currX)
            val layoutErrorViewBlock = {
                errorView.value.isGone = false
                errorView.value.layout(l, 0, r, height)
                emptyView.isGone = true
            }
            val layoutEmptyViewBlock = {
                emptyView.isGone = false
                emptyView.layout(l, 0, r, height)
                runIfErrorViewInitialized { isGone = true }
            }
            when(mState){
                STATE_SHOW_ERROR -> layoutErrorViewBlock()
                STATE_SHOW_EMPTY -> layoutEmptyViewBlock()
                STATE_HIDE -> when(fromState){
                    STATE_SHOW_ERROR -> layoutErrorViewBlock()
                    STATE_SHOW_EMPTY -> layoutEmptyViewBlock()
                }
            }
        }
    }

    override fun computeScroll() {
        super.computeScroll()
        if (mScroller.computeScrollOffset() || errorViewScroller.computeScrollOffset()) {
            requestLayout()
        }
    }

    override fun canScrollVertically(direction: Int): Boolean {
        return when(mState){
            STATE_HIDE -> content.canScrollVertically(direction)
            else -> false
        }
    }

    override fun onNestedScroll(
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        type: Int,
        consumed: IntArray
    ) {
        dispatchNestedScroll(
            dxConsumed,
            dyConsumed,
            dxUnconsumed,
            dyUnconsumed,
            null,
            type,
            consumed
        )
    }

    override fun onStopNestedScroll(target: View, type: Int) {
        mScrollParentHelper.onStopNestedScroll(target, type)
        stopNestedScroll(type)
    }

    override fun onStartNestedScroll(child: View, target: View, axes: Int, type: Int): Boolean {
        mAxes = axes
        return true
    }

    override fun onNestedScrollAccepted(child: View, target: View, axes: Int, type: Int) {
        mScrollParentHelper.onNestedScrollAccepted(child, target, axes, type)
        startNestedScroll(axes, type)
    }

    override fun onNestedScroll(
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        type: Int
    ) {
        dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, null, type)
    }

    /**
     * NestedScrollChild
     */
    override fun dispatchNestedScroll(
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        offsetInWindow: IntArray?,
        type: Int,
        consumed: IntArray
    ) {
        return mScrollChildHelper.dispatchNestedScroll(
            dxConsumed,
            dyConsumed,
            dxUnconsumed,
            dyUnconsumed,
            offsetInWindow,
            type,
            consumed
        )
    }

    override fun dispatchNestedScroll(
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        offsetInWindow: IntArray?,
        type: Int
    ): Boolean {
        return mScrollChildHelper.dispatchNestedScroll(
            dxConsumed,
            dyConsumed,
            dxUnconsumed,
            dyUnconsumed,
            offsetInWindow,
            type
        )
    }

    override fun dispatchNestedPreScroll(
        dx: Int,
        dy: Int,
        consumed: IntArray?,
        offsetInWindow: IntArray?,
        type: Int
    ): Boolean {
        return mScrollChildHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow, type)
    }

    override fun stopNestedScroll(type: Int) {
        mScrollChildHelper.stopNestedScroll(type)
    }

    override fun hasNestedScrollingParent(type: Int): Boolean {
        return mScrollChildHelper.hasNestedScrollingParent(type)
    }

    override fun startNestedScroll(axes: Int, type: Int): Boolean {
        return mScrollChildHelper.startNestedScroll(axes, type)
    }
}