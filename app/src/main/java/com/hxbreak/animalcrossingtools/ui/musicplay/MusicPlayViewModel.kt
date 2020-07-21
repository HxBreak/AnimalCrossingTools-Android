package com.hxbreak.animalcrossingtools.ui.musicplay

import android.support.v4.media.MediaMetadataCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.hxbreak.animalcrossingtools.media.MusicServiceConnection
import timber.log.Timber
import javax.inject.Inject

class MusicPlayViewModel @Inject constructor(val connection: MusicServiceConnection) : ViewModel() {

    private val nowPlaying = Observer<MediaMetadataCompat> {
        Timber.e(it.toString())
    }

    init {
        connection.nowPlaying.observeForever(nowPlaying)
    }

    override fun onCleared() {
        super.onCleared()
        connection.nowPlaying.removeObserver(nowPlaying)
    }
}