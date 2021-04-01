package com.hxbreak.animalcrossingtools

import com.hxbreak.animalcrossingtools.character.CharUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
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

    @Test
    fun testFlow(){
        val f1 = flow {
            repeat(3){
                delay(5000L)
                emit("${Math.random()}")
            }
            delay(Long.MAX_VALUE)
        }
        val f2 = flow {
            repeat(Int.MAX_VALUE){
                delay(2000L)
                emit(it)
            }
        }
        runBlocking {
            f1.combine(f2){ x, y ->
                x to y
            }.collect {
                println(it.toString())
            }
        }
    }
}