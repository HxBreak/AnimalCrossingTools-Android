package com.hxbreak.animalcrossingtools.utils

import android.content.Context
import android.content.res.Resources
import android.view.View
import android.view.inputmethod.InputMethodManager

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

}
