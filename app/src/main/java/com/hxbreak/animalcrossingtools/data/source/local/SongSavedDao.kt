package com.hxbreak.animalcrossingtools.data.source.local

import androidx.room.*
import com.hxbreak.animalcrossingtools.data.SongSaved

@Dao
interface SongSavedDao {

    @Query("select * from song")
    suspend fun getAllSongSaved(): List<SongSaved>

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateSongSaved(fish: List<SongSaved>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongSaved(songs: List<SongSaved>)
}