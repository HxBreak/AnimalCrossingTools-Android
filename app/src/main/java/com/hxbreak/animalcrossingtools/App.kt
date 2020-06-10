package com.hxbreak.animalcrossingtools

import com.hxbreak.animalcrossingtools.data.source.AnimalCrossingDatabase
import com.hxbreak.animalcrossingtools.di.DaggerApplicationComponent
import dagger.android.AndroidInjector
import dagger.android.support.DaggerApplication
import javax.inject.Inject

open class App : DaggerApplication() {

    @Inject
    lateinit var database: AnimalCrossingDatabase

    override fun onCreate() {
        super.onCreate()
    }

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return DaggerApplicationComponent.factory().create(applicationContext)
    }
}