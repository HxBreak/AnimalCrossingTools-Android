package com.hxbreak.animalcrossingtools.data.source.entity

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.hxbreak.animalcrossingtools.adapter.ItemComparable
import java.time.LocalDateTime

@Entity(tableName = "message_entity")
data class MessageEntity(
    val from: String,
    val to: String,
    val mimeType: String,
    val createDateTime: LocalDateTime,
    @PrimaryKey(autoGenerate = true)
    val _id: Long = 0,
    val deleted: Boolean = false,
    val updateDateTime: LocalDateTime? = null,
    val description: String? = null,
    val path: String? = null,
): ItemComparable<Long>{

    @Ignore
    override fun id() = _id
}
