package com.hxbreak.animalcrossingtools.data.services

import com.hxbreak.animalcrossingtools.data.Result
import com.hxbreak.animalcrossingtools.data.source.entity.ArtEntity
import com.hxbreak.animalcrossingtools.data.source.entity.BugEntity
import com.hxbreak.animalcrossingtools.data.source.entity.FishEntity
import com.hxbreak.animalcrossingtools.data.source.entity.VillagerEntity
import retrofit2.http.GET


interface AnimalCrossingServiceV2 {

    @GET("fish/")
    suspend fun allFish(): Result<Map<String, FishEntity>>

    @GET("villagers/")
    suspend fun allVillagers(): Result<Map<String, VillagerEntity>>

    @GET("art/")
    suspend fun allArts(): Result<Map<String, ArtEntity>>

    @GET("bugs/")
    suspend fun allBugs(): Result<Map<String, BugEntity>>

}