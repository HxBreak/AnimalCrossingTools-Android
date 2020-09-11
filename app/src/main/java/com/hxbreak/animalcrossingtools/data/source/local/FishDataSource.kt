package com.hxbreak.animalcrossingtools.data.source.local

import com.hxbreak.animalcrossingtools.data.FishSaved
import com.hxbreak.animalcrossingtools.data.Result
import com.hxbreak.animalcrossingtools.data.source.entity.FishEntityMix

interface FishDataSource {

    suspend fun updateFish(fish: List<FishSaved>)

    suspend fun allFish(): Result<List<FishEntityMix>>

    suspend fun loadAllSaved(): List<FishSaved>

}