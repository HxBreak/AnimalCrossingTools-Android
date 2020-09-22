package com.hxbreak.animalcrossingtools

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.lang.Exception
import java.util.concurrent.ConcurrentHashMap

class GlideProgressCollector : GlideAppFetcherListener {

    private val map = ConcurrentHashMap<String, GlideProgress>()
    private val callback = ConcurrentHashMap<String, MutableLiveData<GlideProgress.Loading>>()
    private val mLock = Any()

    override fun start(key: String) {
        map[key] = GlideProgress.READY
    }

    override fun update(key: String, progress: GlideProgress.Loading) {
        map[key] = progress
        val livedata = callback[key] ?: return
        livedata.postValue(progress)
        callback[key] = livedata
    }

    override fun end(key: String, progress: GlideProgress) {
        map.remove(key)
        callback.remove(key)
    }

    operator fun get(key: String): LiveData<GlideProgress.Loading> {
        synchronized(mLock) {
            val livedata = callback[key] ?: MutableLiveData()
            map[key]?.let {
                if (it is GlideProgress.Loading) {
                    livedata.postValue(it)
                }
            }
            callback[key] = livedata
            return livedata
        }
    }

}

sealed class GlideProgress {

    object READY : GlideProgress()

    object DONE : GlideProgress()

    data class Loading(val read: Long, val totalBytes: Long) : GlideProgress()

    data class Error(val exception: Exception) : GlideProgress()

}

fun GlideProgress.Loading.text() = String.format("%.2f", read.toFloat() / totalBytes.toFloat())
