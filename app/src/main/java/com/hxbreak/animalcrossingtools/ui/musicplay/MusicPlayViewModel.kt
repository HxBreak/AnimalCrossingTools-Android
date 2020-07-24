package com.hxbreak.animalcrossingtools.ui.musicplay

import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.uamp.media.extensions.currentPlayBackPosition
import com.hxbreak.animalcrossingtools.CombinedRunCheck
import com.hxbreak.animalcrossingtools.combinedLiveData
import com.hxbreak.animalcrossingtools.media.MusicServiceConnection
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.floor

class MusicPlayViewModel @Inject constructor(val connection: MusicServiceConnection) : ViewModel() {

    private val nowPlaying = Observer<MediaMetadataCompat> {
        Timber.e(it.toString())
    }

    val current = MutableLiveData<Int>(0)

    private val playbackState = Observer<PlaybackStateCompat> {
    }

    private suspend fun checkPlayState() {
        val it = connection.playbackState.value
        if (it?.state == PlaybackStateCompat.STATE_PLAYING) {
            val curr = it.currentPlayBackPosition.toInt()
            if (curr != current.value) {
                current.postValue(curr)
            }
        }
        delay(50)
    }

    val playerState = combinedLiveData(viewModelScope.coroutineContext,
        x = connection.playbackState,
        y = connection.nowPlaying, runCheck = { x, y -> y }) { x, y ->
        emit(y to x)
    }

    init {
        connection.nowPlaying.observeForever(nowPlaying)
        connection.playbackState.observeForever(playbackState)
        viewModelScope.launch {
            while (true) {
                checkPlayState()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        connection.playbackState.removeObserver(playbackState)
        connection.nowPlaying.removeObserver(nowPlaying)
    }
}