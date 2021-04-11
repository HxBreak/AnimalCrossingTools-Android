package com.hxbreak.animalcrossingtools.data.source.converters

import androidx.room.TypeConverter
import androidx.room.TypeConverters

class CommonConverter {

    @TypeConverter
    fun shortListToString(list: List<Short>?): String? {
        return list?.joinToString(separator = ", ")
    }

    @TypeConverter
    fun stringToShortList(s: String?): List<Short>? {
        return s?.split(",")?.map {
            it.trimStart().toShort()
        }
    }

}