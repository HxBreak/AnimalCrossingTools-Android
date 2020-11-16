package com.hxbreak.animalcrossingtools

import com.hxbreak.animalcrossingtools.character.CharUtil
import kotlinx.coroutines.delay
import net.sourceforge.pinyin4j.PinyinHelper
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.FormatStyle
import java.util.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min


@RunWith(JUnit4::class)
class UnitTest {

    @Test
    fun testLeft(){
    }

    @Test
    fun testIs() {
        println(CharUtil.toCategory(CharUtil.headPinyin("恩")))
        println(CharUtil.toCategory("1"))
        println(CharUtil.toCategory("asd12"))
        println(Duration.ofHours(12).seconds % (3600 * 24) / 3600)
    }
    @Test
    fun timeTest(){
        println(DateTimeFormatter.ISO_LOCAL_DATE.format(Instant.now().atOffset(ZoneOffset.UTC)))
    }

    @Test
    fun testClock(){
        val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).withLocale(Locale.US)
        println(LocalDateTime.now().format(formatter))
        val now = Clock.systemDefaultZone()
        val instant = now.instant()
        println(instant.toEpochMilli() - (instant.epochSecond * 1000))
    }
}