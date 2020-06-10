package com.hxbreak.animalcrossingtools

import com.hxbreak.animalcrossingtools.character.CharUtil
import net.sourceforge.pinyin4j.PinyinHelper
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4


@RunWith(JUnit4::class)
class UnitTest {

    @Test
    fun testIs() {
        println(CharUtil.toCategory(CharUtil.headPinyin("恩")))
        println(CharUtil.toCategory("1"))
        println(CharUtil.toCategory("asd12"))
    }
}