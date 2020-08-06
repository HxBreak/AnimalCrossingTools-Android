package com.hxbreak.animalcrossingtools.i18n

import com.hxbreak.animalcrossingtools.data.source.entity.FishEntity
import com.hxbreak.animalcrossingtools.data.source.entity.SongName
import java.lang.Exception
import java.util.*

fun FishEntity.CommonName.toLocaleName(locale: Locale): String {
    return try {
        val field =
            this.javaClass.getDeclaredField("name${locale.country.toUpperCase()}${locale.language.toLowerCase()}")
        field.isAccessible = true
        field.get(this) as String
    } catch (e: Exception) {
        if (this.nameUSen.isNullOrEmpty()) {
            "Error"
        } else {
            this.nameUSen
        }
    }
}

fun SongName.toLocaleName(locale: Locale): String {
    return try {
        val field =
            this.javaClass.getDeclaredField("name${locale.country.toUpperCase()}${locale.language.toLowerCase()}")
        field.isAccessible = true
        field.get(this) as String
    } catch (e: Exception) {
        if (this.nameUSen.isNullOrEmpty()) {
            "Error"
        } else {
            this.nameUSen
        }
    }
}

