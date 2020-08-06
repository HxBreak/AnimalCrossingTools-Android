package com.hxbreak.animalcrossingtools.ui.settings

import android.os.Build
import androidx.core.os.BuildCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.hxbreak.animalcrossingtools.data.prefs.PreferenceStorage
import com.hxbreak.animalcrossingtools.theme.Theme
import com.hxbreak.animalcrossingtools.theme.themeFromStorageKey
import java.util.*
import javax.inject.Inject

class SettingsViewModel @Inject constructor(val preferenceStorage: PreferenceStorage) :
    ViewModel() {
    val availableThemes: MutableLiveData<List<Theme>> = MutableLiveData()
    val availableResourceLanguage: MutableLiveData<List<Locale>> = MutableLiveData()

    private val themeResult = MutableLiveData<Theme?>()
    val theme: LiveData<Theme>
    val supportedLanguageRange = setOf(
        "US" to "en",
        "EU" to "en",
        "EU" to "de",
        "EU" to "es",
        "US" to "es",
        "EU" to "fr",
        "US" to "fr",
        "EU" to "it",
        "EU" to "nl",
        "CN" to "zh",
        "TW" to "zh",
        "JP" to "ja",
        "KR" to "ko",
        "EU" to "ru"
    ).map { Locale(it.second, it.first) }

    init {
        preferenceStorage.selectedTheme?.let { key ->
            themeResult.value = themeFromStorageKey(key)
        }
        if (themeResult.value == null) {
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> Theme.SYSTEM
                else -> Theme.BATTERY_SAVER
            }
        }
        availableThemes.value = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                listOf(Theme.LIGHT, Theme.DARK, Theme.SYSTEM)
            }
            else -> {
                listOf(Theme.LIGHT, Theme.DARK, Theme.BATTERY_SAVER)
            }
        }

        availableResourceLanguage.value = supportedLanguageRange
        theme = themeResult.map { it ?: Theme.SYSTEM }
    }

    fun setTheme(it: Theme) {
        preferenceStorage.selectedTheme = it.storageKey
    }

    fun setLanguage(lang: Locale) {
        preferenceStorage.selectedLocale = lang
    }
}