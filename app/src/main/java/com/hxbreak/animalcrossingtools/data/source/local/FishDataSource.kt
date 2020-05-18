package com.hxbreak.animalcrossingtools.data.source.local

import androidx.lifecycle.LiveData
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hxbreak.animalcrossingtools.data.Fish
import com.hxbreak.animalcrossingtools.data.FishAddictionPart
import com.hxbreak.animalcrossingtools.data.Result

interface FishDataSource {

    fun observeAllFish(): LiveData<Result<List<Fish>>>

    suspend fun updateFish(fish: List<FishAddictionPart>)

    suspend fun getAllFish(): Result<List<Fish>>

//    suspend fun insertFish(fish: Fish)

}