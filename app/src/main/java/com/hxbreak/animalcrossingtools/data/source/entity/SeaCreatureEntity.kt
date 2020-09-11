package com.hxbreak.animalcrossingtools.data.source.entity

import com.google.gson.annotations.SerializedName
import com.hxbreak.animalcrossingtools.data.SeaCreatureSaved

data class SeaCreatureEntity(
    val id: Int,
    @SerializedName("file-name")
    val fileName: String?,
    val name: LocalizationName,
    val price: Int,
    @SerializedName("catch-phrase")
    private val catchphrase: String? = null,
    @SerializedName("museum-phrase")
    private val museumphrase: String? = null,
    @SerializedName("image_uri")
    val imageUri: String,
    @SerializedName("icon_uri")
    val iconUri: String,
    val speed: String,
    val shadow: String,
    val availability: ItemAvailability,
)

open class SeaCreatureEntityMix(val entity: SeaCreatureEntity, val saved: SeaCreatureSaved?)