package com.hxbreak.animalcrossingtools.ui.bugs

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.hxbreak.animalcrossingtools.adapter.ItemComparable
import com.hxbreak.animalcrossingtools.combinedLiveData
import com.hxbreak.animalcrossingtools.data.BugSaved
import com.hxbreak.animalcrossingtools.data.Result
import com.hxbreak.animalcrossingtools.data.prefs.PreferenceStorage
import com.hxbreak.animalcrossingtools.data.source.DataRepository
import com.hxbreak.animalcrossingtools.data.source.entity.BugEntity
import com.hxbreak.animalcrossingtools.data.source.entity.BugEntityMix
import com.hxbreak.animalcrossingtools.data.source.entity.monthArray
import com.hxbreak.animalcrossingtools.extensions.previousValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.lang.Exception
import javax.inject.Inject

class BugsViewModel @ViewModelInject constructor(
    private val repository: DataRepository,
    val preferenceStorage: PreferenceStorage
): ViewModel(){
    val locale = preferenceStorage.selectedLocale
    val hemisphere = preferenceStorage.selectedHemisphere

    val editMode = MutableLiveData(false)
    val refresh = MutableLiveData(false)
    val error = MutableLiveData<Exception?>()
    val loading = MutableLiveData<Boolean>()

    private val bugEntity = refresh.switchMap {
        loading.value = true
        liveData (viewModelScope.coroutineContext + Dispatchers.IO){
            when(val result = repository.repoSource().allBugs()){
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

    private val bugEntityWithSaved = combinedLiveData(viewModelScope.coroutineContext + Dispatchers.IO,
        x = bugEntity, y = databaseChange, runCheck = {x, y -> x}){ x, y ->
        val saved = repository.local().bugDao().all()
        val result = x.orEmpty().map { entity -> BugEntityMix(entity, saved.firstOrNull { entity.id == it.id }) }
        emit(result)
    }

    val selected = MutableLiveData(emptyList<Int>())

    val bugs = combinedLiveData(viewModelScope.coroutineContext + Dispatchers.Default,
        x = selected, y = bugEntityWithSaved, runCheck = {x, y -> y}){x, y ->
        val result = y.orEmpty().map { item -> BugEntityMixSelectable(item.entity, item.saved, x.orEmpty().contains(item.entity.id)) }
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
        x = selected, y = bugEntityWithSaved, runCheck = {x, y -> y}){x, y ->
        val result =  !x.orEmpty().any { id -> y.orEmpty().firstOrNull { it.entity.id == id }?.saved?.owned ?: false }
        emit(result)
    }


    val donateAction = combinedLiveData(viewModelScope.coroutineContext + Dispatchers.Default,
        x = selected, y = bugEntityWithSaved, runCheck = {x, y -> y}){x, y ->
        val result =  !x.orEmpty().any { id -> y.orEmpty().firstOrNull { it.entity.id == id }?.saved?.donated ?: false }
        emit(result)
    }

    fun toggleDonate(){
        val value = selected.value ?: return
        val to = donateAction.value!!
        loading.value = true
        viewModelScope.launch (viewModelScope.coroutineContext + Dispatchers.IO){
            val newSaved = value.map { id -> bugEntityWithSaved.value?.firstOrNull { it.entity.id == id }?.saved ?: BugSaved(id) }
                .map { BugSaved(it.id, it.owned, to, it.quantity) }
            repository.local().bugDao().insert(newSaved)
            databaseChange.postValue(true)
            loading.postValue(false)
        }
    }

    fun toggleOwn(){
        val value = selected.value ?: return
        val to = ownAction.value!!
        loading.value = true
        viewModelScope.launch (viewModelScope.coroutineContext + Dispatchers.IO){
            val newSaved = value.map { id -> bugEntityWithSaved.value?.firstOrNull { it.entity.id == id }?.saved ?: BugSaved(id) }
                .map { BugSaved(it.id, to, it.donated, it.quantity) }
            repository.local().bugDao().insert(newSaved)
            databaseChange.postValue(true)
            loading.postValue(false)
        }
    }

    fun clearSelected() {
        selected.postValue(emptyList())
    }

    val found = bugEntityWithSaved.map {
        "${it.count { it.saved?.owned ?: false }}/${it.size}"
    }

    val donate = bugEntityWithSaved.map {
        "${it.count { it.saved?.donated ?: false }}/${it.size}"
    }

    val activies = bugEntityWithSaved.map {
        val now = preferenceStorage.timeInNow
        val month = now.monthValue
        val hour = now.hour
        val value = it.count { (it.entity.availability.monthArray(hemisphere).contains(month.toShort()) &&
                it.entity.availability.timeArray.orEmpty().contains(hour.toShort())) }
        "$value/${it.size}"
    }

}

data class BugEntityMixSelectable(
    private val bug: BugEntity,
    private val save: BugSaved?,
    val selected: Boolean
): BugEntityMix(bug, save), ItemComparable<Int>{
    override fun id() = bug.id
}