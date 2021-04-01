package com.hxbreak.animalcrossingtools.ui.fish

import android.annotation.SuppressLint
import androidx.paging.LoadType
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.hxbreak.animalcrossingtools.data.FishSaved
import com.hxbreak.animalcrossingtools.data.Result
import com.hxbreak.animalcrossingtools.data.source.AnimalCrossingDatabase
import com.hxbreak.animalcrossingtools.data.source.DataRepository
import com.hxbreak.animalcrossingtools.data.source.entity.FishEntity
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import timber.log.Timber

class FishRemoteMediator(
    private val repository: DataRepository,
) : RemoteMediator<Int, FishEntity>() {

    val dao = repository.local().fishDao()

    override suspend fun initialize(): InitializeAction {
        return InitializeAction.LAUNCH_INITIAL_REFRESH
    }

    @SuppressLint("RestrictedApi")
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, FishEntity>
    ): MediatorResult {
        return when(val result = repository.repoSource().allNetworkFish()){
            is Result.Success -> {
                repository.local().withTransaction {
                    dao.insertAllFishEntity(result.data)
                }
                MediatorResult.Success(true)
            }
            is Result.Error -> {
                MediatorResult.Error(result.exception)
            }
        }
    }
}