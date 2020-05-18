package com.hxbreak.animalcrossingtools.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import com.hxbreak.animalcrossingtools.ServiceLocator
import com.hxbreak.animalcrossingtools.data.source.DataRepository
import com.hxbreak.animalcrossingtools.ui.fish.FishViewModel
import com.hxbreak.animalcrossingtools.ui.song.SongViewModel


class ViewModelFactory constructor(
    private val repository: DataRepository,
    owner: SavedStateRegistryOwner,
    defaultArgs: Bundle? = null
) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {

    override fun <T : ViewModel> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle
    ) = with(modelClass) {
        when {
            isAssignableFrom(FishViewModel::class.java) ->
                FishViewModel(repository, handle)
            isAssignableFrom(SongViewModel::class.java) ->
                SongViewModel(repository, handle)
            else ->
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    } as T
}

fun Fragment.getViewModelFactory(): ViewModelFactory {
    val repository = ServiceLocator.provideRepository(requireContext())
    return ViewModelFactory(repository, this)
}