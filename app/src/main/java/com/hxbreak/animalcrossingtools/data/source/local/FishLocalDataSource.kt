package com.hxbreak.animalcrossingtools.data.source.local

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.hxbreak.animalcrossingtools.data.FishSaved
import com.hxbreak.animalcrossingtools.data.Result
import com.hxbreak.animalcrossingtools.data.services.AnimalCrossingServiceV2
import com.hxbreak.animalcrossingtools.data.source.entity.FishEntity
import com.hxbreak.animalcrossingtools.data.source.entity.FishEntityMix
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.Exception
import java.lang.IllegalStateException

class FishLocalDataSource internal constructor(
    private val fishDao: FishDao,
    private val service: AnimalCrossingServiceV2,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : FishDataSource {

    override suspend fun updateFish(fish: List<FishSaved>) {
        fishDao.insertFish(fish)
    }

    override suspend fun allFish(): Result<List<FishEntityMix>> {
        val savedList = fishDao.getAllFish()
        return when (val result = service.allFish()) {
            is Result.Success -> Result.Success(result.data.map {
                FishEntityMix(it.value, savedList.firstOrNull { x -> x.id == it.value.id })
            })
            is Result.Error -> result
            else -> throw IllegalStateException("The result from service shouldn\'t using loading")
        }
    }

    override suspend fun loadAllSaved(): List<FishSaved> {
        return fishDao.getAllFish()
    }

}