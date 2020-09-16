package com.hxbreak.animalcrossingtools.data.services

import com.hxbreak.animalcrossingtools.data.Result
import com.hxbreak.animalcrossingtools.data.source.entity.*
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

    @GET("sea/")
    suspend fun allSeaCreatures(): Result<Map<String, SeaCreatureEntity>>

    @GET("fossils/")
    suspend fun allFossils(): Result<Map<String, FossilEntity>>

    @GET("houseware/")
    suspend fun allHousewares(): Result<Map<String, List<HousewareEntity>>>
}