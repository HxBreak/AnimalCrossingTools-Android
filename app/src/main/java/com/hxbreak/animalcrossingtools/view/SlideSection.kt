package com.hxbreak.animalcrossingtools.view

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IntDef
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.FloatValueHolder
import androidx.dynamicanimation.animation.SpringAnimation
import timber.log.Timber
import kotlin.math.max

open class SlideSection : ViewGroup {

    @IntDef(value = [EXPANDED, COLLAPSED])
    @kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
    annotation class State

    companion object {
        const val EXPANDED = 0
        const val COLLAPSED = 1
        const val EXPANDING = 2
        const val COLLAPSING = 3

        const val PARENT_DATA = "PARENT"
        const val KEY_STATE = "State"
    }


    var bounceAnimation: SpringAnimation? = null
    private var mState: Int = COLLAPSED

    private var currentProgress: Int = 0

    private lateinit var maskView: View
    private lateinit var child: View

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(attrs, defStyle)
    }

    protected fun init(attrs: AttributeSet?, defStyle: Int) {
        setWillNotDraw(true)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        measureChildren(widthMeasureSpec, heightMeasureSpec)
        val size = max(MeasureSpec.getSize(heightMeasureSpec), maskView.measuredHeight)
        val underViewSize = max(size, child.measuredHeight)
        when (MeasureSpec.getMode(heightMeasureSpec)) {
            MeasureSpec.UNSPECIFIED -> {
                setMeasuredDimension(
                    getDefaultSize(suggestedMinimumWidth, widthMeasureSpec),
                    getDefaultSize(
                        underViewSize, MeasureSpec.makeMeasureSpec(
                            MeasureSpec.getSize(heightMeasureSpec),
                            MeasureSpec.UNSPECIFIED
                        )
                    )
                )
            }
            else -> super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
        measureChild(child, widthMeasureSpec, MeasureSpec.makeMeasureSpec(underViewSize, MeasureSpec.EXACTLY))
        measureChild(maskView, widthMeasureSpec, MeasureSpec.makeMeasureSpec(underViewSize, MeasureSpec.EXACTLY))
//        Timber.e("onMeasure $measuredWidth $measuredHeight ${child.measuredHeight}")
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
//        Timber.e("onLayout ${maskView.measuredWidth}, ${maskView.measuredHeight}")
        if (mState == EXPANDED){
            currentProgress = movingWidth()
        }else if (mState == COLLAPSED){
            currentProgress = 0
        }
        moveChild(currentProgress)
    }

    private fun moveChild(offset: Int) {
        maskView.layout(offset, 0, maskView.measuredWidth + offset, maskView.measuredHeight)
        child.layout(0, 0, offset, child.measuredHeight)
//        Timber.e("${child.measuredHeight} ${child.measuredWidth}")
    }

    private fun movingWidth() = child.measuredWidth

    val endAnimationListener = DynamicAnimation.OnAnimationEndListener { animation, canceled, value, velocity ->
        currentProgress = value.toInt()
        mState = when (mState) {
            EXPANDING -> EXPANDED
            COLLAPSING -> COLLAPSED
            else -> throw IllegalStateException("Internal Error State $mState EndListener")
        }
        requestLayout()
    }

    /**
     * 设置组件状态
     */
    fun setState(@State state: Int) {
        /**
         * 判断将要变更的状态，即使当前状态为animating也取消当前动画准备进行反向动画
         */
        when (state) {
            EXPANDED -> {
                when (mState) {
                    COLLAPSING -> {
                        bounceAnimation?.cancel() //async end animation
                        mState = EXPANDING
                    }
                    COLLAPSED -> mState = EXPANDING
                    EXPANDING, EXPANDED -> return
                }
            }
            COLLAPSED -> {
                when (mState) {
                    EXPANDING -> {
                        bounceAnimation?.cancel()
                        mState = COLLAPSING
                    }
                    EXPANDED -> mState = COLLAPSING
                    COLLAPSED, COLLAPSING -> return
                }
            }
            else -> throw IllegalStateException("Only Allow 2 State To Handle.")
        }
        if (mState == EXPANDING || mState == COLLAPSING) {
            bounceAnimation = SpringAnimation(FloatValueHolder(currentProgress.toFloat()))
                .addUpdateListener { animation, value, velocity ->
                    currentProgress = value.toInt()
                    requestLayout()
                }
                .addEndListener(endAnimationListener)
                .apply {
                    val to = when (mState) {
                        EXPANDING -> movingWidth()
                        COLLAPSING -> 0
                        else -> throw IllegalStateException("Internal Error State $mState ready start")
                    }
                    animateToFinalPosition(to.toFloat())
                }
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        if (childCount != 2) {
            throw IllegalStateException("Only 2 child allowed")
        }
        child = getChildAt(0)
        maskView = getChildAt(1)
    }

    override fun onSaveInstanceState(): Parcelable? {
        val parcelable = super.onSaveInstanceState()
        val bundle = Bundle()
        bundle.putParcelable(PARENT_DATA, parcelable)
        bundle.putInt(KEY_STATE, mState)
        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        val bundle = state as Bundle
        mState = bundle.getInt(KEY_STATE)
        super.onRestoreInstanceState(bundle.getParcelable(PARENT_DATA))
    }
}