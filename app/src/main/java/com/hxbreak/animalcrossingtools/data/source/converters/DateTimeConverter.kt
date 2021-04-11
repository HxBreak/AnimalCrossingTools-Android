package com.hxbreak.animalcrossingtools.data.source.converters

import androidx.room.TypeConverter
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime

class DateTimeConverter {

    private val UTC_CLOCK = Clock.systemUTC()

    @TypeConverter
    fun dateTimeToLong(datetime: LocalDateTime?): Long? {
        return datetime?.atZone(UTC_CLOCK.zone)?.toInstant()?.toEpochMilli()
    }

    @TypeConverter
    fun longToDateTime(datetime: Long?): LocalDateTime? {
        datetime ?: return null
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(datetime), UTC_CLOCK.zone)
    }
}