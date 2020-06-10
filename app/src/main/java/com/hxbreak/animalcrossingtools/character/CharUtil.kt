package com.hxbreak.animalcrossingtools.character

import com.github.promeg.pinyinhelper.Pinyin
import net.sourceforge.pinyin4j.PinyinHelper
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType
import java.lang.IllegalStateException
import java.util.regex.Pattern

object CharUtil {

    private val format = HanyuPinyinOutputFormat().apply {
        caseType = HanyuPinyinCaseType.LOWERCASE
        toneType = HanyuPinyinToneType.WITHOUT_TONE
//        vCharType = HanyuPinyinVCharType.WITH_U_UNICODE
    }

    private const val DEFAULT_TAG = "#"
    private const val DEFAULT_TAG_CHAR = '#'
    private val Validate = Pattern.compile("([\\w])?")

    fun headPinyin(s: String?): String {
        var code: Char? = null
        if (s.isNullOrBlank()) {
            return DEFAULT_TAG
        } else {
            code = s.trim().toLowerCase().first()
        }
        if (isChinese(code.toInt())) {
            return Pinyin.toPinyin(code)
        } else {
            return code.toString()
        }
    }

    fun toCategory(s: String?): String {
        if (s.isNullOrBlank()) {
            return DEFAULT_TAG
        }
        val ts = s.trim().toLowerCase().getOrElse(0) { DEFAULT_TAG_CHAR }
        val matcher = Validate.matcher(ts.toString())
        if (!matcher.matches()) return DEFAULT_TAG
        if (matcher.groupCount() > 0) {
            return matcher.group(0) ?: DEFAULT_TAG
        } else {
            return throw IllegalStateException("Regex not match and fatal")
        }
    }

    fun isChinese(code: Int): Boolean {
        return code in 19969..40868
    }

}