package com.hxbreak.animalcrossingtools.data.source

import androidx.room.Database
import androidx.room.RoomDatabase
import com.hxbreak.animalcrossingtools.data.Fish
import com.hxbreak.animalcrossingtools.data.SongSaved
import com.hxbreak.animalcrossingtools.data.source.local.FishDao
import com.hxbreak.animalcrossingtools.data.source.local.SongSavedDao

@Database(entities = [Fish::class, SongSaved::class], version = 2, exportSchema = false)
abstract class AnimalCrossingDatabase : RoomDatabase() {

    abstract fun fishDao(): FishDao

    abstract fun songSavedDao(): SongSavedDao

}