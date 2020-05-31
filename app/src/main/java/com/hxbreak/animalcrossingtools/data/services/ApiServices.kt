package com.hxbreak.animalcrossingtools.data.services

import com.hxbreak.animalcrossingtools.data.Result
import com.hxbreak.animalcrossingtools.data.source.entity.FishEntity
import retrofit2.http.GET


interface AnimalCrossingServiceV2 {

    @GET("fish/")
    suspend fun allFish(): Result<Map<String, FishEntity>>
}