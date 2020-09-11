package com.hxbreak.animalcrossingtools.data.source.entity

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.hxbreak.animalcrossingtools.adapter.ItemComparable
import com.hxbreak.animalcrossingtools.data.ArtSaved
import kotlinx.android.parcel.Parcelize

@Parcelize
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
) : Parcelable

open class ArtEntityMix(
    val art: ArtEntity,
    val saved: ArtSaved?,
)