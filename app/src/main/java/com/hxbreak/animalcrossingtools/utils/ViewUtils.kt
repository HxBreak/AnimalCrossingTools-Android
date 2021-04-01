package com.hxbreak.animalcrossingtools.utils

import android.content.Context
import android.content.res.Resources
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ImageButton
import androidx.core.view.forEach

object ViewUtils {

    fun px2dp(context: Context, px: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (px / scale + .5).toInt()
    }

    fun dp2px(context: Context, dp: Float): Int {
        return dp2px(
            context.resources,
            dp
        )
    }

    fun dp2px(resources: Resources, dp: Float): Int {
        val scale = resources.displayMetrics.density
        return (dp * scale + .5).toInt()
    }

    fun View.safetyHideSoftKeyboard(){
        (context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)?.run {
            try {
                hideSoftInputFromWindow(windowToken, 0)
            }catch (e: Exception){ }
        }
    }

    fun <T> findViewByClass(view: View, clazz: Class<T>): T? {
        if (view is ViewGroup){
            view.forEach {
                val result = findViewByClass(it, clazz)
                if (result != null){
                    return result
                }
            }
            return null
        }else{
            return if (clazz.isInstance(view)){
                view as T
            }else{
                null
            }
        }
    }

}

const val ANIMATION_FAST_MILLIS = 50L
const val ANIMATION_SLOW_MILLIS = 100L

fun View.simulateClick(delay: Long = ANIMATION_FAST_MILLIS) {
//    performClick()
    isPressed = true
    invalidate()
    postDelayed({
        invalidate()
        isPressed = false
    }, delay)
}