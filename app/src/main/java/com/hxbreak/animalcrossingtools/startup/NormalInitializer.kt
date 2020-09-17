package com.hxbreak.animalcrossingtools.startup

import android.app.Application
import android.content.Context
import androidx.startup.Initializer

class NormalInitializer: Initializer<Unit>{

    override fun create(context: Context): Unit {
    }

    override fun dependencies(): MutableList<Class<out Initializer<*>>>  = mutableListOf()

}