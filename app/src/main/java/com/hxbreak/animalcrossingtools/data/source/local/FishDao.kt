package com.hxbreak.animalcrossingtools.data.source.local

import androidx.lifecycle.LiveData
import androidx.room.*
import com.hxbreak.animalcrossingtools.data.FishSaved
import com.hxbreak.animalcrossingtools.data.source.entity.FishEntity

@Dao
interface FishDao {

    @Query("select * from fish")
    suspend fun getAllFish(): List<FishSaved>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFish(fish: List<FishSaved>)

    @Query("select * from fish_entity")
    suspend fun allFishEntity(): List<FishEntity>

    @Query("select count(*) from fish_entity")
    suspend fun countFishEntity(): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllFishEntity(fish: List<FishEntity>)

}