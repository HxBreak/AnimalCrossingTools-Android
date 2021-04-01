package com.hxbreak.animalcrossingtools.data.source.entity

import androidx.room.ColumnInfo
import com.google.gson.annotations.SerializedName

data class LocalizationName(
    @ColumnInfo(name = "name_us_en")
    @SerializedName("name-USen")
    var nameUSen: String? = null,

    @ColumnInfo(name = "name_eu_en")
    @SerializedName("name-EUen")
    val nameEUen: String? = null,

    @ColumnInfo(name = "name_eu_de")
    @SerializedName("name-EUde")
    val nameEUde: String? = null,

    @ColumnInfo(name = "name_eu_es")
    @SerializedName("name-EUes")
    val nameEUes: String? = null,

    @ColumnInfo(name = "name_us_es")
    @SerializedName("name-USes")
    val nameUSes: String? = null,

    @ColumnInfo(name = "name_eu_fr")
    @SerializedName("name-EUfr")
    val nameEUfr: String? = null,

    @ColumnInfo(name = "name_us_fr")
    @SerializedName("name-USfr")
    val nameUSfr: String? = null,

    @ColumnInfo(name = "name_eu_it")
    @SerializedName("name-EUit")
    val nameEUit: String? = null,

    @ColumnInfo(name = "name_eu_nl")
    @SerializedName("name-EUnl")
    val nameEUnl: String? = null,

    @ColumnInfo(name = "name_cn_zh")
    @SerializedName("name-CNzh")
    val nameCNzh: String? = null,

    @ColumnInfo(name = "name_tw_zh")
    @SerializedName("name-TWzh")
    val nameTWzh: String? = null,

    @ColumnInfo(name = "name_jp_ja")
    @SerializedName("name-JPja")
    val nameJPja: String? = null,

    @ColumnInfo(name = "name_kr_ko")
    @SerializedName("name-KRko")
    val nameKRko: String? = null,

    @ColumnInfo(name = "name_eu_ru")
    @SerializedName("name-EUru")
    val nameEUru: String? = null,
)