package com.hxbreak.animalcrossingtools

import android.app.Application
import com.hxbreak.animalcrossingtools.data.source.AnimalCrossingDatabase
import com.hxbreak.animalcrossingtools.media.MusicServiceConnection
import dagger.hilt.android.HiltAndroidApp
import io.flutter.embedding.engine.FlutterEngine
import javax.inject.Inject

@HiltAndroidApp
open class App : Application() {

    @Inject
    lateinit var database: AnimalCrossingDatabase

    @Inject
    lateinit var musicServiceConnection: MusicServiceConnection

//    @Inject
//    lateinit var engine: FlutterEngine

    override fun onCreate() {
        super.onCreate()
    }

}