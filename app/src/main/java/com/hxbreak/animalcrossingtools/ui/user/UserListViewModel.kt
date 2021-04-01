package com.hxbreak.animalcrossingtools.ui.user

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import com.hxbreak.animalcrossingtools.di.AndroidId
import com.hxbreak.animalcrossingtools.services.handler.InstantMessageController
import kotlinx.coroutines.flow.collect

class UserListViewModel @ViewModelInject constructor(
    @AndroidId val id: String,
    val controller: InstantMessageController
) : ViewModel() {

    init {

    }
}
