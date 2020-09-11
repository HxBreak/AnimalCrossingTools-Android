package com.hxbreak.animalcrossingtools.data.source.remote

import com.hxbreak.animalcrossingtools.data.Result
import com.hxbreak.animalcrossingtools.data.services.AnimalCrossingServiceV2
import com.hxbreak.animalcrossingtools.data.source.AnimalCrossingDatabase
import com.hxbreak.animalcrossingtools.data.source.entity.ArtEntity
import com.hxbreak.animalcrossingtools.data.source.entity.ArtEntityMix
import com.hxbreak.animalcrossingtools.data.source.entity.BugEntity
import com.hxbreak.animalcrossingtools.data.source.entity.VillagerEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import java.lang.IllegalStateException

class RepoDataSource(
    private val service: AnimalCrossingServiceV2,
    private val database: AnimalCrossingDatabase,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
){
    suspend fun allVillagers(): Result<List<VillagerEntity>> = when(val result = service.allVillagers()){
        is Result.Success -> Result.Success(result.data.values.toList())
        is Result.Error -> result
        else -> throw IllegalStateException("The result from service shouldn\'t using loading")
    }

    suspend fun allArts(): Result<List<ArtEntity>> = when(val result = service.allArts()){
        is Result.Success -> Result.Success(result.data.values.toList())
        is Result.Error -> result
        else -> throw IllegalStateException("The result from service shouldn\'t using loading")
    }

    suspend fun allBugs(): Result<List<BugEntity>> = when(val result = service.allBugs()){
        is Result.Success -> Result.Success(result.data.values.toList())
        is Result.Error -> result
        else -> throw IllegalStateException("The result from service shouldn\'t using loading")
    }
}