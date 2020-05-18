package com.hxbreak.animalcrossingtools

import android.app.Application

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        ServiceLocator.provideRepository(this)
    }
}