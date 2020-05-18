package com.hxbreak.animalcrossingtools.data.source.remote

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.hxbreak.animalcrossingtools.data.Result
import com.hxbreak.animalcrossingtools.data.SongSaved
import com.hxbreak.animalcrossingtools.data.services.AnimalCrossingServices
import com.hxbreak.animalcrossingtools.data.services.Song
import com.hxbreak.animalcrossingtools.data.services.SongMix
import com.hxbreak.animalcrossingtools.data.source.local.SongSavedDao
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class SongRemoteDataSource(
    val services: AnimalCrossingServices,
    val songSavedDao: SongSavedDao
) : SongDataSource {
    override fun getAllSongs(): LiveData<Result<Map<String, Song>>> {
//        val result = MutableLiveData<Result<List<SongMix>>>()
//        var r: LiveData<Result<Map<String, Song>>>? = null
//        suspendCoroutine<Unit> { con ->
//            r = services.allSongs()
//            r?.observeForever { con.resume(Unit) }
//        }
//        val ret: Result<Map<String, Song>>? = r?.value
//        if (ret is Result.Success){
//
//        }else{
//
//        }
        return services.allSongs()
    }

    override suspend fun updateSongs(songs: List<SongSaved>) {

    }

    override suspend fun getAllSavedSongs(): List<SongSaved> {
        return songSavedDao.getAllSongSaved()
    }
}
