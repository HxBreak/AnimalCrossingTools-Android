package com.hxbreak.animalcrossingtools.extensions

import android.annotation.SuppressLint
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.hxbreak.animalcrossingtools.GlideRequest
import com.hxbreak.animalcrossingtools.R
import com.hxbreak.animalcrossingtools.theme.Theme
import com.hxbreak.animalcrossingtools.utils.ViewUtils


/**
 * Having to suppress lint. Bug raised: 128789886
 */
@SuppressLint("WrongConstant")
fun AppCompatActivity.updateForTheme(theme: Theme) = when (theme) {
    Theme.DARK -> delegate.localNightMode = AppCompatDelegate.MODE_NIGHT_YES
    Theme.LIGHT -> delegate.localNightMode = AppCompatDelegate.MODE_NIGHT_NO
    Theme.SYSTEM -> delegate.localNightMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
    Theme.BATTERY_SAVER -> delegate.localNightMode = AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
}

fun GlideRequest<*>.littleCircleWaitAnimation(context: Context) = this.let {
    val color = context.resources.getColor(R.color.colorAccent)
    val drawable = CircularProgressDrawable(context).apply {
        strokeWidth = ViewUtils.dp2px(context, 2f).toFloat()
        centerRadius = ViewUtils.dp2px(context, 16f).toFloat()
        setColorSchemeColors(color)
        start()
    }
//    this.placeholder(drawable)
    this
}

fun RecyclerView.removeAllItemDecorations(){
    repeat(itemDecorationCount){
        removeItemDecorationAt(it)
    }
}
