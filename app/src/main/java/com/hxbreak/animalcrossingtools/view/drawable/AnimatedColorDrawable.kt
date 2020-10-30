package com.hxbreak.animalcrossingtools.view.drawable

import android.animation.Animator
import android.animation.ValueAnimator
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.PixelFormat
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.google.android.material.animation.ArgbEvaluatorCompat

class AnimatedColorDrawable(
    private val color1: Int,
    private val color2: Int,
    private val duration: Long,
) : Drawable(){

    private var currentColor = color1
    private var mAlpha: Int = 255
    private var mAnimator: Animator? = null

    companion object {
        val INTERPOLATOR = FastOutSlowInInterpolator()
    }

    fun forward(){
        if (currentColor == color2) return
        val animator = ValueAnimator.ofInt(currentColor, color2)
        animator.duration = duration
        animator.setEvaluator(ArgbEvaluatorCompat())
        animator.interpolator = INTERPOLATOR
        animator.addUpdateListener {
            currentColor = it.animatedValue as Int
            invalidateSelf()
        }
        if (mAnimator?.isRunning == true){
            mAnimator?.cancel()
        }
        mAnimator = animator
        animator.start()
    }

    fun reverse(){
        if (currentColor == color1) return
        val animator = ValueAnimator.ofInt(currentColor, color1)
        animator.duration = duration
        animator.setEvaluator(ArgbEvaluatorCompat())
        animator.interpolator = INTERPOLATOR
        animator.addUpdateListener {
            currentColor = it.animatedValue as Int
            invalidateSelf()
        }
        if (mAnimator?.isRunning == true){
            mAnimator?.cancel()
        }
        mAnimator = animator
        animator.start()
    }

    override fun draw(canvas: Canvas) {
        canvas.drawColor(currentColor)
    }

    override fun setAlpha(alpha: Int) {
        mAlpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
    }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }

}