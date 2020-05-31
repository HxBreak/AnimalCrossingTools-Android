package com.hxbreak.animalcrossingtools.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fish")
data class FishSaved @JvmOverloads constructor(
    @PrimaryKey @ColumnInfo(name = "id") var id: Int = 0,
    @ColumnInfo(name = "owned") var owned: Boolean = false,
    @ColumnInfo(name = "donated") var donated: Boolean = false,
    @ColumnInfo(name = "quantity") var quantity: Int = 0
)

@Entity(tableName = "song")
data class SongSaved @JvmOverloads constructor(
    @PrimaryKey @ColumnInfo(name = "id") var id: Int = 0,
    @ColumnInfo(name = "owned") var owned: Boolean = false,
    @ColumnInfo(name = "quantity") var quantity: Int = 0
)

data class FishAddictionPart(
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "owned") val owned: Boolean,
    @ColumnInfo(name = "donated") val donated: Boolean,
    @ColumnInfo(name = "quantity") val quantity: Int
)