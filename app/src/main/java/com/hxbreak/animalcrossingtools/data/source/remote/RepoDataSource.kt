package com.hxbreak.animalcrossingtools.data.source.remote

import com.hxbreak.animalcrossingtools.data.FishSaved
import com.hxbreak.animalcrossingtools.data.Result
import com.hxbreak.animalcrossingtools.data.services.AnimalCrossingServiceV2
import com.hxbreak.animalcrossingtools.data.source.AnimalCrossingDatabase
import com.hxbreak.animalcrossingtools.data.source.entity.*
import kotlinx.coroutines.*
import timber.log.Timber
import java.lang.IllegalStateException
import kotlin.time.measureTime

class RepoDataSource(
    val service: AnimalCrossingServiceV2,
    private val database: AnimalCrossingDatabase,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
){
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(ioDispatcher + serviceJob)

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

    suspend fun allSeaCreature(): Result<List<SeaCreatureEntity>> = when(val result = service.allSeaCreatures()){
        is Result.Success -> Result.Success(result.data.values.toList())
        is Result.Error -> result
        else -> throw IllegalStateException("The result from service shouldn\'t using loading")
    }

    suspend fun allFossils(): Result<List<FossilEntity>> = when(val result = service.allFossils()){
        is Result.Success -> Result.Success(result.data.values.toList())
        is Result.Error -> result
    }

    suspend fun allHousewares(): Result<Pair<Job, List<List<FurnitureEntity>>>> = when (val result = service.allHousewares()){
        is Result.Success -> {
            result.data.entries.forEach { entity ->
                entity.value.forEach { it.seriesId = entity.key }
            }
            val job = serviceScope.launch {
                val insertDatabaseTime = measureTime {
                    database.furnitureDao().insert(result.data.values.flatten())
                }.inMilliseconds
                Timber.e("use $insertDatabaseTime to Insert All Data")
            }
            Result.Success(job to result.data.values.toList())
        }
        is Result.Error -> Result.Error(result.exception)
        else -> throw IllegalStateException("The result from service shouldn\'t using loading")
    }

    suspend fun allLocalFish(): List<FishEntityMix> {
        val savedList = database.fishDao().allFishSaved()
        return database.fishDao().allFishEntity().map {
            FishEntityMix(it, savedList.firstOrNull { x -> x.id == it.id })
        }
    }

    suspend fun allFish(): Pair<Job?, Result<List<FishEntityMix>>> {
        when (val result = service.allFish()) {
            is Result.Success -> {
                val job = serviceScope.launch {
                    val insertDatabaseTime = measureTime {
                        database.fishDao().insertAllFishEntity(result.data.values.toList())
                    }.inMilliseconds
                    Timber.e("use $insertDatabaseTime to Insert All Data")
                }
                val savedList = database.fishDao().allFishSaved()
                val data = Result.Success(result.data.map {
                    FishEntityMix(it.value, savedList.firstOrNull { x -> x.id == it.value.id })
                })
                return job to data
            }
            is Result.Error -> return null to result
        }
    }

    suspend fun allNetworkFish(): Result<List<FishEntity>> {
        when (val result = service.allFish()) {
            is Result.Success -> {
                return Result.Success(result.data.values.toList())
            }
            is Result.Error -> return result
        }
    }

    suspend fun allFishSaved(): List<FishSaved> {
        return database.fishDao().allFishSaved()
    }
}