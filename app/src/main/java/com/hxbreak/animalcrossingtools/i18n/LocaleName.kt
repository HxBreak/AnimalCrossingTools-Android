package com.hxbreak.animalcrossingtools.i18n

import android.database.Cursor
import com.hxbreak.animalcrossingtools.data.source.entity.FishEntity
import com.hxbreak.animalcrossingtools.data.source.entity.LocalizationName
import java.lang.Exception
import java.util.*

fun LocalizationName.toLocaleName(locale: Locale): String {
    return try {
        val field =
            this.javaClass.getDeclaredField("name${locale.country.toUpperCase()}${locale.language.toLowerCase()}")
        field.isAccessible = true
        field.get(this) as String
    } catch (e: Exception) {
        if (this.nameUSen.isNullOrEmpty()) {
            "Error"
        } else {
            this.nameUSen!!
        }
    }
}

fun Locale.toDatabaseNameColumn() = "name_${country.toLowerCase()}_${language.toLowerCase()}"

fun Cursor.toLocaleName(locale: Locale): String? {
    if (isClosed) return null
    val index = getColumnIndex(locale.toDatabaseNameColumn())
    if (index < 0) return null
    return getString(index)
}
