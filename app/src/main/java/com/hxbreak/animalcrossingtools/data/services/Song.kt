package com.hxbreak.animalcrossingtools.data.services

import androidx.room.Entity
import com.google.gson.annotations.SerializedName
import com.hxbreak.animalcrossingtools.data.SongSaved


data class Song(
    @SerializedName("buy-price")
    val buyPrice: Int?,
    @SerializedName("file-name")
    val fileName: String,
    val id: Int,
    val isOrderable: Boolean,
    val name: SongName,
    @SerializedName("sell-price")
    val sellPrice: Int?
)

open class SongMix(
    val song: Song,
    val songSaved: SongSaved?
)