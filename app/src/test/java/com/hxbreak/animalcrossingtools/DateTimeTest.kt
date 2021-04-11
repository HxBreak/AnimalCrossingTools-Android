package com.hxbreak.animalcrossingtools

import com.hxbreak.animalcrossingtools.data.source.converters.DateTimeConverter
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset

@RunWith(JUnit4::class)
class DateTimeTest {

    @Test
    fun testDateConvert(){
        val localDate = LocalDateTime.now()
        val clock = Clock.systemUTC()
        val converter = DateTimeConverter()
        val datetime = LocalDateTime.now(clock)
        val long = converter.dateTimeToLong(localDate.atOffset(ZoneOffset.UTC).toLocalDateTime())
        val _datetime = converter.longToDateTime(long)
//        println(listOf(localDate, datetime, _datetime, long))
        Assert.assertEquals(localDate, _datetime)
        Assert.assertNotEquals(localDate, datetime)
    }
}