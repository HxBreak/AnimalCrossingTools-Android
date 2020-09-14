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

@Entity(tableName = "bug")
data class BugSaved @JvmOverloads constructor(
    @PrimaryKey @ColumnInfo(name = "id") var id: Int = 0,
    @ColumnInfo(name = "owned") var owned: Boolean = false,
    @ColumnInfo(name = "donated") var donated: Boolean = false,
    @ColumnInfo(name = "quantity") var quantity: Int = 0
)

@Entity(tableName = "seacreature")
data class SeaCreatureSaved @JvmOverloads constructor(
    @PrimaryKey @ColumnInfo(name = "id") var id: Int = 0,
    @ColumnInfo(name = "owned") var owned: Boolean = false,
    @ColumnInfo(name = "donated") var donated: Boolean = false,
    @ColumnInfo(name = "quantity") var quantity: Int = 0
)

@Entity(tableName = "fossil")
data class FossilSaved @JvmOverloads constructor(
    @PrimaryKey @ColumnInfo(name = "id") var id: String,
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

@Entity(tableName = "art")
data class ArtSaved @JvmOverloads constructor(
    @PrimaryKey @ColumnInfo(name = "id") var id: Int = 0,
    @ColumnInfo(name = "owned") var owned: Boolean = false,
    @ColumnInfo(name = "quantity") var quantity: Int = 0
)

