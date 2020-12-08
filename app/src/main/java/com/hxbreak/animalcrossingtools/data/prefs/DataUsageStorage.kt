package com.hxbreak.animalcrossingtools.data.prefs

import androidx.lifecycle.LiveData
import java.time.Instant


interface DataUsageStorage : ApplicationPreference {

    var selectStorableDataRefreshDuration: StorableDuration

    var lastFurnitureRefreshDateTime: Instant

    var lastFishEntityRefreshDateTime: Instant

    val selectStorableDataRefreshDurationLiveData: LiveData<StorableDuration>
}
