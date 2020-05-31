package com.hxbreak.animalcrossingtools.data.source.entity

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
    val sellPrice: Int?,
    @SerializedName("image_uri")
    val imageUrl: String?,
    @SerializedName("music_uri")
    val musicUrl: String?
)

open class SongMix(
    val song: Song,
    val songSaved: SongSaved?
)