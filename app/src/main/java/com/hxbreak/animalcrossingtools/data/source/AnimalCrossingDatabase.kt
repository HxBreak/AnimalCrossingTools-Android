package com.hxbreak.animalcrossingtools.data.source

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.hxbreak.animalcrossingtools.data.*
import com.hxbreak.animalcrossingtools.data.source.converters.CommonConverter
import com.hxbreak.animalcrossingtools.data.source.entity.FishEntity
import com.hxbreak.animalcrossingtools.data.source.entity.FurnitureEntity
import com.hxbreak.animalcrossingtools.data.source.local.*

@Database(entities = [
    FishSaved::class,
    SongSaved::class,
    ArtSaved::class,
    BugSaved::class,
    SeaCreatureSaved::class,
    FossilSaved::class,
    FurnitureEntity::class,
    FishEntity::class,
], version = 1, exportSchema = false)
@TypeConverters(value = [CommonConverter::class])
abstract class AnimalCrossingDatabase : RoomDatabase() {

    abstract fun fishDao(): FishDao

    abstract fun songSavedDao(): SongSavedDao

    abstract fun artDao(): ArtDao

    abstract fun bugDao(): BugDao

    abstract fun seaCreatureDao(): SeaCreatureDao

    abstract fun fossilDao(): FossilDao

    abstract fun furnitureDao(): FurnitureDao

}