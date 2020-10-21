package com.hxbreak.animalcrossingtools.di

import android.content.Context
import com.hxbreak.animalcrossingtools.data.prefs.PreferenceStorage
import com.hxbreak.animalcrossingtools.theme.Theme
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.FlutterEngineCache
import io.flutter.embedding.engine.dart.DartExecutor
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodChannel
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object FlutterModule {

    @JvmStatic
    @Singleton
    @Provides
    fun provideFlutterEngine(@ApplicationContext context: Context, storage: PreferenceStorage): FlutterEngine {
        val engine = FlutterEngine(context)
        engine.dartExecutor.executeDartEntrypoint(
            DartExecutor.DartEntrypoint.createDefault()
        )
        FlutterEngineCache.getInstance().put("only", engine)
        val channel = MethodChannel(engine.dartExecutor, "com.hxbreak.animalcrossingtools/app")
        channel.setMethodCallHandler { call, result ->
            when(call.method){
                "currentTheme" -> {
                    result.success(storage.selectedTheme ?: Theme.SYSTEM.storageKey)
                }
            }
        }
        val eventChannel = EventChannel(engine.dartExecutor, "com.hxbreak.animalcrossingtools/app/event")
        val observerSinkList = mutableListOf<EventChannel.EventSink>()
        eventChannel.setStreamHandler(object : EventChannel.StreamHandler{
            override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
                events?.let { observerSinkList.add(it) }
            }

            override fun onCancel(arguments: Any?) {}
        })
        storage.observableSelectedTheme.observeForever { theme ->
            observerSinkList.forEach {
                it.success(
                    mutableMapOf(
                        "type" to "theme",
                        "data" to theme,
                    )
                )
            }
        }
        return engine
    }

}