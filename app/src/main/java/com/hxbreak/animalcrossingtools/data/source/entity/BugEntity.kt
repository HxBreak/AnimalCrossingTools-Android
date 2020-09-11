package com.hxbreak.animalcrossingtools.data.source.entity

import com.google.gson.annotations.SerializedName
import com.hxbreak.animalcrossingtools.data.BugSaved

data class BugEntity(
    val id: Int,
    @SerializedName("file-name")
    val fileName: String?,
    val name: LocalizationName,
    @SerializedName("price-flick")
    val priceFlick: Int,
    val price: Int,
    @SerializedName("catch-phrase")
    private val catchphrase: String? = null,

    @SerializedName("museum-phrase")
    private val museumphrase: String? = null,
    @SerializedName("image_uri")
    val imageUri: String,

    @SerializedName("icon_uri")
    val iconUri: String,
    @SerializedName("museum-desc")
    val museumDesc: String,
    val availability: ItemAvailability,
)

open class BugEntityMix(val entity: BugEntity, val saved: BugSaved?)