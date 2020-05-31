package com.hxbreak.animalcrossingtools

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.hxbreak.animalcrossingtools.di.ApplicationModule
import com.hxbreak.tracker_proto.data.ConnectedClient
import javax.inject.Inject

class TempViewModel @Inject constructor(
    @ApplicationModule.AndroidId id: String
) : ViewModel() {
    companion object {
        const val OPEN_SERVER_ADDR = "120.79.4.153"
    }

    val onlines = MutableLiveData<List<ConnectedClient>>()
    val lastRecvTime = MutableLiveData(0L)
    val isConnected = onlines.map {
        (System.currentTimeMillis() - (lastRecvTime.value ?: 0)) < (1000 * 3)
    }
    val peerConnected = MutableLiveData(false)
    val peerLastData = MutableLiveData<String>()
    val peerInfomation = MutableLiveData<ConnectedClient>()

}
