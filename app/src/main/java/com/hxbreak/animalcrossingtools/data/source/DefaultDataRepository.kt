package com.hxbreak.animalcrossingtools.data.source

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.hxbreak.animalcrossingtools.data.Fish
import com.hxbreak.animalcrossingtools.data.FishAddictionPart
import com.hxbreak.animalcrossingtools.data.Result
import com.hxbreak.animalcrossingtools.data.SongSaved
import com.hxbreak.animalcrossingtools.data.services.Song
import kotlinx.coroutines.*

class DefaultDataRepository(
    private val localSource: AnimalCrossingDataSource,
    private val localDatabase: AnimalCrossingDatabase,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : DataRepository() {
    override fun local() = localDatabase

    override fun observeAllFish(): LiveData<Result<List<Fish>>> {
        return localSource.fishSource().observeAllFish()
    }

    override suspend fun updateFish(fish: List<FishAddictionPart>) {
        localSource.fishSource().updateFish(fish)
    }

    override suspend fun getAllFish(): Result<List<Fish>> {
        return localSource.fishSource().getAllFish()
    }

    override fun getAllSongs(): LiveData<Result<Map<String, Song>>> {
        return localSource.songSource().getAllSongs()
    }

    override suspend fun updateSongs(songs: List<SongSaved>) {
        return localSource.songSource().updateSongs(songs)
    }

    override suspend fun getAllSavedSongs(): List<SongSaved> {
        return localSource.songSource().getAllSavedSongs()
    }

}