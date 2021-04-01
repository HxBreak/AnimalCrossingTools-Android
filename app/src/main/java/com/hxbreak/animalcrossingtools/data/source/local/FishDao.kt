package com.hxbreak.animalcrossingtools.data.source.local

import androidx.lifecycle.LiveData
import androidx.paging.PagingSource
import androidx.room.*
import com.hxbreak.animalcrossingtools.data.FishSaved
import com.hxbreak.animalcrossingtools.data.source.entity.FishEntity

@Dao
interface FishDao {

    @Query("select * from fish")
    suspend fun allFishSaved(): List<FishSaved>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFish(fish: List<FishSaved>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllSaved(fish: List<FishSaved>)

    @Query("select * from fish_entity")
    suspend fun allFishEntity(): List<FishEntity>

    @Query("select count(*) from fish_entity")
    suspend fun countFishEntity(): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllFishEntity(fish: List<FishEntity>)

    @Query("select * from fish_entity")
    fun paging(): PagingSource<Int, FishEntity>

    @Query("select * from fish")
    fun allSavedLiveData(): LiveData<List<FishSaved>>

    @Query("select * from fish_entity")
    fun allFishLiveData(): LiveData<List<FishEntity>>

}