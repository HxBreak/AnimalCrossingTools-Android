package com.hxbreak.animalcrossingtools.ui.fish

import android.util.Pair
import androidx.lifecycle.*
import com.hxbreak.animalcrossingtools.character.CharUtil
import com.hxbreak.animalcrossingtools.combineLiveData
import com.hxbreak.animalcrossingtools.data.FishAddictionPart
import com.hxbreak.animalcrossingtools.data.FishSaved
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

    private val itemsWithLocalData = MediatorLiveData<List<FishEntityMix>>().apply {
        var tmpItems: List<FishEntityMix>? = null
        var tmpSaved: List<FishSaved>? = null

        fun emit() {
            viewModelScope.launch(Dispatchers.IO) {
                if (tmpItems != null && tmpSaved != null) {
                    val value = tmpItems.orEmpty().map {
                        FishEntityMix(
                            it.fish,
                            tmpSaved.orEmpty().firstOrNull { x -> x.id == it.fish.id })
                    }
                    postValue(value)
                }
            }
        }
        addSource(savedList) {
            tmpSaved = it.orEmpty()
            emit()
        }
        addSource(items) {
            tmpItems = it.orEmpty()
            emit()
        }
    }

    private fun handleError(exception: Exception, retry: () -> Unit) {
        error.postValue(Pair.create(exception, retry))
    }

    private val selected = MutableLiveData<List<FishEntityMix>>()

    val unused = combineLiveData(x = selected, y = itemsWithLocalData) { x, y ->
        emit(arrayListOf<String>())
    }

    /**
     * expose
     */
    val selectedFish = MediatorLiveData<List<FishEntityMix>>().apply {
        var ids: List<Int>? = null
        var tmpSaved: List<FishEntityMix>? = null

        fun emit() {
            if (tmpSaved != null && ids != null) {
                viewModelScope.launch(Dispatchers.IO) {
                    postValue(tmpSaved.orEmpty().filter { ids?.contains(it.fish.id) ?: false })
                }
            }
        }

        addSource(itemsWithLocalData) {
            tmpSaved = it.orEmpty()
            emit()
        }
        addSource(selected) {
            ids = it.orEmpty().map { it.fish.id }
            emit()
        }
    }
    val editMode = MutableLiveData<Boolean>(false)

    val donateAction = MediatorLiveData<Boolean>().apply {
        var ids: List<Int>? = null
        var tmpSaved: List<FishSaved>? = null

        fun emit() {
            if (tmpSaved != null && ids != null) {
                viewModelScope.launch(Dispatchers.IO) {
                    postValue(!ids.orEmpty().any { id ->
                        tmpSaved.orEmpty().firstOrNull { it.id == id }?.donated ?: false
                    })
                }
            }
        }

        addSource(savedList) {
            tmpSaved = it.orEmpty()
            emit()
        }
        addSource(selected) {
            ids = it.orEmpty().map { it.fish.id }
            emit()
        }
    }

    val bookmarkAction = MediatorLiveData<Boolean>().apply {
        var ids: List<Int>? = null
        var tmpSaved: List<FishSaved>? = null

        fun emit() {
            if (tmpSaved != null && ids != null) {
                viewModelScope.launch(Dispatchers.IO) {
                    postValue(!ids.orEmpty().any { id ->
                        tmpSaved.orEmpty().firstOrNull { it.id == id }?.owned ?: false
                    })
                }
            }
        }

        addSource(savedList) {
            tmpSaved = it.orEmpty()
            emit()
        }
        addSource(selected) {
            ids = it.orEmpty().map { it.fish.id }
            emit()
        }
    }

//    val bookmarkAction = selected.map { !it.any { it.saved?.owned ?: false } }

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
                        .sortedBy { CharUtil.headPinyin(it.fish.fish.name.nameCNzh) }
                    postValue(value)
                }
            }
        }
        addSource(selected) {
            tmpSelected = it
            emit()
        }
        addSource(itemsWithLocalData) {
            tmpItems = it
            emit()
        }
    }

    val found = data.map {
        "${it.count { it.fish.saved?.owned ?: false }}/${it.size}"
    }

    val donated = data.map {
        "${it.count { it.fish.saved?.donated ?: false }}/${it.size}"
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
        "Todo"
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
}
