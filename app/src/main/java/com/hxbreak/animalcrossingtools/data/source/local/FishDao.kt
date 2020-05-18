package com.hxbreak.animalcrossingtools.data.source.local

import androidx.lifecycle.LiveData
import androidx.room.*
import com.hxbreak.animalcrossingtools.data.FishAddictionPart
import com.hxbreak.animalcrossingtools.data.Fish

@Dao
interface FishDao {

    @Query("select * from fish")
    fun observeAllFish(): LiveData<List<Fish>>

    @Query("select * from fish")
    suspend fun getAllFish(): List<Fish>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFish(fish: Fish)

    @Update(entity = Fish::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateFish(fish: List<FishAddictionPart>)
}