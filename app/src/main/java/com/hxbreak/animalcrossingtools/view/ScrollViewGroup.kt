package com.hxbreak.animalcrossingtools.view

import android.content.Context
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Scroller
import androidx.core.view.*
import timber.log.Timber
import kotlin.math.abs


class ScrollViewGroup @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr), NestedScrollingParent3, NestedScrollingChild3 {

    private val mScrollParentHelper = NestedScrollingParentHelper(this)
    private val mScrollChildHelper = NestedScrollingChildHelper(this)
    private val mScroller = Scroller(context)
    private lateinit var mPinnedView: View

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
        measureChildren(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        updateLayout()
    }

    class LayoutParams : MarginLayoutParams {

        constructor(c: Context?, attrs: AttributeSet?) : super(c, attrs)

        constructor(width: Int, height: Int) : super(width, height)

        constructor(source: ViewGroup.LayoutParams?) : super(source) {}
    }

    private fun effectScroll(dy: Int) {
        mScroller.finalY = (mScroller.finalY + dy).coerceIn(0, mPinnedView.measuredHeight)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        for (i in 0 until childCount) {
            val view = get(i)
            if (view.tag is String && "pin" == view.tag) {
                mPinnedView = view
            }
        }
    }

    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
        val oldScroll: Int
        val left = if (dy > 0) {
            dispatchNestedPreScroll(dx, dy, consumed, null, type)
            oldScroll = mScroller.finalY
            dy - consumed[1]
        } else {
            oldScroll = mScroller.finalY
            dy
        }
        effectScroll(left)
        val scrolled = mScroller.finalY - oldScroll
        consumed[1] += scrolled
        if (dy < 0) {
            dispatchNestedPreScroll(dx, dy - consumed[1], consumed, null, type)
        }
        mScroller.forceFinished(true)
        updateLayout()
    }

    override fun computeScroll() {
        super.computeScroll()
        if (mScroller.computeScrollOffset()) {
//            Timber.i("computeScrollOffset")
            requestLayout()
        }
    }

    private fun updateLayout() {
//        Timber.i("updateLayout")
        val oScroller = if (mScroller.isFinished) mScroller.finalY else mScroller.currY
        val scroll = oScroller.coerceIn(0, mPinnedView.measuredHeight)
        if (oScroller < 0 || oScroller > mPinnedView.measuredHeight) {
//            Timber.i("forceFinished")
            mScroller.finalY = scroll
            mScroller.forceFinished(true)
        }

        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child == mPinnedView) {
                mPinnedView.layout(0, -scroll, child.measuredWidth, child.measuredHeight - scroll)
            } else {
                child.layout(
                    0,
                    mPinnedView.measuredHeight - scroll,
                    child.measuredWidth,
                    measuredHeight
                )
            }
        }
    }

    override fun onSaveInstanceState(): Parcelable? {
        val parcelable = super.onSaveInstanceState()
        val bundle = Bundle()
        bundle.putParcelable("parentData", parcelable)
        bundle.putInt("offset", mScroller.finalY)
        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        val bundle = state as Bundle
        mScroller.finalY = bundle.getInt("offset")
        super.onRestoreInstanceState(bundle.getParcelable("parentData"))
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