package com.hxbreak.animalcrossingtools.ui.fish

import android.util.Pair
import androidx.lifecycle.*
import com.hxbreak.animalcrossingtools.character.CharUtil
import com.hxbreak.animalcrossingtools.combinedLiveData
import com.hxbreak.animalcrossingtools.data.FishSaved
import com.hxbreak.animalcrossingtools.data.Result
import com.hxbreak.animalcrossingtools.data.source.DataRepository
import com.hxbreak.animalcrossingtools.data.source.entity.FishEntityMix
import kotlinx.coroutines.*
import java.lang.IllegalStateException
import java.util.*
import javax.inject.Inject

data class SelectableFishEntity(var selected: Boolean, val fish: FishEntityMix)

class FishViewModel @Inject constructor(
    private val repository: DataRepository
) : ViewModel() {

    val refresh = MutableLiveData(false)
    val loading = MutableLiveData(false)
    val error = MutableLiveData<Pair<Exception, () -> Unit>>()

    private val items: LiveData<List<FishEntityMix>> = refresh.switchMap { forceUpdate ->
        loading.value = true
        liveData(viewModelScope.coroutineContext + Dispatchers.IO) {
            val result = repository.fishSource().allFish()
            isDataLoadingError.postValue(result !is Result.Success)
            loading.postValue(false)

            when (result) {
                is Result.Success -> emit(result.data)
                is Result.Error -> handleError(result.exception) {
                    refresh.postValue(forceUpdate)
                }
                else -> throw IllegalStateException("Unsupported State $result")
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

    private val selected = MutableLiveData<List<FishEntityMix>>()

    private val internalClickedFishId = MutableLiveData<Int>(-1)
    val clickedFish = combinedLiveData(x = internalClickedFishId, y = itemsWithLocalData,
        runCheck = { x, y -> x }) { x, y ->
        if (x == -1) {
            emit(null)
        } else {
            emit(y?.firstOrNull { it.fish.id == x })
        }
    }

    /**
     * after
     */
    val selectedFish = combinedLiveData(context = Dispatchers.IO,
        x = itemsWithLocalData, y = selected,
        runCheck = { x, _ -> x }) { items, sel ->
        if (!(items == null || sel == null)) {
            val ids = sel.map { it.fish.id }
            emit(items.filter { ids.contains(it.fish.id) })
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


    val bookmarkAction = combinedLiveData(
        viewModelScope.coroutineContext + Dispatchers.IO,
        x = savedList, y = selected
    ) { saved, sel ->
        val ids = sel.orEmpty().map { it.fish.id }
        val value = !ids.any { id -> saved.orEmpty().firstOrNull { it.id == id }?.owned ?: false }
        emit(value)
    }

    val data = combinedLiveData(viewModelScope.coroutineContext + Dispatchers.IO,
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
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
        val actives = it.count {
            it.fish.availability.monthArraySouthern.contains(currentMonth)
        }
        "$actives/${it.size}"
    }

    val isDataLoadingError = MutableLiveData(false)

    init {
        loadFish()
    }

    private fun loadFish() {
        refresh.value = false//trigger of load
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
        if (selectedFish.value.isNullOrEmpty()) return
        val toValue = bookmarkAction.value!!
        viewModelScope.launch {
            val modifyStatus = selectedFish.value!!.map {
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

    fun toggleBookmark() {
        if (selectedFish.value.isNullOrEmpty()) return
        val toValue = donateAction.value!!
        viewModelScope.launch {
            val modifyStatus = selectedFish.value!!.map {
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
