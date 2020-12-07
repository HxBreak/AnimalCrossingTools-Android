package com.hxbreak.animalcrossingtools.data.source.entity

import com.google.gson.annotations.SerializedName

data class FishEntity(
    val id: Int = 0,
    @SerializedName("file-name")
    val filename: String,
    val name: LocalizationName,
    val availability: ItemAvailability,
    val shadow: String,
    val price: Int = 0,
    @SerializedName("price-cj")
    val priceCj: Int = 0,

    @SerializedName("catch-phrase")
    val catchphrase: String? = null,

    @SerializedName("museum-phrase")
    val museumphrase: String,
    val image_uri: String,
    val icon_uri: String,
)