package com.hxbreak.animalcrossingtools.ui.houseware

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.hxbreak.animalcrossingtools.adapter.ItemComparable
import com.hxbreak.animalcrossingtools.data.Result
import com.hxbreak.animalcrossingtools.data.prefs.PreferenceStorage
import com.hxbreak.animalcrossingtools.data.source.DataRepository
import com.hxbreak.animalcrossingtools.data.source.entity.HousewareEntity
import com.hxbreak.animalcrossingtools.fragment.Event
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class HousewaresViewModel @ViewModelInject constructor(
    private val repository: DataRepository,
    private val preferenceStorage: PreferenceStorage,
): ViewModel(){
    val locale = preferenceStorage.selectedLocale
    val editMode = MutableLiveData<Boolean>(false)
    val loading = MutableLiveData(false)
    val refresh = MutableLiveData(false)
    val error = MutableLiveData<Throwable>()
    val database = MutableLiveData<Event<String>>()
    val housewares = refresh.switchMap {
        loading.value = true
        liveData (viewModelScope.coroutineContext + Dispatchers.IO){
            viewModelScope.launch (viewModelScope.coroutineContext + Dispatchers.IO){
                val cache = repository.local().housewaresDao().all().groupBy {
                    it.seriesId
                }.map { HousewareVariants(it.value) }
                emit(cache)
            }
            when (val result = repository.repoSource().allHousewares()){
                is Result.Success -> {
                    emit(result.data.second.map { HousewareVariants(it) })
                    result.data.first.join()
                    database.postValue(Event("Database updated"))
                }
                is Result.Error -> {
                    error.postValue(result.exception)
                }
            }
            loading.postValue(false)
        }
    }
}

data class HousewareVariants(
    val variants: List<HousewareEntity>
): ItemComparable<String> {
    override fun id() = variants.first().seriesId
}