package com.hxbreak.animalcrossingtools.di

import android.content.Context
import dagger.Module
import dagger.Provides
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.FlutterEngineCache
import io.flutter.embedding.engine.dart.DartExecutor
import javax.inject.Singleton

@Module
object FlutterModule {

    @JvmStatic
    @Singleton
    @Provides
    fun provideFlutterEngine(context: Context): FlutterEngine {
        val engine = FlutterEngine(context)
        engine.dartExecutor.executeDartEntrypoint(
            DartExecutor.DartEntrypoint.createDefault()
        )
        FlutterEngineCache.getInstance().put("only", engine)
        return engine
    }

}