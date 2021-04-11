package com.hxbreak.animalcrossingtools.ui.user

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.hxbreak.animalcrossingtools.di.AndroidId
import com.hxbreak.animalcrossingtools.services.handler.InstantMessageController
import com.hxbreak.stun.DiscoverInfo
import com.hxbreak.stun.StunHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*

class UserListViewModel @ViewModelInject constructor(
    @AndroidId val id: String,
    val controller: InstantMessageController
) : ViewModel() {

    val stunTest = StunHelper.testNatType(0, "stun.sipgate.net", 10000)
        .flowOn(Dispatchers.IO)
//        .shareIn(viewModelScope, SharingStarted.Lazily, 1)

    val stunResult = MutableStateFlow<DiscoverInfo?>(null)
}
