package com.hxbreak.animalcrossingtools.view

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.core.view.*
import kotlin.math.abs


class ScrollViewGroup @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr), NestedScrollingParent3, NestedScrollingChild3 {

    private val mScrollParentHelper = NestedScrollingParentHelper(this)
    private val mScrollChildHelper = NestedScrollingChildHelper(this)

    init {
        mScrollChildHelper.isNestedScrollingEnabled = true
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
//        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
//        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
//        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
//        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        measureChildren(widthMeasureSpec, heightMeasureSpec)
//        val first = getChildAt(0)
//        val second = getChildAt(1)
//        getChildAt(1).measure(widthMeasureSpec, getChildMeasureSpec(
//            widthMeasureSpec,
//            0,
//            heightSize - first.measuredHeight
//        ))
//        setMeasuredDimension(widthSize, heightSize)
    }

    private var mScrolled = 0

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val first = getChildAt(0)
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (i == 0) {
                child.layout(0, -mScrolled, child.measuredWidth, child.measuredHeight - mScrolled)
            } else {
                child.layout(
                    0,
                    first.measuredHeight - mScrolled,
                    child.measuredWidth,
                    measuredHeight
                )
            }
        }
//        val body = get(1)
//        body.measure(MeasureSpec.makeMeasureSpec(measuredWidth, MeasureSpec.EXACTLY),
//            MeasureSpec.makeMeasureSpec(body.bottom - body.top, MeasureSpec.EXACTLY))
    }

    class LayoutParams : MarginLayoutParams {

        constructor(c: Context?, attrs: AttributeSet?) : super(c, attrs)

        constructor(width: Int, height: Int) : super(width, height)

        constructor(source: ViewGroup.LayoutParams?) : super(source) {}
    }

    fun canStickyTopScrollUp(): Boolean {
        val stickyTopView = get(0)
        return stickyTopView.bottom > 0
    }


    fun canStickyTopScrollDown(): Boolean {
        val stickyTopView = get(0)
        return stickyTopView.bottom < stickyTopView.measuredHeight
    }

    private fun getStickyTopScrollDownLeft(): Int {
        val stickyTopView = get(0)
        return stickyTopView.measuredHeight - stickyTopView.bottom
    }

    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
        val stickyTopView = get(0)
        val body = get(1)
        if (dy > 0) {
            var offset = 0
            if (canStickyTopScrollUp()) {
                offset = dy
                if (dy > stickyTopView.bottom) {
                    offset = stickyTopView.bottom
                }
                stickyTopView.offsetTopAndBottom(-offset)
                body.layout(0, stickyTopView.bottom, measuredWidth, measuredHeight)
                consumed[1] = offset
                mScrolled += offset
            }
        } else {
            if (canStickyTopScrollDown()) {
                val left = getStickyTopScrollDownLeft()
                var offset = abs(dy)
                if (abs(dy) > left) {
                    offset = left
                }
                stickyTopView.offsetTopAndBottom(offset)
                body.layout(0, stickyTopView.bottom, measuredWidth, measuredHeight)
                consumed[1] = -offset
                mScrolled -= offset
            }
        }
        dispatchNestedPreScroll(dx, dy, consumed, null, type)
    }

    override fun onStopNestedScroll(target: View, type: Int) {
        mScrollParentHelper.onStopNestedScroll(target, type)
        stopNestedScroll(type)
    }

    override fun onStartNestedScroll(child: View, target: View, axes: Int, type: Int): Boolean {
        return axes and ViewCompat.SCROLL_AXIS_VERTICAL != 0
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