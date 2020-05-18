package com.hxbreak.animalcrossingtools.data.source

import androidx.lifecycle.LiveData
import com.hxbreak.animalcrossingtools.data.Fish
import com.hxbreak.animalcrossingtools.data.Result
import com.hxbreak.animalcrossingtools.data.source.local.FishDataSource
import com.hxbreak.animalcrossingtools.data.source.remote.SongDataSource

abstract class DataRepository : FishDataSource, SongDataSource {

    abstract fun local(): AnimalCrossingDatabase
}