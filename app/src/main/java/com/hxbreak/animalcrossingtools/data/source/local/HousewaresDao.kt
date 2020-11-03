package com.hxbreak.animalcrossingtools.data.source.local

import android.database.Cursor
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteQuery
import com.hxbreak.animalcrossingtools.data.source.entity.HousewareEntity

@Dao
interface HousewaresDao {

    @Query("select * from housewares")
    suspend fun all(): List<HousewareEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: List<HousewareEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOne(entity: HousewareEntity)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(entity: HousewareEntity)

    @RawQuery
    fun getCursorViaQuery(query: SupportSQLiteQuery?): Cursor

    @RawQuery
    fun filterViaQuery(query: SupportSQLiteQuery?): List<HousewareEntity>
}