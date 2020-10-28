package com.hxbreak.animalcrossingtools.data.source

import androidx.room.Database
import androidx.room.RoomDatabase
import com.hxbreak.animalcrossingtools.data.*
import com.hxbreak.animalcrossingtools.data.source.entity.HousewareEntity
import com.hxbreak.animalcrossingtools.data.source.local.*

@Database(entities = [
    FishSaved::class,
    SongSaved::class,
    ArtSaved::class,
    BugSaved::class,
    SeaCreatureSaved::class,
    FossilSaved::class,
    HousewareEntity::class,
], version = 1, exportSchema = false)
abstract class AnimalCrossingDatabase : RoomDatabase() {

    abstract fun fishDao(): FishDao

    abstract fun songSavedDao(): SongSavedDao

    abstract fun artDao(): ArtDao

    abstract fun bugDao(): BugDao

    abstract fun seaCreatureDao(): SeaCreatureDao

    abstract fun fossilDao(): FossilDao

    abstract fun housewaresDao(): HousewaresDao

}