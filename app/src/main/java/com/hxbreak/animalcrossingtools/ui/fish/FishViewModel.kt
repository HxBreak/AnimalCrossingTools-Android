package com.hxbreak.animalcrossingtools.ui.fish

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import androidx.paging.*
import androidx.room.withTransaction
import com.hxbreak.animalcrossingtools.GlideProgressCollector
import com.hxbreak.animalcrossingtools.data.FishSaved
import com.hxbreak.animalcrossingtools.data.prefs.DataUsageStorage
import com.hxbreak.animalcrossingtools.data.prefs.Hemisphere
import com.hxbreak.animalcrossingtools.data.prefs.PreferenceStorage
import com.hxbreak.animalcrossingtools.data.source.DataRepository
import com.hxbreak.animalcrossingtools.data.source.entity.FishEntityMix
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class FishViewModel @ViewModelInject constructor(
    private val repository: DataRepository,
    val preferenceStorage: PreferenceStorage,
    private val dataUsageStorage: DataUsageStorage,
    val collector: GlideProgressCollector,
) : ViewModel() {

    val locale = preferenceStorage.selectedLocale
    val hemisphere = preferenceStorage.selectedHemisphere

    val refresh = MutableLiveData(false)
    val loading = MutableLiveData(false)

    private val dao = repository.local().fishDao()

    private val paging = Pager(PagingConfig(Int.MAX_VALUE, enablePlaceholders = false),
        null, FishRemoteMediator(repository)){
        dao.paging()
    }.flow.cachedIn(viewModelScope)

    private val savedLiveData = dao.allSavedLiveData()
    val saved = savedLiveData.asFlow()

    val selectedIds = MutableStateFlow<List<Long>>(emptyList())

    val pagingFish = combine(paging, saved){ p, saved ->
        p.map { entity ->
            val mix = FishEntityMix(entity, saved.firstOrNull { entity.id == it.id })
            mix
        }
    }

    val editMode = MutableLiveData(false)

    val donateAction = combine(selectedIds, saved){ idList, saved ->
        !idList.any { id -> saved.firstOrNull { it.id.toLong() == id }?.donated ?: false }
    }.flowOn(Dispatchers.Default).shareIn(viewModelScope, SharingStarted.Lazily, replay = 1)

    val foundAction = combine(selectedIds, saved){ idList, saved ->
        !idList.any { id -> saved.firstOrNull { it.id.toLong() == id }?.owned ?: false }
    }.flowOn(Dispatchers.Default).shareIn(viewModelScope, SharingStarted.Lazily, replay = 1)

    private val allFish = dao.allFishLiveData().asFlow()

    val found = allFish.combine(saved){ fishList, savedList ->
        "${savedList.count { it.owned }}/${fishList.size}"
    }.flowOn(Dispatchers.Default)

    val donated = allFish.combine(saved){ fishList, savedList ->
        "${savedList.count { it.donated }}/${fishList.size}"
    }.flowOn(Dispatchers.Default)


    val active = allFish.map {
        val timeInNow = preferenceStorage.timeInNow
        val currentMonth = timeInNow.monthValue.toShort()
        val currentHour = timeInNow.hour.toShort()
        val actives = it.count {
            val availability = it.availability
            availability.requireTimeArray().contains(currentHour) && ((if (hemisphere == Hemisphere.Southern)
                availability.monthArraySouthern
            else
                availability.monthArrayNorthern)
                .contains(currentMonth))
        }
        "$actives/${it.size}"
    }.flowOn(Dispatchers.IO)

    fun selectFish(id: Long, isSelect: Boolean) {
        viewModelScope.launch {
            val v = selectedIds.value
            if (v.contains(id)){
                if (!isSelect){
                    selectedIds.compareAndSet(v, v.filter { it != id })
                }
            }else{
                if (isSelect){
                    val newArr = mutableListOf<Long>()
                    newArr.addAll(v)
                    newArr.add(id)
                    selectedIds.compareAndSet(v, newArr)
                }
            }
        }
    }

    private suspend fun compressState(ids: List<Long>, map: (List<FishSaved>, Long) -> FishSaved){
        val list = savedLiveData.value.orEmpty()
        val changeList = ids.map { id ->
            map(list, id)
        }
        repository.local().withTransaction {
            dao.insertAllSaved(changeList)
        }
    }

    fun toggleFounded(ids: List<Long>) {
        viewModelScope.launch {
            val toValue = foundAction.replayCache.first()
            compressState(ids){ list, id ->
                list.firstOrNull { it.id.toLong() == id }?.copy(owned = toValue) ?: FishSaved(id.toInt(), owned = toValue)
            }
        }
    }

    fun toggleDonate(ids: List<Long>) {
        viewModelScope.launch {
            val toValue = donateAction.replayCache.first()
            compressState(ids){ list, id ->
                list.firstOrNull { it.id.toLong() == id }?.copy(donated = toValue) ?: FishSaved(id.toInt(), donated = toValue)
            }
        }
    }

}