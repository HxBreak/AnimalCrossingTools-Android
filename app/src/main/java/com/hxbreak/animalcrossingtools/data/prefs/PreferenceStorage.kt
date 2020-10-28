package com.hxbreak.animalcrossingtools.data.prefs

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.WorkerThread
import androidx.core.content.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.hxbreak.animalcrossingtools.theme.Theme
import timber.log.Timber
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

interface PreferenceStorage {

    var selectedTheme: String?

    var observableSelectedTheme: LiveData<String>

    var observableTimeZone: LiveData<ZoneId>

    var observableLocale: LiveData<Locale>

    val dateTimeFormatter: LiveData<DateTimeFormatter>

    val observableHemisphere: LiveData<Hemisphere>

    var selectedLocale: Locale

    var selectedHemisphere: Hemisphere

    var selectedTimeZone: TimeZone

    val timeInNow: LocalDateTime
}

enum class Hemisphere{
    Northern,
    Southern
}

class SharedPreferenceStorage constructor(context: Context) : PreferenceStorage {

    private val changeListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        when (key) {
            PREF_DARK_MODE_ENABLED -> observableSelectedThemeResult.value = selectedTheme
            PREF_TIMEZONE -> observableSelectedTimeZoneId.value = ZoneId.of(selectedTimeZone.id)
            PREF_ISLAND_HEMISPHERE -> observableSelectedHemisphere.value = selectedHemisphere
            else -> {
                Timber.e("Not Handle For $key")
            }
        }
    }

    /**
     * changeListener should init before prefs
     */
    private val prefs: Lazy<SharedPreferences> = lazy { // Lazy to prevent IO access to main thread.
        context.applicationContext.getSharedPreferences(
            PREFS_NAME, Context.MODE_PRIVATE
        ).apply {
            registerOnSharedPreferenceChangeListener(changeListener)
        }
    }

    private val observableSelectedThemeResult = MutableLiveData<String>()

    override var selectedTimeZone: TimeZone by TimeZonePreference(prefs,
        PREF_TIMEZONE, TimeZone.getDefault())

    private val observableSelectedTimeZoneId = MutableLiveData<ZoneId>(ZoneId.of(selectedTimeZone.id))

    override var selectedTheme by StringPreference(
        prefs, PREF_DARK_MODE_ENABLED, Theme.SYSTEM.storageKey
    )

    private val observableSelectedLocale by lazy { MutableLiveData<Locale>() }

    override var selectedLocale by LocalePreference(
        prefs, PREF_RESOURCE_LANGUAGE, PREF_RESOURCE_REGION, Locale.getDefault(), observableSelectedLocale
    )

    override var selectedHemisphere: Hemisphere by HemispherePreference(prefs,
        PREF_ISLAND_HEMISPHERE, Hemisphere.Northern)

    private val observableSelectedHemisphere by lazy { MutableLiveData<Hemisphere>(selectedHemisphere) }

    override val timeInNow: LocalDateTime
        get() = LocalDateTime.now(Clock.system(ZoneId.of(selectedTimeZone.id)))

    override var observableSelectedTheme: LiveData<String>
        get() {
            observableSelectedThemeResult.value = selectedTheme
            return observableSelectedThemeResult
        }
        set(_) = throw IllegalAccessException("This property can't be changed")

    override var observableTimeZone: LiveData<ZoneId>
        get() {
            return observableSelectedTimeZoneId
        }
        set(_) = throw Exception("This property can't be changed")

    override var observableLocale: LiveData<Locale>
        get() = observableSelectedLocale
        set(_) = throw Exception("This property can't be changed")

    override val dateTimeFormatter: LiveData<DateTimeFormatter>
        get() = observableSelectedLocale.map {
            DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).withLocale(it)
        }

    override val observableHemisphere: LiveData<Hemisphere>
        get() = observableSelectedHemisphere

    init {
        observableSelectedLocale.value = selectedLocale
    }

    companion object {
        const val PREFS_NAME = "settings"
        const val PREF_DARK_MODE_ENABLED = "pref_dark_mode"
        const val PREF_RESOURCE_LANGUAGE = "pref_resource_language"
        const val PREF_RESOURCE_REGION = "pref_resource_region"
        const val PREF_ISLAND_HEMISPHERE = "pref_island_hemisphere"
        const val PREF_TIMEZONE = "pref_timezone"

    }
}

class TimeZonePreference(
    private val preferences: Lazy<SharedPreferences>,
    private val name: String,
    private val defaultTimeZone: TimeZone
) : ReadWriteProperty<Any, TimeZone>{
    override fun setValue(thisRef: Any, property: KProperty<*>, value: TimeZone) {
        preferences.value.edit {
            putString(name, value.id)
        }
    }

    override fun getValue(thisRef: Any, property: KProperty<*>): TimeZone {
        val strValue = preferences.value.getString(name, null) ?: return defaultTimeZone
        return try {
            TimeZone.getTimeZone(strValue)
        }catch (e: Exception){
            defaultTimeZone
        }
    }
}

class HemispherePreference(
    private val preferences: Lazy<SharedPreferences>,
    private val name: String,
    private val defaultHemisphere: Hemisphere
) : ReadWriteProperty<Any, Hemisphere>{
    override fun setValue(thisRef: Any, property: KProperty<*>, value: Hemisphere) {
        preferences.value.edit {
            putString(name, value.name)
        }
    }

    override fun getValue(thisRef: Any, property: KProperty<*>): Hemisphere {
        val strValue = preferences.value.getString(name, null) ?: return defaultHemisphere
        return try {
            Hemisphere.valueOf(strValue)
        }catch (e: Exception){
            defaultHemisphere
        }
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
    private val defaultValue: Locale,
    private val liveData: MutableLiveData<Locale>? = null
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
        liveData?.postValue(value)
    }
}
