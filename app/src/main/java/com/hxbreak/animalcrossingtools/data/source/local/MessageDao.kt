package com.hxbreak.animalcrossingtools.data.source.local

import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hxbreak.animalcrossingtools.data.source.entity.MessageEntity

@Dao
interface MessageDao {

    @Query("select * from message_entity where (`from` = :from and `to` = :to) or (`from` = :to and `to` = :from) order by createDateTime desc")
    fun chatSource(from: String, to: String): PagingSource<Int, MessageEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertAll(vararg entity: MessageEntity)
}