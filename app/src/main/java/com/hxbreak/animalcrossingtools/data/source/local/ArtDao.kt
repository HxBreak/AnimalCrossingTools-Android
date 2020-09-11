package com.hxbreak.animalcrossingtools.data.source.local

import androidx.room.*
import com.hxbreak.animalcrossingtools.data.ArtSaved

@Dao
interface ArtDao {

    @Query("select * from art")
    suspend fun getAllArtSaved(): List<ArtSaved>

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateArtSaved(arts: List<ArtSaved>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArtSaved(arts: List<ArtSaved>)
}