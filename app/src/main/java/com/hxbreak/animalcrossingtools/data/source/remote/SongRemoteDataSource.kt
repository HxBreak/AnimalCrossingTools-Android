package com.hxbreak.animalcrossingtools.data.source.remote

import androidx.lifecycle.LiveData
import com.hxbreak.animalcrossingtools.data.Result
import com.hxbreak.animalcrossingtools.data.SongSaved
import com.hxbreak.animalcrossingtools.data.services.AnimalCrossingServices
import com.hxbreak.animalcrossingtools.data.source.entity.Song
import com.hxbreak.animalcrossingtools.data.source.local.SongSavedDao

class SongRemoteDataSource(
    val services: AnimalCrossingServices,
    val songSavedDao: SongSavedDao
) : SongDataSource {
    override fun getAllSongs(): LiveData<Result<Map<String, Song>>> {
        return services.allSongs()
    }

    override suspend fun updateSongs(songs: List<SongSaved>) {

    }

    override suspend fun getAllSavedSongs(): List<SongSaved> {
        return songSavedDao.getAllSongSaved()
    }
}
