package com.hxbreak.animalcrossingtools

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.hxbreak.animalcrossingtools.data.prefs.PreferenceStorage
import com.hxbreak.animalcrossingtools.media.MusicServiceConnection
import com.hxbreak.animalcrossingtools.theme.Theme
import com.hxbreak.animalcrossingtools.theme.ThemedActivityDelegate
import com.hxbreak.animalcrossingtools.theme.themeFromStorageKey
import javax.inject.Inject

class MainActivityViewModel @ViewModelInject constructor(
    themedActivityDelegate: ThemedActivityDelegateImpl,
    val connection: MusicServiceConnection
) : ViewModel(), ThemedActivityDelegate by themedActivityDelegate


class ThemedActivityDelegateImpl @Inject constructor(
    private val preferenceStorage: PreferenceStorage
) : ThemedActivityDelegate {
    override val theme: LiveData<Theme> by lazy(LazyThreadSafetyMode.NONE) {
        preferenceStorage.observableSelectedTheme.map { themeFromStorageKey(it) }
    }

    override val currentTheme: Theme
        get() = if (preferenceStorage.selectedTheme == null) {
            Theme.SYSTEM
        } else {
            themeFromStorageKey(preferenceStorage.selectedTheme!!)
        }
}