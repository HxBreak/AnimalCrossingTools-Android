package com.hxbreak.animalcrossingtools.ui.fish

import androidx.lifecycle.*
import com.hxbreak.animalcrossingtools.data.FishAddictionPart
import com.hxbreak.animalcrossingtools.data.Result
import com.hxbreak.animalcrossingtools.data.source.DataRepository
import com.hxbreak.animalcrossingtools.data.source.entity.FishEntityMix
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.IllegalStateException
import java.util.*
import javax.inject.Inject

data class SelectableFishEntity(var selected: Boolean, val fish: FishEntityMix)

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


class FishViewModel @Inject constructor(
    private val repository: DataRepository
) : ViewModel() {
    private val refresh = MutableLiveData(false)
    val items: LiveData<List<FishEntityMix>> = refresh.switchMap { forceUpdate ->
        liveData(viewModelScope.coroutineContext + Dispatchers.IO) {
            val result = repository.fishSource().allFish()
            isDataLoadingError.postValue(result !is Result.Success)
            when (result) {
                is Result.Success -> emit(result.data)
                is Result.Error -> handleError(result.exception)
                else -> throw IllegalStateException("Unsupported State $result")
            }
//            emitSource(repository.observeAllFish().switchMap {
//                liveData (viewModelScope.coroutineContext + Dispatchers.IO){
//                    withContext(viewModelScope.coroutineContext){
//                        isDataLoadingError.value = it !is Result.Success
//                    }
//                    emitSource(filterFish(it))
//                }
//            }
//            )
        }
    }

    private fun handleError(exception: Exception) {

    }

    val found = items.map {
        "0"
//        "${it.count { it.owned }}/${it.size}"
    }

    val donated = items.map {
        //        "${it.count { it.donated }}/${it.size}"
        "0"
    }

    val active = items.map {
        //        val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
//        val actives = it.count { i ->
//            booleanArrayOf(
//                i.jan, i.feb, i.mar, i.apr, i.may, i.jun, i.jul, i.aug, i.sep,
//                i.oct, i.nov, i.dec
//            ).getOrElse(currentMonth) { false }
//        }
//        "$actives/${it.size}"
        "0"
    }


    private val selected = MutableLiveData<List<FishEntityMix>>()

    /**
     * expose
     */
    val selectedFish: LiveData<List<FishEntityMix>> = selected
    val editMode = MutableLiveData<Boolean>(false)

    val donateAction = selected.map { !it.any { it.saved?.donated ?: false } }
    val bookmarkAction = selected.map { !it.any { it.saved?.owned ?: false } }


//    val combinedLiveData = CombinedLiveData(selected, items) { x, y ->
//        y?.map {
//            //            SelectableFish(x?.contains(it) ?: false, it)
//            SelectableFishEntity(x?.any { fish -> fish.name.equals(it.name) } ?: false, it)
//        }
//    }

    val data = MediatorLiveData<List<SelectableFishEntity>>().apply {
        var tmpSelected: List<FishEntityMix>? = Collections.emptyList()
        var tmpItems: List<FishEntityMix>? = null
        fun emit() {
            viewModelScope.launch(Dispatchers.IO) {
                tmpItems?.let { itemList ->
                    val tempSel = tmpSelected.orEmpty()
                    val value = itemList.map { i ->
                        SelectableFishEntity(
                            tempSel.any { it.fish.id == i.fish.id },
                            i
                        )
                    }
                    postValue(value)
                }
            }
        }
        addSource(selected) {
            tmpSelected = it
            emit()
        }
        addSource(items) {
            tmpItems = it
            emit()
        }
    }

    val isDataLoadingError = MutableLiveData(false)

//    private fun filterFish(fishResult: Result<List<Fish>>): LiveData<List<Fish>> {
//        val result = MutableLiveData<List<Fish>>()
//        if (fishResult is Result.Success) {
////            isDataLoadingError.value = false
//            viewModelScope.launch {
//                result.value = fishResult.data
//                /**
//                 * refresh selected data, when new data coming
//                 */
//                selected.value = fishResult.data.filter { f ->
//                    selected.value?.any { f.name == it.name } ?: false
//                }
//            }
//        } else {
////            isDataLoadingError.value = true
//        }
//        return result
//    }

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
        val target = newSelected.firstOrNull { it.fish.id == it.fish.id }
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
            //            val modifyStatus = selectedFish.value!!.map {
//                FishAddictionPart(
//                    it.name,
//                    toValue,
//                    it.donated,
//                    it.quantity
//                )
//            }
//            repository.updateFish(modifyStatus)
            loadFish()
        }
    }

    fun toggleBookmark() {
        if (selectedFish.value.isNullOrEmpty()) return
        val toValue = donateAction.value!!
//        viewModelScope.launch {
//            val modifyStatus = selectedFish.value!!.map {
//                FishAddictionPart(
//                    it.name,
//                    it.owned,
//                    toValue,
//                    it.quantity
//                )
//            }
//            repository.updateFish(modifyStatus)
//            loadFish()
//        }
    }

    fun clearSelected() {
        if (!selected.value.isNullOrEmpty())
            selected.value = emptyList()
    }
}
