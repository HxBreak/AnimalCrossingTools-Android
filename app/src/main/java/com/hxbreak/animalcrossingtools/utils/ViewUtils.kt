package com.hxbreak.animalcrossingtools.utils

import android.content.Context
import android.content.res.Resources

object ViewUtils {

    fun px2dp(context: Context, px: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (px / scale + .5).toInt()
    }

    inline fun dp2px(context: Context, dp: Float): Int {
        return dp2px(
            context.resources,
            dp
        )
    }

    fun dp2px(resources: Resources, dp: Float): Int {
        val scale = resources.displayMetrics.density
        return (dp * scale + .5).toInt()
    }

}
