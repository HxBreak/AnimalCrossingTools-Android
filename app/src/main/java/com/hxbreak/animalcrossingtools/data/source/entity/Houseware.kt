package com.hxbreak.animalcrossingtools.data.source.entity

import com.google.gson.annotations.SerializedName

data class HousewareEntity(
    val name: LocalizationName,
    @SerializedName("body-title")
    val bodyTitle: String,
    @SerializedName("buy-price")
    val buyPrice: Int,
    val canCustomizeBody: Boolean,
    val canCustomizePattern: Boolean,
    @SerializedName("color-1")
    val color1: String,
    @SerializedName("color-2")
    val color2: String,
    @SerializedName("file-name")
    val fileName: String,
    @SerializedName("hha-concept-1")
    val hhaConcept1: String,
    @SerializedName("hha-concept-2")
    val hhaConcept2: String,
    @SerializedName("hha-series")
    val hhaSeries: String,
    @SerializedName("hha-set")
    val hhaSet: String,
    val image_uri: String,
    @SerializedName("internal-id")
    val internalId: String,
    val isCatalog: Boolean,
    val isDIY: Boolean,
    val isInteractive: Boolean,
    val isOutdoor: Boolean,
    @SerializedName("kit-cost")
    val kitCost: String,
    @SerializedName("lighting-type")
    val lightingType: String,
    val pattern: String,
    @SerializedName("pattern-title")
    val patternTitle: String,
    @SerializedName("sell-price")
    val sellPrice: Int,
    val size: String,
    val source: String,
    @SerializedName("source-detail")
    val sourceDetail: String,
    @SerializedName("speaker-type")
    val speakerType: String,
    val tag: String,
    val variant: String,
    @SerializedName("variant-id")
    val variantId: String,
    val version: String
)