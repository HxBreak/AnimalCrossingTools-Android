package com.hxbreak.animalcrossingtools.data.source.local

import androidx.room.*
import com.hxbreak.animalcrossingtools.data.BugSaved

@Dao
interface BugDao {

    @Query("select * from bug")
    suspend fun all(): List<BugSaved>

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(bugs: List<BugSaved>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bugs: List<BugSaved>)

}