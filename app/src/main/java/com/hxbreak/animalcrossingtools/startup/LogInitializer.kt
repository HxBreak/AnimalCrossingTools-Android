package com.hxbreak.animalcrossingtools.startup

import android.content.Context
import androidx.startup.Initializer
import timber.log.Timber

class LogInitializer: Initializer<Unit>{

    override fun create(context: Context): Unit {
        Timber.plant(Timber.DebugTree())
    }

    override fun dependencies(): MutableList<Class<out Initializer<*>>>  = mutableListOf()

}