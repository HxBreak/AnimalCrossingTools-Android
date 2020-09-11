package com.hxbreak.animalcrossingtools.data.source

import androidx.room.Database
import androidx.room.RoomDatabase
import com.hxbreak.animalcrossingtools.data.ArtSaved
import com.hxbreak.animalcrossingtools.data.BugSaved
import com.hxbreak.animalcrossingtools.data.FishSaved
import com.hxbreak.animalcrossingtools.data.SongSaved
import com.hxbreak.animalcrossingtools.data.source.local.ArtDao
import com.hxbreak.animalcrossingtools.data.source.local.BugDao
import com.hxbreak.animalcrossingtools.data.source.local.FishDao
import com.hxbreak.animalcrossingtools.data.source.local.SongSavedDao

@Database(entities = [FishSaved::class, SongSaved::class, ArtSaved::class, BugSaved::class], version = 1, exportSchema = false)
abstract class AnimalCrossingDatabase : RoomDatabase() {

    abstract fun fishDao(): FishDao

    abstract fun songSavedDao(): SongSavedDao

    abstract fun artDao(): ArtDao

    abstract fun bugDao(): BugDao

}