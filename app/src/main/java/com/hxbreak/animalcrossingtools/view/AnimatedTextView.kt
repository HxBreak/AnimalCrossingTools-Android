package com.hxbreak.animalcrossingtools.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.dynamicanimation.animation.FloatValueHolder
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import androidx.dynamicanimation.animation.SpringForce.*
import kotlin.math.max

open class AnimatedTextView : FrameLayout {

    sealed class ScrollDirection {
        object UP : ScrollDirection()
        object DOWN : ScrollDirection()
    }

    companion object {
        val DOWN = ScrollDirection.DOWN
        val UP = ScrollDirection.UP
    }

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

    protected lateinit var title: TextView

    protected lateinit var nextTitle: TextView

    protected fun init(attrs: AttributeSet?, defStyle: Int) {
        title = TextView(context, attrs, defStyle)
        nextTitle = TextView(context, attrs, defStyle)
        title.maxLines = 1
        nextTitle.maxLines = 1
        val params = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        addView(title, params)
        addView(nextTitle, params)
        setWillNotDraw(true)
    }

    protected var titleHeight = 0

    protected var currentValue = 0f

    protected var movementHeight = 0

    protected var direction: ScrollDirection = DOWN

    override fun onLayout(p0: Boolean, p1: Int, p2: Int, p3: Int, p4: Int) {
        layoutChild()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        measureChildren(widthMeasureSpec, heightMeasureSpec)
        titleHeight = title.measuredHeight
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        movementHeight = h
    }

    var currentAnimation: SpringAnimation? = null

    protected var lastText: CharSequence? = null
    protected var currentText: CharSequence? = null

    private fun maxWidth(){
        measureChild(nextTitle, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED))
        measureChild(title, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED))
        val maxWidth = max(title.measuredWidth, nextTitle.measuredWidth)
        setMeasuredDimension(maxWidth, measuredHeight)
        requestLayout()
    }

    fun clearLastSelected(){
        lastText = null
    }

    /**
     * 为TextView设置新的text
     * @param text 新设置的文字
     * @param forceUp 强制从下往上滚动
     * @param allowUpScroll 允许当text == lastText 时从下往上滚动
     */
    fun setText(text: CharSequence, forceUp: Boolean = false, allowUpScroll: Boolean = true) {
        direction = if (text == lastText && allowUpScroll || forceUp) UP else DOWN
        val isNotSet = currentText == null
        lastText = currentText
        currentText = text
        if (isNotSet) {
            title.text = text //NonAnimation For First
            return
        }
        title.text = lastText
        nextTitle.text = currentText
        currentAnimation?.cancel()
        currentAnimation = SpringAnimation(FloatValueHolder(currentValue))
            .addUpdateListener { animation, value, velocity ->
                currentValue = value
                layoutChild()
            }
            .addEndListener { animation, canceled, value, velocity ->
                currentAnimation = null
                if (canceled){
                    title.text = text
                    nextTitle.text = lastText
                }else{
                    currentValue = 0f
                    title.text = text
                    nextTitle.text = lastText
                    nextTitle.visibility = View.GONE
                    layoutChild()
                    maxWidth()
                }
            }
            .apply {
                nextTitle.visibility = View.VISIBLE
                nextTitle.text = currentText
                title.text = lastText
                val force = SpringForce()
                force.dampingRatio = DAMPING_RATIO_LOW_BOUNCY
                force.stiffness = STIFFNESS_MEDIUM
                spring = force
                maxWidth()
                if (direction is ScrollDirection.DOWN){
                    animateToFinalPosition(movementHeight.toFloat())
                }else{
                    animateToFinalPosition(-movementHeight.toFloat())
                    lastText = null
                }
            }

    }

    fun getText() = currentText

    private fun layoutChild() {
        val top = (measuredHeight - titleHeight) / 2
        val titleTop = top + currentValue.toInt()
        title.layout(0, titleTop, measuredWidth, titleTop + title.measuredHeight)
        if (direction is ScrollDirection.DOWN){
            nextTitle.layout(0, titleTop - movementHeight, measuredWidth, titleTop + nextTitle.measuredHeight - movementHeight)
        }else{
            val nextTop = titleTop + movementHeight
            nextTitle.layout(0, nextTop, measuredWidth, nextTop + nextTitle.measuredHeight)
        }
    }

}
