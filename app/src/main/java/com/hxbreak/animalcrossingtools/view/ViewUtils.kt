package com.hxbreak.animalcrossingtools.view

import android.content.Context

object ViewUtils {

    fun px2dp(context: Context, px: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (px / scale + .5).toInt()
    }

    fun dp2px(context: Context, dp: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dp * scale + .5).toInt()
    }

}