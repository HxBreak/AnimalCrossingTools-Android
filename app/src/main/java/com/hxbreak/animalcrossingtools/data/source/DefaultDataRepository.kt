package com.hxbreak.animalcrossingtools.data.source

import androidx.lifecycle.LiveData
import com.hxbreak.animalcrossingtools.data.Result
import com.hxbreak.animalcrossingtools.data.SongSaved
import com.hxbreak.animalcrossingtools.data.source.entity.Song
import kotlinx.coroutines.*
import javax.inject.Inject

class DefaultDataRepository @Inject constructor(
    private val source: AnimalCrossingDataSource,
    private val localDatabase: AnimalCrossingDatabase,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : DataRepository() {
    override fun local() = localDatabase

    override fun repoSource() = source.repoSource()

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