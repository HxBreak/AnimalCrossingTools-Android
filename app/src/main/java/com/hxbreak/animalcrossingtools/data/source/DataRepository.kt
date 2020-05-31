package com.hxbreak.animalcrossingtools.data.source

import com.hxbreak.animalcrossingtools.data.source.local.FishDataSource
import com.hxbreak.animalcrossingtools.data.source.remote.SongDataSource

abstract class DataRepository : SongDataSource {

    abstract fun local(): AnimalCrossingDatabase

    abstract fun fishSource(): FishDataSource
}