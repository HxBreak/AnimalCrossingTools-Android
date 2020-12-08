package com.hxbreak.animalcrossingtools.data.source.entity

import androidx.room.ColumnInfo
import com.google.gson.annotations.SerializedName
import com.hxbreak.animalcrossingtools.data.prefs.Hemisphere


data class ItemAvailability (
    @SerializedName("month-northern")
    @ColumnInfo(name = "month_northern")
    val monthnorthern: String? = null,

    @SerializedName("month-southern")
    @ColumnInfo(name = "month_southern")
    var monthsouthern: String? = null,
    var time: String? = null,
    var isAllDay: Boolean = false,
    var isAllYear: Boolean = false,
    var location: String? = null,
    var rarity: String? = null,

    @SerializedName("month-array-northern")
    @ColumnInfo(name = "month_array_northern")
    var monthArrayNorthern: List<Short>,

    @SerializedName("month-array-southern")
    @ColumnInfo(name = "month_array_southern")
    var monthArraySouthern: List<Short>,

    @SerializedName("time-array")
    @ColumnInfo(name = "time_array")
    var timeArray: List<Short>? = null
){
    fun requireTimeArray() = timeArray ?: throw Exception("Time Array Is Require")
}

fun ItemAvailability.monthArray(hemisphere: Hemisphere) =
    (if (hemisphere == Hemisphere.Northern) monthArrayNorthern else monthArraySouthern)