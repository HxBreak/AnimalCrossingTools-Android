package com.hxbreak.animalcrossingtools.ui.houseware

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hxbreak.animalcrossingtools.data.prefs.PreferenceStorage
import com.hxbreak.animalcrossingtools.data.source.DataRepository
import javax.inject.Inject

class HousewaresViewModel @Inject constructor(
    private val repository: DataRepository,
    private val preferenceStorage: PreferenceStorage,
): ViewModel(){
    val locale = preferenceStorage.selectedLocale
    val editMode = MutableLiveData<Boolean>(false)
}