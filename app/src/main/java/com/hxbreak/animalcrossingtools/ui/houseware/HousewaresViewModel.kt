package com.hxbreak.animalcrossingtools.ui.houseware

import androidx.lifecycle.*
import com.hxbreak.animalcrossingtools.adapter.ItemComparable
import com.hxbreak.animalcrossingtools.data.Result
import com.hxbreak.animalcrossingtools.data.prefs.PreferenceStorage
import com.hxbreak.animalcrossingtools.data.source.DataRepository
import com.hxbreak.animalcrossingtools.data.source.entity.HousewareEntity
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

class HousewaresViewModel @Inject constructor(
    private val repository: DataRepository,
    private val preferenceStorage: PreferenceStorage,
): ViewModel(){
    val locale = preferenceStorage.selectedLocale
    val editMode = MutableLiveData<Boolean>(false)
    val loading = MutableLiveData(false)
    val refresh = MutableLiveData(false)
    val housewares = refresh.switchMap {
        loading.value = true
        liveData (viewModelScope.coroutineContext + Dispatchers.IO){
            when (val result = repository.repoSource().allHousewares()){
                is Result.Success -> emit(result.data.map { HousewareVariants(it) })
                is Result.Error -> {}
            }
            loading.postValue(false)
        }
    }
}

data class HousewareVariants(
    val variants: List<HousewareEntity>
): ItemComparable<String> {
    override fun id() = variants.first().internalId
}