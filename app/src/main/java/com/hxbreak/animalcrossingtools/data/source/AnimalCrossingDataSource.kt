package com.hxbreak.animalcrossingtools.data.source

import com.hxbreak.animalcrossingtools.data.source.local.FishDataSource
import com.hxbreak.animalcrossingtools.data.source.local.SongSavedDao
import com.hxbreak.animalcrossingtools.data.source.remote.SongDataSource

interface AnimalCrossingDataSource {

    fun fishSource(): FishDataSource

    fun songSource(): SongDataSource

}