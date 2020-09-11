package com.hxbreak.animalcrossingtools.data.source.local

import androidx.room.*
import com.hxbreak.animalcrossingtools.data.SeaCreatureSaved

@Dao
interface SeaCreatureDao {

    @Query("select * from seacreature")
    suspend fun all(): List<SeaCreatureSaved>

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(seaCreatures: List<SeaCreatureSaved>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(seaCreatures: List<SeaCreatureSaved>)

}