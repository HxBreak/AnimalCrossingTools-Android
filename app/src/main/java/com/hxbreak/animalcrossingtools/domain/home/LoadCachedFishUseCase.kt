package com.hxbreak.animalcrossingtools.domain.home

import com.hxbreak.animalcrossingtools.data.prefs.Hemisphere
import com.hxbreak.animalcrossingtools.data.prefs.PreferenceStorage
import com.hxbreak.animalcrossingtools.data.source.DataRepository
import com.hxbreak.animalcrossingtools.data.source.entity.FishEntity
import com.hxbreak.animalcrossingtools.domain.UseCase
import com.hxbreak.animalcrossingtools.shared.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject

data class LocalAvailabilityEntity<E>(
    val inDay: Boolean,
    val inHour: Boolean,
    val entity: E,
)

class LoadCachedFishUseCase @Inject constructor(
    private val repository: DataRepository,
    private val preference: PreferenceStorage,
    @IoDispatcher dispatcher: CoroutineDispatcher
) : UseCase<Unit, List<LocalAvailabilityEntity<FishEntity>>?>(dispatcher) {

    override suspend fun execute(parameters: Unit): List<LocalAvailabilityEntity<FishEntity>>? {
        val count = repository.local().fishDao().countFishEntity()
        val datetime = LocalDateTime.now(Clock.system(ZoneId.of(preference.selectedTimeZone.id)))
        if (count > 0){
            val allSaved = repository.local().fishDao().allFishSaved()
            return repository.local().fishDao().allFishEntity().filter {
                if (allSaved.any { saved -> saved.id == it.id && (saved.donated || saved.owned) }) return@filter false
                val month = if (preference.selectedHemisphere == Hemisphere.Northern){
                    it.availability.monthArrayNorthern
                } else {
                    it.availability.monthArraySouthern
                }
                month.contains(datetime.month.value.toShort())


            }.map {
                val activeInNow = it.availability.timeArray.orEmpty().contains(datetime.hour.toShort())
                LocalAvailabilityEntity(true, activeInNow, it)
            }
        }
        return null
    }
}