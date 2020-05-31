package com.hxbreak.animalcrossingtools.data.source.local

import androidx.lifecycle.LiveData
import com.hxbreak.animalcrossingtools.data.FishAddictionPart
import com.hxbreak.animalcrossingtools.data.FishSaved
import com.hxbreak.animalcrossingtools.data.Result
import com.hxbreak.animalcrossingtools.data.source.entity.FishEntity
import com.hxbreak.animalcrossingtools.data.source.entity.FishEntityMix

interface FishDataSource {

//    fun observeAllFish(): LiveData<Result<List<Fish>>>
//
suspend fun updateFish(fish: List<FishSaved>)
//
//    suspend fun getAllFish(): Result<List<Fish>>

    suspend fun allFish(): Result<List<FishEntityMix>>

    suspend fun loadAllSaved(): List<FishSaved>

}