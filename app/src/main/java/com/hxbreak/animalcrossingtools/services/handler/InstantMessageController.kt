package com.hxbreak.animalcrossingtools.services.handler

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hxbreak.backend.BackendPacket
import io.netty.channel.Channel
import kotlinx.coroutines.*

class InstantMessageController {

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private val _lastException = MutableLiveData<Throwable>()
    private val _authorized = MutableLiveData(false)
    private val _lobbyList = MutableLiveData<List<BackendPacket.NetUserEntity>>()

    val authorized: LiveData<Boolean>
        get() = _authorized

    val lastException: LiveData<Throwable>
        get() = _lastException

    val lobbyList: LiveData<List<BackendPacket.NetUserEntity>>
        get() = _lobbyList

    /**
     * Init By InstantMessageHandler Remove By InstantMessageHandler
     */
    var mainChannel: Channel? = null
    set(value) {
        field = value
        if (value == null){
            serviceScope.launch {
                if (_authorized.value != false){
                    //clear up
                    _lobbyList.value = null
                    _authorized.value = false
                }
            }
        }
    }

    suspend fun sendMessageTo(toId: String, msg: String): Boolean {
        return withContext(serviceScope.coroutineContext){
            if ((mainChannel?.isActive == true) && (mainChannel?.isOpen == true)){
                if (_authorized.value == true){
                    return@withContext true
                }else{
                    handleException(Exception("User Is Not Authorized"))
                    return@withContext false
                }
            }
            false
        }
    }

    fun handleException(throwable: Throwable?){
        serviceScope.launch {
            _lastException.value = throwable
        }
    }

    fun updateOnlineList(list: List<BackendPacket.NetUserEntity>) {
        serviceScope.launch {
            _lobbyList.value = list
        }
    }

    fun userAuthorized() {
        serviceScope.launch {
            if (_authorized.value != true){
                _authorized.value = true
            }
        }
    }
}