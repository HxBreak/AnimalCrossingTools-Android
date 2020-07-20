package com.hxbreak.animalcrossingtools.extensions

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.hxbreak.animalcrossingtools.theme.Theme


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
