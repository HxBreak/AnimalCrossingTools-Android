package com.hxbreak.animalcrossingtools.data.source.local

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.hxbreak.animalcrossingtools.data.Fish
import com.hxbreak.animalcrossingtools.data.FishAddictionPart
import com.hxbreak.animalcrossingtools.data.Result
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.Exception

class FishLocalDataSource internal constructor(
    private val fishDao: FishDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : FishDataSource {

    override fun observeAllFish(): LiveData<Result<List<Fish>>> {
        return fishDao.observeAllFish().map {
            Result.Success(it)
        }
    }

    override suspend fun updateFish(fish: List<FishAddictionPart>) {
        fishDao.updateFish(fish)
    }

    override suspend fun getAllFish(): Result<List<Fish>> = withContext(ioDispatcher) {
        try {
            return@withContext Result.Success(fishDao.getAllFish())
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

}