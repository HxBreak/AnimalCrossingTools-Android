package com.hxbreak.animalcrossingtools.data.source

import androidx.lifecycle.LiveData
import com.hxbreak.animalcrossingtools.data.FishAddictionPart
import com.hxbreak.animalcrossingtools.data.Result
import com.hxbreak.animalcrossingtools.data.SongSaved
import com.hxbreak.animalcrossingtools.data.source.entity.Song
import com.hxbreak.animalcrossingtools.data.source.entity.FishEntity
import com.hxbreak.animalcrossingtools.data.source.local.FishDataSource
import kotlinx.coroutines.*
import javax.inject.Inject

class DefaultDataRepository @Inject constructor(
    private val source: AnimalCrossingDataSource,
    private val localDatabase: AnimalCrossingDatabase,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : DataRepository() {
    override fun local() = localDatabase

    override fun fishSource(): FishDataSource {
        return source.fishSource()
    }

//    override fun observeAllFish(): LiveData<Result<List<Fish>>> {
//        return source.fishSource().observeAllFish()
//    }
//
//    override suspend fun updateFish(fish: List<FishAddictionPart>) {
//        source.fishSource().updateFish(fish)
//    }
//
//    override suspend fun getAllFish(): Result<List<Fish>> {
//        return source.fishSource().getAllFish()
//    }


    override fun getAllSongs(): LiveData<Result<Map<String, Song>>> {
        return source.songSource().getAllSongs()
    }

    override suspend fun updateSongs(songs: List<SongSaved>) {
        return source.songSource().updateSongs(songs)
    }

    override suspend fun getAllSavedSongs(): List<SongSaved> {
        return source.songSource().getAllSavedSongs()
    }
}