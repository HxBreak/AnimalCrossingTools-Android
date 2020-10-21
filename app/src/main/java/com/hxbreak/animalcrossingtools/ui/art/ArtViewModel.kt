package com.hxbreak.animalcrossingtools.ui.art

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.hxbreak.animalcrossingtools.adapter.ItemComparable
import com.hxbreak.animalcrossingtools.combinedLiveData
import com.hxbreak.animalcrossingtools.data.ArtSaved
import com.hxbreak.animalcrossingtools.data.Result
import com.hxbreak.animalcrossingtools.data.prefs.PreferenceStorage
import com.hxbreak.animalcrossingtools.data.source.DataRepository
import com.hxbreak.animalcrossingtools.data.source.entity.ArtEntity
import com.hxbreak.animalcrossingtools.data.source.entity.ArtEntityMix
import com.hxbreak.animalcrossingtools.fragment.Event
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.lang.Exception
import java.lang.IllegalStateException
import javax.inject.Inject

class ArtViewModel @ViewModelInject constructor(
    private val repository: DataRepository,
    private val preferenceStorage: PreferenceStorage
) : ViewModel(){

    val locale = preferenceStorage.selectedLocale

    val refresh = MutableLiveData<Boolean>(false)
    val loading = MutableLiveData(false)
    val erro = MutableLiveData<Event<Exception>>()
    val editMode = MutableLiveData(false)
    private val savedChange = MutableLiveData<Boolean>(false)

    private val artEntity = refresh.switchMap {
        loading.value = true
        liveData (viewModelScope.coroutineContext + Dispatchers.IO){
            when(val result = repository.repoSource().allArts()){
                is Result.Success -> emit(result.data)
                is Result.Error -> erro.value = Event(result.exception)
                else -> throw IllegalStateException("other state is not allow")
            }
            loading.postValue(false)
        }
    }


    private val artWithSaved = combinedLiveData(viewModelScope.coroutineContext + Dispatchers.Default,
        x = artEntity, y = savedChange, runCheck = {x, y -> x }){ art, y ->
        val saved = repository.local().artDao().getAllArtSaved()
        val result = art.orEmpty().map { entity -> ArtEntityMix(entity, saved.firstOrNull { entity.id == it.id }) }
        emit(result)
    }

    val selected = MutableLiveData<List<Int>>(emptyList())

    val arts = combinedLiveData(viewModelScope.coroutineContext + Dispatchers.Default,
        x = selected, y = artWithSaved, runCheck = { x, y -> y }){ x, y ->
        val result = y.orEmpty().map {
            ArtEntityMixSelectable(it.art, it.saved, x?.contains(it.art.id) ?: false) }
        emit(result)
    }

    val collectedText = artWithSaved.switchMap {
        liveData (viewModelScope.coroutineContext + Dispatchers.Default){
            emit("${it.count { it.saved?.owned ?: false }}/${it.size}")
        }
    }

    val ownAction = combinedLiveData(viewModelScope.coroutineContext + Dispatchers.Default,
        x = selected, y = artWithSaved, runCheck = {x, y -> x && y}){x, y ->
        val result = !x.orEmpty().any { id -> y.orEmpty().firstOrNull { id == it.art.id }?.saved?.owned ?: false }
        emit(result)
    }

    fun toggleArt(id: Int) {
        val list = mutableListOf<Int>()
        list.addAll(selected.value.orEmpty())
        if (list.contains(id)) {
            list.remove(id)
        } else {
            list.add(id)
        }
        selected.value = list
    }

    fun toggleOwnArt(){
        if (selected.value.isNullOrEmpty()) return
        val to = ownAction.value!!
        val value = selected.value!!.map { ArtSaved(it, to, 0) }
        viewModelScope.launch (viewModelScope.coroutineContext + Dispatchers.IO){
            loading.postValue(true)
            repository.local().artDao().insertArtSaved(value)
            loading.postValue(false)
            savedChange.postValue(true)
        }
    }

    fun clearSelected() {
        selected.postValue(emptyList())
    }
}

data class ArtEntityMixSelectable(
    private val art1: ArtEntity,
    private val saved1: ArtSaved?,
    val selected: Boolean):
    ArtEntityMix(art1, saved1), ItemComparable<Int>{
    override fun id() = art1.id
}
