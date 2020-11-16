package com.hxbreak.animalcrossingtools.ui.fish

import android.util.Pair
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.hxbreak.animalcrossingtools.GlideProgressCollector
import com.hxbreak.animalcrossingtools.adapter.ItemComparable
import com.hxbreak.animalcrossingtools.character.CharUtil
import com.hxbreak.animalcrossingtools.combinedLiveData
import com.hxbreak.animalcrossingtools.data.FishSaved
import com.hxbreak.animalcrossingtools.data.Result
import com.hxbreak.animalcrossingtools.data.prefs.Hemisphere
import com.hxbreak.animalcrossingtools.data.prefs.PreferenceStorage
import com.hxbreak.animalcrossingtools.data.source.DataRepository
import com.hxbreak.animalcrossingtools.data.source.entity.FishEntityMix
import com.hxbreak.animalcrossingtools.extensions.previousValue
import com.hxbreak.animalcrossingtools.fragment.Event
import com.hxbreak.animalcrossingtools.i18n.toLocaleName
import kotlinx.coroutines.*
import java.lang.IllegalStateException
import java.util.*
import javax.inject.Inject

data class SelectableFishEntity(var selected: Boolean, val fish: FishEntityMix): ItemComparable<Int>{
    override fun id() = fish.fish.id
}

class FishViewModel @ViewModelInject constructor(
    private val repository: DataRepository,
    val preferenceStorage: PreferenceStorage,
    val collector: GlideProgressCollector,
) : ViewModel() {

    val locale = preferenceStorage.selectedLocale
    val hemisphere = preferenceStorage.selectedHemisphere

    val refresh = MutableLiveData(false)
    val loading = MutableLiveData(false)
    val error = MutableLiveData<Pair<Exception, () -> Unit>?>()


    private val items = refresh.switchMap { forceUpdate ->
        loading.value = true
        liveData(viewModelScope.coroutineContext + Dispatchers.IO) {
            val result = repository.fishSource().allFish()
            loading.postValue(false)

            when (result) {
                is Result.Success -> {
                    withContext(Dispatchers.Main){
                        error.value = null
                    }
                    result.data.forEach {
                        it.fish.localeName = it.fish.name.toLocaleName(locale)
                    }
                    emit(result.data)
                }
                is Result.Error -> handleError(result.exception) {
                    refresh.postValue(forceUpdate)
                }
            }
        }
    }

    private val localChanged = refresh.map { it } as MutableLiveData

    private val savedList = localChanged.switchMap {
        liveData(viewModelScope.coroutineContext + Dispatchers.IO) {
            emit(repository.fishSource().loadAllSaved())
        }
    }

    private val itemsWithLocalData = combinedLiveData(
        viewModelScope.coroutineContext + Dispatchers.IO,
        x = savedList,
        y = items
    ) { saved, fetched ->
        if (saved != null && fetched != null) {
            val value = fetched.map {
                FishEntityMix(it.fish, saved.firstOrNull { save -> save.id == it.fish.id })
            }
            emit(value)
        }
    }

    private fun handleError(exception: Exception, retry: () -> Unit) {
        error.postValue(Pair.create(exception, retry))
    }

    val selected = MutableLiveData<List<FishEntityMix>>(emptyList())

    private val internalClickedFishId = MutableLiveData(-1)

    val clickedFish = combinedLiveData(x = internalClickedFishId, y = itemsWithLocalData,
            runCheck = { x, y -> x }) { x, y ->
            if (x == -1) {
                emit(Event(null))
            } else {
                emit(Event(y?.firstOrNull { it.fish.id == x }))
            }
        }

    val editMode = MutableLiveData<Boolean>(false)

    val donateAction = combinedLiveData(
        viewModelScope.coroutineContext + Dispatchers.IO,
        x = savedList, y = selected
    ) { saved, sel ->
        val ids = sel.orEmpty().map { it.fish.id }
        val value = !ids.any { id -> saved.orEmpty().firstOrNull { it.id == id }?.donated ?: false }
        emit(value)
    }


    val foundAction = combinedLiveData(
        viewModelScope.coroutineContext + Dispatchers.IO,
        x = savedList, y = selected
    ) { saved, sel ->
        val ids = sel.orEmpty().map { it.fish.id }
        val value = !ids.any { id -> saved.orEmpty().firstOrNull { it.id == id }?.owned ?: false }
        emit(value)
    }

    val data: LiveData<List<SelectableFishEntity>> =
        combinedLiveData(viewModelScope.coroutineContext + Dispatchers.IO,
            x = itemsWithLocalData, y = selected, runCheck = { x, y -> x }) { items, sel ->
            val value = items.orEmpty().map { item ->
                SelectableFishEntity(
                    sel.orEmpty().any { it.fish.id == item.fish.id },
                    item
                )
            }
            .sortedBy { CharUtil.headPinyin(it.fish.fish.name.nameCNzh) }
            emit(value)
        }

    val found = data.map {
        "${it.count { it.fish.saved?.owned ?: false }}/${it.size}"
    }

    val donated = data.map {
        "${it.count { it.fish.saved?.donated ?: false }}/${it.size}"
    }

    val active = items.map {
        val timeInNow = preferenceStorage.timeInNow
        val currentMonth = timeInNow.monthValue.toShort()
        val currentHour = timeInNow.hour.toShort()
        val actives = it.count {
            val availability = it.fish.availability
            availability.requireTimeArray().contains(currentHour) && ((if (hemisphere == Hemisphere.Southern)
                availability.monthArraySouthern
            else
                availability.monthArrayNorthern)
                .contains(currentMonth))
        }
        "$actives/${it.size}"
    }

    fun toggleFish(fish: FishEntityMix) {
        val oldSel = selected.value
        val newSelected = arrayListOf<FishEntityMix>()
        oldSel?.let {
            newSelected.addAll(it)
        }
        val target = newSelected.firstOrNull { it.fish.id == fish.fish.id }
        if (target != null) {
            newSelected.remove(target)
        } else {
            newSelected.add(fish)
        }
        selected.value = newSelected
    }

    fun toggleFounded() {
        if (selected.value.isNullOrEmpty()) return
        val toValue = foundAction.value!!
        viewModelScope.launch {
            val fish = data.value.orEmpty().filter { it.selected }.map { it.fish }
            val modifyStatus = fish.map {
                FishSaved(
                    it.fish.id,
                    toValue,
                    it.saved?.donated ?: false,
                    it.saved?.quantity ?: 0
                )
            }
            repository.fishSource().updateFish(modifyStatus)
            localChanged.postValue(true)
        }
    }

    fun toggleDonate() {
        if (selected.value.isNullOrEmpty()) return
        val toValue = donateAction.value!!
        viewModelScope.launch {
            val fish = data.value.orEmpty().filter { it.selected }.map { it.fish }
            val modifyStatus = fish.map {
                FishSaved(
                    it.fish.id,
                    it.saved?.owned ?: false,
                    toValue,
                    it.saved?.quantity ?: 0
                )
            }
            repository.fishSource().updateFish(modifyStatus)
            localChanged.postValue(true)
        }
    }

    fun clearSelected() {
        if (!selected.value.isNullOrEmpty())
            selected.value = emptyList()
    }

    fun fishOnClick(id: Int) {
        if (id == internalClickedFishId.value) {
            internalClickedFishId.value = -1
        } else {
            internalClickedFishId.value = id
        }
    }
}