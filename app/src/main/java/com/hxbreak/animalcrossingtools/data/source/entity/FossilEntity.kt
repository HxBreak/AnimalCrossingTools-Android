package com.hxbreak.animalcrossingtools.data.source.entity

import com.google.gson.annotations.SerializedName
import com.hxbreak.animalcrossingtools.data.BugSaved
import com.hxbreak.animalcrossingtools.data.FossilSaved

data class FossilEntity(
    @SerializedName("file-name")
    val fileName: String,
    val name: LocalizationName,
    val price: Int,
    @SerializedName("image_uri")
    val imageUri: String,
    @SerializedName("museum-phrase")
    val museumphrase: String? = null,
    @SerializedName("part-of")
    val partOf: String? = null,
)

open class FossilEntityMix(val entity: FossilEntity, val saved: FossilSaved?)