package com.hxbreak.animalcrossingtools.ui.fish

import androidx.lifecycle.*
import com.hxbreak.animalcrossingtools.data.Fish
import com.hxbreak.animalcrossingtools.data.FishAddictionPart
import com.hxbreak.animalcrossingtools.data.Result
import com.hxbreak.animalcrossingtools.data.source.DataRepository
import kotlinx.coroutines.launch
import java.util.*

data class SelectableFish(var selected: Boolean, val fish: Fish)

class CombinedLiveData<T, K, S>(
    source1: LiveData<T>,
    source2: LiveData<K>,
    private val combine: (data1: T?, data2: K?) -> S
) : MediatorLiveData<S>() {

    private var data1: T? = null
    private var data2: K? = null

    init {
        super.addSource(source1) {
            data1 = it
            value = combine(data1, data2)
        }
        super.addSource(source2) {
            data2 = it
            value = combine(data1, data2)
        }
    }

    override fun <T : Any?> removeSource(toRemote: LiveData<T>) {
        throw UnsupportedOperationException()
    }
}


class FishViewModel(
    private val repository: DataRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val refresh = MutableLiveData(false)
    val items: LiveData<List<Fish>> = refresh.switchMap { forceUpdate ->
        repository.observeAllFish().switchMap { filterFish(it) }
    }

    val found = items.map {
        "${it.count { it.owned }}/${it.size}"
    }

    val donated = items.map {
        "${it.count { it.donated }}/${it.size}"
    }

    val active = items.map {
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
        val actives = it.count { i ->
            booleanArrayOf(
                i.jan, i.feb, i.mar, i.apr, i.may, i.jun, i.jul, i.aug, i.sep,
                i.oct, i.nov, i.dec
            ).getOrElse(currentMonth) { false }
        }
        "$actives/${it.size}"
    }


    private val selected = MutableLiveData<List<Fish>>()
    val selectedFish: LiveData<List<Fish>> = selected
    val editMode = MutableLiveData<Boolean>(false)

    val donateAction = selected.map { !it.any { it.donated } }
    val bookmarkAction = selected.map { !it.any { it.owned } }


    val combinedLiveData = CombinedLiveData(selected, items) { x, y ->
        y?.map {
            //            SelectableFish(x?.contains(it) ?: false, it)
            SelectableFish(x?.any { fish -> fish.name.equals(it.name) } ?: false, it)
        }
    }

    val isDataLoadingError = MutableLiveData(false)

    private fun filterFish(fishResult: Result<List<Fish>>): LiveData<List<Fish>> {
        val result = MutableLiveData<List<Fish>>()
        if (fishResult is Result.Success) {
            isDataLoadingError.value = false
            viewModelScope.launch {
                result.value = fishResult.data
                /**
                 * refresh selected data, when new data coming
                 */
                selected.value = fishResult.data.filter { f ->
                    selected.value?.any { f.name == it.name } ?: false
                }
            }
        } else {
            isDataLoadingError.value = true
        }
        return result
    }

    init {
        loadFish()
    }

    private fun loadFish() {
        refresh.value = false//trigger of load
    }

    fun toggleFish(fish: Fish) {
        val oldSel = selected.value
        val newSelected = arrayListOf<Fish>()
        oldSel?.let {
            newSelected.addAll(it)
        }
        if (newSelected.contains(fish)) {
            newSelected.remove(fish)
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
                FishAddictionPart(
                    it.name,
                    toValue,
                    it.donated,
                    it.quantity
                )
            }
            repository.updateFish(modifyStatus)
            loadFish()
        }
    }

    fun toggleBookmark() {
        if (selectedFish.value.isNullOrEmpty()) return
        val toValue = donateAction.value!!
        viewModelScope.launch {
            val modifyStatus = selectedFish.value!!.map {
                FishAddictionPart(
                    it.name,
                    it.owned,
                    toValue,
                    it.quantity
                )
            }
            repository.updateFish(modifyStatus)
            loadFish()
        }
    }

    fun clearSelected() {
        if (!selected.value.isNullOrEmpty())
            selected.value = emptyList()
    }
}
