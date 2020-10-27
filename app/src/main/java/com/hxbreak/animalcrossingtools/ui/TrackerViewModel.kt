package com.hxbreak.animalcrossingtools.ui

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.hxbreak.animalcrossingtools.di.AndroidId
import com.hxbreak.animalcrossingtools.di.ApplicationModule
import javax.inject.Inject

class TrackerViewModel @ViewModelInject constructor(@AndroidId val id: String) :
    ViewModel()
