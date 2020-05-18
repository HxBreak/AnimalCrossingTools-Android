package com.hxbreak.animalcrossingtools.data.source.remote

import androidx.lifecycle.LiveData
import com.hxbreak.animalcrossingtools.data.Result
import com.hxbreak.animalcrossingtools.data.SongSaved
import com.hxbreak.animalcrossingtools.data.services.Song

interface SongDataSource {

    fun getAllSongs(): LiveData<Result<Map<String, Song>>>

    suspend fun updateSongs(songs: List<SongSaved>)

    suspend fun getAllSavedSongs(): List<SongSaved>

}