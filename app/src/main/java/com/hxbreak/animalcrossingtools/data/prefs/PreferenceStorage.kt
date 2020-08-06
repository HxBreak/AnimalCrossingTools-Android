package com.hxbreak.animalcrossingtools.data.prefs

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.WorkerThread
import androidx.core.content.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hxbreak.animalcrossingtools.theme.Theme
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

interface PreferenceStorage {

    var selectedTheme: String?

    var observableSelectedTheme: LiveData<String>

    var selectedLocale: Locale

}

@Singleton
class SharedPreferenceStorage @Inject constructor(context: Context) : PreferenceStorage {

    private val prefs: Lazy<SharedPreferences> = lazy { // Lazy to prevent IO access to main thread.
        context.applicationContext.getSharedPreferences(
            PREFS_NAME, Context.MODE_PRIVATE
        ).apply {
            registerOnSharedPreferenceChangeListener(changeListener)
        }
    }

    private val observableSelectedThemeResult = MutableLiveData<String>()

    private val changeListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        when (key) {
            PREF_DARK_MODE_ENABLED -> observableSelectedThemeResult.value = selectedTheme
        }
    }

    override var selectedTheme by StringPreference(
        prefs, PREF_DARK_MODE_ENABLED, Theme.SYSTEM.storageKey
    )

    override var selectedLocale by LocalePreference(
        prefs, PREF_RESOURCE_LANGUAGE, PREF_RESOURCE_REGION, Locale.getDefault()
    )

    override var observableSelectedTheme: LiveData<String>
        get() {
            observableSelectedThemeResult.value = selectedTheme
            return observableSelectedThemeResult
        }
        set(_) = throw IllegalAccessException("This property can't be changed")

    companion object {
        const val PREFS_NAME = "settings"
        const val PREF_DARK_MODE_ENABLED = "pref_dark_mode"
        const val PREF_RESOURCE_LANGUAGE = "pref_resource_language"
        const val PREF_RESOURCE_REGION = "pref_resource_region"
    }
}


class StringPreference(
    private val preferences: Lazy<SharedPreferences>,
    private val name: String,
    private val defaultValue: String?
) : ReadWriteProperty<Any, String?> {

    @WorkerThread
    override fun getValue(thisRef: Any, property: KProperty<*>): String? {
        return preferences.value.getString(name, defaultValue)
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: String?) {
        preferences.value.edit { putString(name, value) }
    }
}


class LocalePreference(
    private val preferences: Lazy<SharedPreferences>,
    private val language: String,
    private val region: String,
    private val defaultValue: Locale
) : ReadWriteProperty<Any, Locale> {

    @WorkerThread
    override fun getValue(thisRef: Any, property: KProperty<*>): Locale {
        val lang = preferences.value.getString(language, null)
        val reg = preferences.value.getString(region, null)
        if (lang == null || reg == null) {
            return defaultValue
        }
        return Locale(lang, reg)
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: Locale) {
        preferences.value.edit {
            putString(language, value.language)
            putString(region, value.country)
        }
    }
}
