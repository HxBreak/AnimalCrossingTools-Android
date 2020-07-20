package com.hxbreak.animalcrossingtools.ui.musicplay

import androidx.lifecycle.ViewModel
import com.hxbreak.animalcrossingtools.media.MusicServiceConnection
import javax.inject.Inject

class MusicPlayViewModel @Inject constructor(connection: MusicServiceConnection) : ViewModel() {

    init {
        connection.transportControls.playFromMediaId("1", null)
    }
}