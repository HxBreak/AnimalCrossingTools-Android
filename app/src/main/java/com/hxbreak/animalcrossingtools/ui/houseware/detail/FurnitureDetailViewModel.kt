package com.hxbreak.animalcrossingtools.ui.houseware.detail

import androidx.lifecycle.*
import com.hxbreak.animalcrossingtools.data.prefs.PreferenceStorage
import com.hxbreak.animalcrossingtools.data.source.DataRepository
import com.hxbreak.animalcrossingtools.fragment.Event
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers

class FurnitureDetailViewModel @AssistedInject constructor(
    private val repository: DataRepository,
    private val preference: PreferenceStorage,
    @Assisted private val filename: String,
    @Assisted private val housewareId: Long,
): ViewModel(){

    val locale = preference.selectedLocale

    val dao by lazy {
        repository.local().furnitureDao()
    }

    val item = MutableLiveData(filename)

    val items = liveData(viewModelScope.coroutineContext + Dispatchers.IO) {
        emit(dao.allInternalId(housewareId.toString()))
    }

    val current = items.map {
        Event(it.indexOfFirst { it.fileName == filename })
    }

    @AssistedInject.Factory
    interface AssistedFactory {
        fun create(filename: String, housewareId: Long): FurnitureDetailViewModel
    }

    companion object {

        @Suppress("UNCHECKED_CAST")
        fun provideFactory(
            assistedFactory: AssistedFactory,
            filename: String,
            housewareId: Long
        ) = object : ViewModelProvider.Factory{
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return assistedFactory.create(filename, housewareId) as T
            }
        }
    }
}