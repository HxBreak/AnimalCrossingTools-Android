package com.hxbreak.animalcrossingtools.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.OverScroller
import android.widget.ScrollView
import android.widget.Scroller
import androidx.core.graphics.minus
import androidx.core.math.MathUtils
import kotlin.math.absoluteValue
import kotlin.math.max

class FlowerMap @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var blockWidth: Int = 0
    var blockHeight: Int = 0
    var virtualCountX = 10
    var virtualCountY = 5
    var viewOffsetX = 0F
    var viewOffsetY = 0F
    var div = 0
    val mScroller = OverScroller(context, LinearInterpolator())

    init {
        blockWidth = ViewUtils.dp2px(context, 24f)
        blockHeight = ViewUtils.dp2px(context, 24f)
        div = ViewUtils.dp2px(context, 1f)
        mScroller.setFriction(3F)
        Log.e("HxBreak", "FlowerMap Init")
    }

    private fun isFlower(
        emptyOnStart: Boolean,
        x: Int,
        y: Int,
        rowCount: Int,
        columnCount: Int
    ): Boolean {
        /*
        O X O X
        X O X O
        O X O X

        O X O
        X O X
        O X O
         */
        val vx = x + 1
        val vy = y + 1
        if (vx % 2 == 0) {
            if (vy % 2 == 0) {
                return true
            }
        } else {
            if (vy % 2 == 1) {
                return true
            }
        }

        return false
    }

    fun cropSquare(x: Float, y: Float, value: Rect): Rect {
        val rect = Rect()

        rect.left = ((value.left - x) / blockWidth).toInt()
        rect.right = ((value.right - x) / blockWidth).toInt()

        rect.bottom = ((value.bottom - y) / blockHeight).toInt()
        rect.top = ((value.top - y) / blockHeight).toInt()

        return rect
    }

    var flingX = 0
    var flingY = 0

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.let {
            it.save()
            it.drawARGB(0xff, 0xff, 0, 0)
//            if (isScrolling){
//                Log.e("HxBreak", "asd ${mScroller.currX} ${mScroller.currY} ${mScroller.currVelocity}")
//                invalidate()
//            }
            it.drawText(
                "viewOffsetX: ${viewOffsetX + flingX}, viewOffsetY: ${viewOffsetY + flingY}",
                0F,
                100F,
                Paint().apply {
                    color = Color.BLACK
                    isAntiAlias = true
                    textSize = 40F
                    style = Paint.Style.FILL_AND_STROKE
                    strokeWidth = 1F
                })
            it.translate(viewOffsetX + flingX, viewOffsetY + flingY)
            val rect = Rect(0, 0, blockWidth, blockHeight)
            rect.inset(div, div)
            val black = Paint().apply {
                color = Color.BLACK
            }
            val white = Paint().apply {
                color = Color.WHITE
            }
            val xBlocks = measuredWidth / blockWidth
            val yBlocks = measuredHeight / blockHeight
            val renderRect = Rect(0, measuredHeight, measuredWidth, 0)
            val renderBoxRect =
                cropSquare((viewOffsetX + flingX), (viewOffsetY + flingY), renderRect)
            for (virtualY in (max(renderBoxRect.bottom, 0))..(renderBoxRect.top)) {
                if (virtualY >= virtualCountY)
                    break
                it.save()
                it.translate(0F, (virtualY * blockHeight).toFloat())
                for (virtualX in (max(renderBoxRect.left, 0))..(renderBoxRect.right)) {
                    if (virtualX >= virtualCountX)
                        break
                    if (isFlower(true, virtualX, virtualY, xBlocks, yBlocks)) {
                        continue
                    } else {
                        it.save()
                        it.translate((virtualX * blockWidth).toFloat(), 0F)
                        it.drawRect(rect, black)
                        it.restore()
                    }
                }
                it.restore()
            }
            it.restore()
        }
    }

    var mTouchDownX = 0F
    var mTouchDownY = 0F


    private val mGestureDetectorListener = object : GestureDetector.SimpleOnGestureListener() {

        override fun onDoubleTap(e: MotionEvent?): Boolean {
            performClick()
            return super.onDoubleTap(e)
        }

        override fun onDown(e: MotionEvent?): Boolean {
            mScroller.forceFinished(true)
            viewOffsetX += flingX
            viewOffsetY += flingY
            flingX = 0
            flingY = 0
            invalidate()
            return true
        }

        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent?,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            Log.e("HxBreak", "onFling $velocityX $velocityY")
            if (e1 == null || e2 == null) {
                return false
            }

//            mScroller.fling(0, 0,
//                velocityX.toInt(),
//                velocityY.toInt(),
//                velocityX.toInt(), velocityX.toInt(), velocityY.toInt(), velocityY.toInt())
            invalidate()
            return super.onFling(e1, e2, velocityX, velocityY)
        }

        override fun onScroll(
            e1: MotionEvent?,
            e2: MotionEvent?,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            if (e1 == null || e2 == null) {
                return false
            }
            Log.e("HxBreak", "${e1.x} ${e1.y} $distanceX, $distanceY")
            viewOffsetX -= distanceX
            viewOffsetY -= distanceY
            invalidate()
            return super.onScroll(e1, e2, distanceX, distanceY)
        }
    }

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
    }

    override fun computeScroll() {
        super.computeScroll()
        if (mScroller.computeScrollOffset()) {
            invalidate()
            flingX = mScroller.currX
            flingY = mScroller.currY
            Log.e("HxBreak", "${mScroller.currX} ${mScroller.currY} ${mScroller.currVelocity}")
        } else {
        }


    }

    val mGestureDetector = GestureDetector(context, mGestureDetectorListener)
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let {
            return mGestureDetector.onTouchEvent(event)
//            when(it.action){
//                MotionEvent.ACTION_DOWN -> {
//                    mTouchDownX = event.x
//                    mTouchDownY = event.y
//                }
//                MotionEvent.ACTION_MOVE -> {
//                    val moveX = event.x - mTouchDownX
//                    val moveY = event.y - mTouchDownY
//                    viewOffsetX += moveX
//                    viewOffsetY += moveY
//                    mTouchDownX = event.x
//                    mTouchDownY = event.y
//                }
//                MotionEvent.ACTION_UP -> {
//                    mTouchDownX = 0F
//                    mTouchDownY = 0F
//                }
//                else -> {}
//            }
//            invalidate()
        }
        return false
    }
}