package com.hxbreak.animalcrossingtools.data.prefs

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.hxbreak.animalcrossingtools.theme.Theme
import com.hxbreak.animalcrossingtools.ui.settings.datausage.toReadableString
import timber.log.Timber
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*

interface ApplicationPreference

interface PreferenceStorage : ApplicationPreference {

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


sealed class StorableDuration {
    object DOWNLOAD_ALWAYS : StorableDuration()
    object DOWNLOAD_WHEN_EMPTY : StorableDuration()

    data class InTime(val duration: Duration) : StorableDuration()

    override fun toString(): String {
        return when(this) {
            is DOWNLOAD_ALWAYS -> { "always" }
            is DOWNLOAD_WHEN_EMPTY -> { "whenEmpty" }
            is InTime -> {
                "InTime ${duration.toReadableString()}"
            }
        }
    }
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

