package com.hxbreak.animalcrossingtools.data.source.local

import android.database.Cursor
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteQuery
import com.hxbreak.animalcrossingtools.data.source.entity.FurnitureEntity

@Dao
interface FurnitureDao {

    @Query("select * from housewares")
    suspend fun all(): List<FurnitureEntity>

    @Query("select count(*) from housewares")
    suspend fun count(): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: List<FurnitureEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOne(entity: FurnitureEntity)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(entity: FurnitureEntity)

    @Query("select * from housewares where internal_id == :internalId")
    suspend fun allInternalId(internalId: String): List<FurnitureEntity>

    @RawQuery
    fun getCursorViaQuery(query: SupportSQLiteQuery?): Cursor

    @RawQuery
    fun filterViaQuery(query: SupportSQLiteQuery?): List<FurnitureEntity>
}