package com.hxbreak.animalcrossingtools.ui.fossil

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.hxbreak.animalcrossingtools.adapter.ItemComparable
import com.hxbreak.animalcrossingtools.combinedLiveData
import com.hxbreak.animalcrossingtools.data.FossilSaved
import com.hxbreak.animalcrossingtools.data.Result
import com.hxbreak.animalcrossingtools.data.prefs.PreferenceStorage
import com.hxbreak.animalcrossingtools.data.source.DataRepository
import com.hxbreak.animalcrossingtools.data.source.entity.FossilEntity
import com.hxbreak.animalcrossingtools.data.source.entity.FossilEntityMix
import com.hxbreak.animalcrossingtools.extensions.previousValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception
import javax.inject.Inject

class FossilViewModel @ViewModelInject constructor(
    private val repository: DataRepository,
    private val preferenceStorage: PreferenceStorage
): ViewModel(){
    val locale = preferenceStorage.selectedLocale

    val editMode = MutableLiveData(false)
    val refresh = MutableLiveData(false)
    val error = MutableLiveData<Exception?>()
    val loading = MutableLiveData<Boolean>()

    private val fossilEntity = refresh.switchMap {
        loading.value = true
        liveData (viewModelScope.coroutineContext + Dispatchers.IO){
            when(val result = repository.repoSource().allFossils()){
                is Result.Success -> {
                    error.postValue(null)
                    emit(result.data)
                }
                is Result.Error -> error.postValue(result.exception)
            }
            loading.postValue(false)
        }
    }

    private val databaseChange = MutableLiveData<Boolean>()

    private val fossilEntityWithSaved = combinedLiveData(viewModelScope.coroutineContext + Dispatchers.IO,
        x = fossilEntity, y = databaseChange, runCheck = { x, y -> x}){ x, y ->
        val saved = repository.local().fossilDao().all()
        val result = x.orEmpty().map { entity -> FossilEntityMix(entity, saved.firstOrNull { entity.fileName == it.id }) }
        emit(result)
    }

    val selected = MutableLiveData(emptyList<String>())

    val fossils = combinedLiveData(viewModelScope.coroutineContext + Dispatchers.Default,
        x = selected, y = fossilEntityWithSaved, runCheck = { x, y -> y}){ x, y ->
        val result = y.orEmpty().map { item -> FossilEntityMixSelectable(item.entity, item.saved, x.orEmpty().contains(item.entity.fileName)) }
        emit(result)
    }

    fun toggle(id: String) {
        val list = mutableListOf<String>()
        list.addAll(selected.value.orEmpty())
        if (list.contains(id)) {
            list.remove(id)
        } else {
            list.add(id)
        }
        selected.value = list
    }

    val ownAction = combinedLiveData(viewModelScope.coroutineContext + Dispatchers.Default,
        x = selected, y = fossilEntityWithSaved, runCheck = { x, y -> y}){ x, y ->
        val result =  !x.orEmpty().any { id -> y.orEmpty().firstOrNull { it.entity.fileName == id }?.saved?.owned ?: false }
        emit(result)
    }


    val donateAction = combinedLiveData(viewModelScope.coroutineContext + Dispatchers.Default,
        x = selected, y = fossilEntityWithSaved, runCheck = { x, y -> y}){ x, y ->
        val result =  !x.orEmpty().any { id -> y.orEmpty().firstOrNull { it.entity.fileName == id }?.saved?.donated ?: false }
        emit(result)
    }

    fun toggleDonate(){
        val value = selected.value ?: return
        val to = donateAction.value!!
        loading.value = true
        viewModelScope.launch (viewModelScope.coroutineContext + Dispatchers.IO){
            val newSaved = value.map { id -> fossilEntityWithSaved.value?.firstOrNull { it.entity.fileName == id }?.saved ?: FossilSaved(id) }
                .map { FossilSaved(it.id, it.owned, to, it.quantity) }
            repository.local().fossilDao().insert(newSaved)
            databaseChange.postValue(true)
            loading.postValue(false)
        }
    }

    fun toggleOwn(){
        val value = selected.value ?: return
        val to = ownAction.value!!
        loading.value = true
        viewModelScope.launch (viewModelScope.coroutineContext + Dispatchers.IO){
            val newSaved = value.map { id -> fossilEntityWithSaved.value?.firstOrNull { it.entity.fileName == id }?.saved ?: FossilSaved(id) }
                .map { FossilSaved(it.id, to, it.donated, it.quantity) }
            repository.local().fossilDao().insert(newSaved)
            databaseChange.postValue(true)
            loading.postValue(false)
        }
    }

    fun clearSelected() {
        selected.postValue(emptyList())
    }

    val found = fossilEntityWithSaved.map {
        "${it.count { it.saved?.owned ?: false }}/${it.size}"
    }

    val donate = fossilEntityWithSaved.map {
        "${it.count { it.saved?.donated ?: false }}/${it.size}"
    }

}

data class FossilEntityMixSelectable(
    private val fossil: FossilEntity,
    private val save: FossilSaved?,
    val selected: Boolean
): FossilEntityMix(fossil, save), ItemComparable<String>{
    override fun id() = fossil.fileName
}