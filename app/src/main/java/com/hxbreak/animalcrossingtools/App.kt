package com.hxbreak.animalcrossingtools

import android.app.Application
import android.content.Intent
import android.os.Bundle
import com.hxbreak.animalcrossingtools.data.source.AnimalCrossingDatabase
import com.hxbreak.animalcrossingtools.media.MusicServiceConnection
import com.hxbreak.animalcrossingtools.services.InstantMessageServices
import dagger.hilt.android.HiltAndroidApp
import io.flutter.embedding.engine.FlutterEngine
import java.lang.Exception
import javax.inject.Inject

@HiltAndroidApp
open class App : Application() {

    @Inject
    lateinit var database: AnimalCrossingDatabase

    @Inject
    lateinit var musicServiceConnection: MusicServiceConnection

    @Inject
    lateinit var engine: FlutterEngine

    override fun onCreate() {
        super.onCreate()
        val intent = Intent(this, InstantMessageServices::class.java)
        try {
            startService(intent)
        }catch (e: Exception){
            e.printStackTrace()
        }
    }
}