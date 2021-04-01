package com.hxbreak.animalcrossingtools.data.source.entity

import com.hxbreak.animalcrossingtools.adapter.ItemComparable
import com.hxbreak.animalcrossingtools.data.FishSaved

data class FishEntityMix(
    val fish: FishEntity,
    val saved: FishSaved?
): ItemComparable<Int>{
    override fun id() = fish.id
}