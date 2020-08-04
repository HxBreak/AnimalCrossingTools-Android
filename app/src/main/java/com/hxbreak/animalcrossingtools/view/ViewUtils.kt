package com.hxbreak.animalcrossingtools.view

import android.content.Context
import android.content.res.Resources

object ViewUtils {

    fun px2dp(context: Context, px: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (px / scale + .5).toInt()
    }

    inline fun dp2px(context: Context, dp: Float): Int {
        return dp2px(context.resources, dp)
    }

    fun dp2px(resources: Resources, dp: Float): Int {
        val scale = resources.displayMetrics.density
        return (dp * scale + .5).toInt()
    }

}

object FontUtils {

    fun px2sp(context: Context, pxValue: Float): Int {
        val fontScale = context.resources.displayMetrics.scaledDensity
        return (pxValue / fontScale + 0.5f).toInt()
    }

    fun sp2px(context: Context, spValue: Float): Int {
        val fontScale = context.resources.displayMetrics.scaledDensity
        return (spValue * fontScale + 0.5f).toInt()
    }

}