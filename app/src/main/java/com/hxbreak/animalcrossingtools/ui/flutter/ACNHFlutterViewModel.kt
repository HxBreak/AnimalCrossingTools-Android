package com.hxbreak.animalcrossingtools.ui.flutter

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import io.flutter.embedding.engine.FlutterEngine

class ACNHFlutterViewModel @ViewModelInject constructor(
    private val engine: FlutterEngine,
    @Assisted val savedState: SavedStateHandle
) : ViewModel(){
    init {
        savedState.get<String>("destination")?.let {
            engine.navigationChannel.pushRoute(it)
        }
    }
}