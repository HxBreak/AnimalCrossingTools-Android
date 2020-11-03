package com.hxbreak.animalcrossingtools.ui.houseware.detail

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hxbreak.animalcrossingtools.data.source.DataRepository
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject

class HousewareDetailViewModel @AssistedInject constructor(
    private val repository: DataRepository,
    @Assisted private val housewareId: Long
): ViewModel(){

    val item = MutableLiveData(housewareId)


    @AssistedInject.Factory
    interface AssistedFactory {
        fun create(housewareId: Long): HousewareDetailViewModel
    }

    companion object {

        @Suppress("UNCHECKED_CAST")
        fun provideFactory(
            assistedFactory: AssistedFactory,
            housewareId: Long
        ) = object : ViewModelProvider.Factory{
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return assistedFactory.create(housewareId) as T
            }
        }
    }
}