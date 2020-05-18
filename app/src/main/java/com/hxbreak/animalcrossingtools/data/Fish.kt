package com.hxbreak.animalcrossingtools.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fish")
data class Fish @JvmOverloads constructor(
    @PrimaryKey @ColumnInfo(name = "name") var name: String = "",
    @ColumnInfo(name = "image_link") var imageLink: String = "",
    @ColumnInfo(name = "location") var location: String = "",
    @ColumnInfo(name = "shadow_size") var shadowSize: String = "",
    @ColumnInfo(name = "time") var time: String = "",
    @ColumnInfo(name = "price") var price: Int = 0,
    @ColumnInfo(name = "jan") var jan: Boolean = false,
    @ColumnInfo(name = "feb") var feb: Boolean = false,
    @ColumnInfo(name = "mar") var mar: Boolean = false,
    @ColumnInfo(name = "apr") var apr: Boolean = false,
    @ColumnInfo(name = "may") var may: Boolean = false,
    @ColumnInfo(name = "jun") var jun: Boolean = false,
    @ColumnInfo(name = "jul") var jul: Boolean = false,
    @ColumnInfo(name = "aug") var aug: Boolean = false,
    @ColumnInfo(name = "sep") var sep: Boolean = false,
    @ColumnInfo(name = "oct") var oct: Boolean = false,
    @ColumnInfo(name = "nov") var nov: Boolean = false,
    @ColumnInfo(name = "dec") var dec: Boolean = false,
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