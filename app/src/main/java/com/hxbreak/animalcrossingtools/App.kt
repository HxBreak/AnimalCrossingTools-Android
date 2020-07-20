package com.hxbreak.animalcrossingtools

import com.hxbreak.animalcrossingtools.data.source.AnimalCrossingDatabase
import com.hxbreak.animalcrossingtools.di.DaggerApplicationComponent
import com.hxbreak.animalcrossingtools.media.MusicServiceConnection
import dagger.android.AndroidInjector
import dagger.android.support.DaggerApplication
import timber.log.Timber
import javax.inject.Inject

open class App : DaggerApplication() {

    @Inject
    lateinit var database: AnimalCrossingDatabase

    @Inject
    lateinit var musicServiceConnection: MusicServiceConnection

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return DaggerApplicationComponent.factory().create(applicationContext)
    }
}