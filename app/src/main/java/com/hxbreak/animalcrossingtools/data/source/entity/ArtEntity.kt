package com.hxbreak.animalcrossingtools.data.source.entity

import com.google.gson.annotations.SerializedName
import com.hxbreak.animalcrossingtools.data.ArtSaved

data class ArtEntity(
    val id: Int,
    @SerializedName("file-name")
    val fileName: String?,
    val name: LocalizationName,
    val hasFake: Boolean,
    @SerializedName("buy-price")
    val buyPrice: Int?,
    @SerializedName("sell-price")
    val sellPrice: Int?,
    @SerializedName("image_uri")
    val imageUri: String,
    @SerializedName("museum-desc")
    val museumDesc: String,
)

open class ArtEntityMix(
    val art: ArtEntity,
    val saved: ArtSaved?,
)