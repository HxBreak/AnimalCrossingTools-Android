package com.hxbreak.animalcrossingtools.data.source.local

import androidx.room.*
import com.hxbreak.animalcrossingtools.data.FossilSaved
import com.hxbreak.animalcrossingtools.data.SeaCreatureSaved

@Dao
interface FossilDao {

    @Query("select * from fossil")
    suspend fun all(): List<FossilSaved>

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(fossils: List<FossilSaved>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(fossils: List<FossilSaved>)

}