package com.hxbreak.animalcrossingtools

import com.hxbreak.animalcrossingtools.data.source.AnimalCrossingDatabase
import com.hxbreak.animalcrossingtools.di.DaggerApplicationComponent
import com.hxbreak.animalcrossingtools.media.MusicServiceConnection
import dagger.android.AndroidInjector
import dagger.android.support.DaggerApplication
import io.flutter.embedding.engine.FlutterEngine
import javax.inject.Inject

open class App : DaggerApplication() {

    @Inject
    lateinit var database: AnimalCrossingDatabase

    @Inject
    lateinit var musicServiceConnection: MusicServiceConnection

    @Inject
    lateinit var engine: FlutterEngine

    override fun onCreate() {
        super.onCreate()
    }

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return DaggerApplicationComponent.factory().create(applicationContext)
    }
}