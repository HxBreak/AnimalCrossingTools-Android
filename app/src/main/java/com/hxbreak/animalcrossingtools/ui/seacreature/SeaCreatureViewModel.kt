package com.hxbreak.animalcrossingtools.ui.seacreature

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.hxbreak.animalcrossingtools.adapter.ItemComparable
import com.hxbreak.animalcrossingtools.combinedLiveData
import com.hxbreak.animalcrossingtools.data.Result
import com.hxbreak.animalcrossingtools.data.SeaCreatureSaved
import com.hxbreak.animalcrossingtools.data.prefs.PreferenceStorage
import com.hxbreak.animalcrossingtools.data.source.DataRepository
import com.hxbreak.animalcrossingtools.data.source.entity.SeaCreatureEntity
import com.hxbreak.animalcrossingtools.data.source.entity.SeaCreatureEntityMix
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception
import javax.inject.Inject

class SeaCreatureViewModel @ViewModelInject constructor(
    private val repository: DataRepository,
    private val preferenceStorage: PreferenceStorage
) : ViewModel(){
    val locale = preferenceStorage.selectedLocale

    val editMode = MutableLiveData(false)
    val refresh = MutableLiveData(false)
    val error = MutableLiveData<Exception>()
    val loading = MutableLiveData<Boolean>()

    private val seaCreatureEntity = refresh.switchMap {
        loading.value = true
        liveData (viewModelScope.coroutineContext + Dispatchers.IO){
            val result = repository.repoSource().allSeaCreature()
            when(result){
                is Result.Success -> emit(result.data)
                is Result.Error -> error.postValue(result.exception)
            }
            loading.postValue(false)
        }
    }

    private val databaseChange = MutableLiveData<Boolean>()

    private val seaCreaturesEntityWithSaved = combinedLiveData(viewModelScope.coroutineContext + Dispatchers.IO,
        x = seaCreatureEntity, y = databaseChange, runCheck = { x, y -> x}){ x, y ->
        val saved = repository.local().seaCreatureDao().all()
        val result = x.orEmpty().map { entity -> SeaCreatureEntityMix(entity, saved.firstOrNull { entity.id == it.id }) }
        emit(result)
    }

    val selected = MutableLiveData(emptyList<Int>())

    val seacreatures = combinedLiveData(viewModelScope.coroutineContext + Dispatchers.Default,
        x = selected, y = seaCreaturesEntityWithSaved, runCheck = { x, y -> y}){ x, y ->
        val result = y.orEmpty().map { item -> SeaCreatureEntityMixSelectable(item.entity, item.saved, x.orEmpty().contains(item.entity.id)) }
        emit(result)
    }

    fun toggle(id: Int) {
        val list = mutableListOf<Int>()
        list.addAll(selected.value.orEmpty())
        if (list.contains(id)) {
            list.remove(id)
        } else {
            list.add(id)
        }
        selected.value = list
    }

    val ownAction = combinedLiveData(viewModelScope.coroutineContext + Dispatchers.Default,
        x = selected, y = seaCreaturesEntityWithSaved, runCheck = { x, y -> y}){ x, y ->
        val result =  !x.orEmpty().any { id -> y.orEmpty().firstOrNull { it.entity.id == id }?.saved?.owned ?: false }
        emit(result)
    }


    val donateAction = combinedLiveData(viewModelScope.coroutineContext + Dispatchers.Default,
        x = selected, y = seaCreaturesEntityWithSaved, runCheck = { x, y -> y}){ x, y ->
        val result =  !x.orEmpty().any { id -> y.orEmpty().firstOrNull { it.entity.id == id }?.saved?.donated ?: false }
        emit(result)
    }

    fun toggleDonate(){
        val value = selected.value ?: return
        val to = donateAction.value!!
        loading.value = true
        viewModelScope.launch (viewModelScope.coroutineContext + Dispatchers.IO){
            val newSaved = value.map { id -> seaCreaturesEntityWithSaved.value?.firstOrNull { it.entity.id == id }?.saved ?: SeaCreatureSaved(id) }
                .map { SeaCreatureSaved(it.id, it.owned, to, it.quantity) }
            repository.local().seaCreatureDao().insert(newSaved)
            databaseChange.postValue(true)
            loading.postValue(false)
        }
    }

    fun toggleOwn(){
        val value = selected.value ?: return
        val to = ownAction.value!!
        loading.value = true
        viewModelScope.launch (viewModelScope.coroutineContext + Dispatchers.IO){
            val newSaved = value.map { id -> seaCreaturesEntityWithSaved.value?.firstOrNull { it.entity.id == id }?.saved ?: SeaCreatureSaved(id) }
                .map { SeaCreatureSaved(it.id, to, it.donated, it.quantity) }
            repository.local().seaCreatureDao().insert(newSaved)
            databaseChange.postValue(true)
            loading.postValue(false)
        }
    }

    fun clearSelected() {
        selected.postValue(emptyList())
    }

    val found = seaCreaturesEntityWithSaved.map {
        "${it.count { it.saved?.owned ?: false }}/${it.size}"
    }

    val donate = seaCreaturesEntityWithSaved.map {
        "${it.count { it.saved?.donated ?: false }}/${it.size}"
    }
}

data class SeaCreatureEntityMixSelectable(
    private val seaCreature: SeaCreatureEntity,
    private val save: SeaCreatureSaved?,
    val selected: Boolean
): SeaCreatureEntityMix(seaCreature, save), ItemComparable<Int> {
    override fun id() = seaCreature.id
}