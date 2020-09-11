package com.hxbreak.animalcrossingtools.data.source.local

import androidx.lifecycle.LiveData
import androidx.room.*
import com.hxbreak.animalcrossingtools.data.FishSaved

@Dao
interface FishDao {

    @Query("select * from fish")
    suspend fun getAllFish(): List<FishSaved>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFish(fish: List<FishSaved>)

}