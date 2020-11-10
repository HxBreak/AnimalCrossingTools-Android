package com.hxbreak.animalcrossingtools.data.prefs

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.time.Instant


class SharedDataUsageStorage constructor(context: Context) : DataUsageStorage {

    val listener = SharedPreferences.OnSharedPreferenceChangeListener{ sharedPreferences, key ->
        when(key){
            STORABLE_REFRESH_TYPE, STORABLE_REFRESH_TIME -> {
                selectDataRefreshDurationLiveData.postValue(selectStorableDataRefreshDuration)
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
            registerOnSharedPreferenceChangeListener(listener)
        }
    }

    override var selectStorableDataRefreshDuration: StorableDuration by StorableDurationPreference(
        prefs, STORABLE_REFRESH_TYPE, STORABLE_REFRESH_TIME, StorableDuration.DOWNLOAD_WHEN_EMPTY
    )

    override var lastFurnitureRefreshDateTime: Instant by InstantPreference(
        prefs, LAST_FURNITURES_REFRESH_DATETIME, Instant.MIN
    )

    private val selectDataRefreshDurationLiveData = MutableLiveData<StorableDuration>(selectStorableDataRefreshDuration)

    override val selectStorableDataRefreshDurationLiveData: LiveData<StorableDuration> by lazy {
        selectDataRefreshDurationLiveData
    }


    companion object {
        const val LAST_FURNITURES_REFRESH_DATETIME = "LAST_FURNITURES_REFRESH_DATETIME"
        const val PREFS_NAME = "setting_datausage"
        const val STORABLE_REFRESH_TYPE = "STORABLE_REFRESH_TYPE"
        const val STORABLE_REFRESH_TIME = "STORABLE_REFRESH_TIME"
    }
}