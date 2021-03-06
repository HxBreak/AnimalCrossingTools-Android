package com.hxbreak.animalcrossingtools.ui.villager

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.hxbreak.animalcrossingtools.GlideProgressCollector
import com.hxbreak.animalcrossingtools.data.Result
import com.hxbreak.animalcrossingtools.data.prefs.PreferenceStorage
import com.hxbreak.animalcrossingtools.data.source.DataRepository
import com.hxbreak.animalcrossingtools.fragment.Event
import kotlinx.coroutines.Dispatchers
import java.lang.Exception
import java.lang.IllegalStateException
import javax.inject.Inject

class VillagerViewModel @ViewModelInject constructor(
    private val repository: DataRepository,
    private val preferenceStorage: PreferenceStorage,
    val collector: GlideProgressCollector,
): ViewModel(){

    val locale = preferenceStorage.selectedLocale

    val refresh = MutableLiveData(false)
    val loading = MutableLiveData(false)
    val error = MutableLiveData<Exception?>()

    val villagers = refresh.switchMap {
        loading.value = true
        liveData (viewModelScope.coroutineContext + Dispatchers.IO){
            when(val result = repository.repoSource().allVillagers()){
                is Result.Success -> {
                    error.postValue(null)
                    emit(result.data)
                }
                is Result.Error -> error.postValue(result.exception)
            }
            loading.postValue(false)
        }
    }
}