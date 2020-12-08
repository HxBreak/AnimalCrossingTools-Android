package com.hxbreak.animalcrossingtools.data.source

import com.hxbreak.animalcrossingtools.data.source.remote.RepoDataSource
import com.hxbreak.animalcrossingtools.data.source.remote.SongDataSource

interface AnimalCrossingDataSource {

    fun songSource(): SongDataSource

    fun repoSource(): RepoDataSource

}