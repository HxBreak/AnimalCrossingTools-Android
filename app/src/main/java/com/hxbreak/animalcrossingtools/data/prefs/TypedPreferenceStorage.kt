package com.hxbreak.animalcrossingtools.data.prefs

import android.content.SharedPreferences
import androidx.annotation.WorkerThread
import androidx.core.content.edit
import androidx.lifecycle.MutableLiveData
import java.time.Duration
import java.time.Instant
import java.util.*
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty


class TimeZonePreference(
    private val preferences: Lazy<SharedPreferences>,
    private val name: String,
    private val defaultTimeZone: TimeZone
) : ReadWriteProperty<Any, TimeZone> {
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

class InstantPreference(
    private val preferences: Lazy<SharedPreferences>,
    private val name: String,
    private val defaultValue: Instant
) : ReadWriteProperty<Any, Instant> {
    override fun setValue(thisRef: Any, property: KProperty<*>, value: Instant) {
        preferences.value.edit {
            putLong(name, value.epochSecond)
        }
    }

    override fun getValue(thisRef: Any, property: KProperty<*>): Instant {
        val value = preferences.value.getLong(name, 0)
        return try {
            Instant.ofEpochSecond(value)
        }catch (e: Exception){
            defaultValue
        }
    }
}

class HemispherePreference(
    private val preferences: Lazy<SharedPreferences>,
    private val name: String,
    private val defaultHemisphere: Hemisphere
) : ReadWriteProperty<Any, Hemisphere> {
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

class StorableDurationPreference(
    private val preferences: Lazy<SharedPreferences>,
    private val typeName: String,
    private val durationName: String,
    private val defaultValue: StorableDuration
) : ReadWriteProperty<Any, StorableDuration> {
    override fun setValue(thisRef: Any, property: KProperty<*>, value: StorableDuration) {
        preferences.value.edit {
            when(value){
                is StorableDuration.DOWNLOAD_ALWAYS -> {
                    putString(typeName, "always")
                }
                is StorableDuration.DOWNLOAD_WHEN_EMPTY -> {
                    putString(typeName, "when_empty")
                }
                is StorableDuration.InTime -> {
                    putString(typeName, "in_time")
                    putLong(durationName, value.duration.seconds)
                }
            }
        }
    }

    private fun greaterThanZero(value: Long): Long{
        if (value > 0) return value
        error("this value should bigger than ZERO")
    }

    override fun getValue(thisRef: Any, property: KProperty<*>): StorableDuration {
        return when(preferences.value.getString(typeName, null) ?: defaultValue){
            "always" -> StorableDuration.DOWNLOAD_ALWAYS
            "when_empty" -> StorableDuration.DOWNLOAD_WHEN_EMPTY
            "in_time" ->
                try {
                    StorableDuration.InTime(
                        Duration.ofSeconds(
                            greaterThanZero(preferences.value.getLong(durationName, -1))
                        )
                    )
                }catch (e: Exception){
                    defaultValue
                }
            else -> defaultValue
        }
    }
}