package com.hxbreak.animalcrossingtools.data.source.entity

import androidx.room.*
import com.google.gson.annotations.SerializedName
import com.hxbreak.animalcrossingtools.adapter.ItemComparable

@Entity(tableName = "fish_entity")
data class FishEntity(
    @PrimaryKey
    val id: Int = 0,
    @SerializedName("file-name")
    val filename: String,
    @Embedded
    val name: LocalizationName,
    @Embedded
    val availability: ItemAvailability,
    val shadow: String,
    val price: Int = 0,
    @SerializedName("price-cj")
    @ColumnInfo(name = "price_cj")
    val priceCj: Int = 0,

    @SerializedName("catch-phrase")
    val catchphrase: String? = null,

    @SerializedName("museum-phrase")
    val museumphrase: String,
    val image_uri: String,
    val icon_uri: String,
): ItemComparable<Int>{
    @Ignore
    override fun id() = id
}