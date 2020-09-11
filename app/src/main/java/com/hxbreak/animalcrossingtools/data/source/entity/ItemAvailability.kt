package com.hxbreak.animalcrossingtools.data.source.entity

import com.google.gson.annotations.SerializedName


data class ItemAvailability (
    @SerializedName("month-northern")
    val monthnorthern: String? = null,

    @SerializedName("month-southern")
    var monthsouthern: String? = null,
    var time: String? = null,
    var isAllDay: Boolean = false,
    var isAllYear: Boolean = false,
    var location: String? = null,
    var rarity: String? = null,

    @SerializedName("month-array-northern")
    var monthArrayNorthern: List<Int>,

    @SerializedName("month-array-southern")
    var monthArraySouthern: List<Int>,

    )