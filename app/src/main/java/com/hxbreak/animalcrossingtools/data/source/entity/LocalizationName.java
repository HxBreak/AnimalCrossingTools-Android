package com.hxbreak.animalcrossingtools.data.source.entity;

import androidx.room.ColumnInfo;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class LocalizationName implements Serializable {

    @ColumnInfo(name = "name_us_en")
    @SerializedName("name-USen")
    private String nameUSen;

    @ColumnInfo(name = "name_eu_en")
    @SerializedName("name-EUen")
    private String nameEUen;

    @ColumnInfo(name = "name_eu_de")
    @SerializedName("name-EUde")
    private String nameEUde;

    @ColumnInfo(name = "name_eu_es")
    @SerializedName("name-EUes")
    private String nameEUes;

    @ColumnInfo(name = "name_us_es")
    @SerializedName("name-USes")
    private String nameUSes;

    @ColumnInfo(name = "name_eu_fr")
    @SerializedName("name-EUfr")
    private String nameEUfr;

    @ColumnInfo(name = "name_us_fr")
    @SerializedName("name-USfr")
    private String nameUSfr;

    @ColumnInfo(name = "name_eu_it")
    @SerializedName("name-EUit")
    private String nameEUit;

    @ColumnInfo(name = "name_eu_nl")
    @SerializedName("name-EUnl")
    private String nameEUnl;

    @ColumnInfo(name = "name_cn_zh")
    @SerializedName("name-CNzh")
    private String nameCNzh;

    @ColumnInfo(name = "name_tw_zh")
    @SerializedName("name-TWzh")
    private String nameTWzh;

    @ColumnInfo(name = "name_jp_ja")
    @SerializedName("name-JPja")
    private String nameJPja;

    @ColumnInfo(name = "name_kr_ko")
    @SerializedName("name-KRko")
    private String nameKRko;

    @ColumnInfo(name = "name_eu_ru")
    @SerializedName("name-EUru")
    private String nameEUru;

    public String getNameUSen() {
        return nameUSen;
    }

    public void setNameUSen(String nameUSen) {
        this.nameUSen = nameUSen;
    }

    public String getNameEUen() {
        return nameEUen;
    }

    public void setNameEUen(String nameEUen) {
        this.nameEUen = nameEUen;
    }

    public String getNameEUde() {
        return nameEUde;
    }

    public void setNameEUde(String nameEUde) {
        this.nameEUde = nameEUde;
    }

    public String getNameEUes() {
        return nameEUes;
    }

    public void setNameEUes(String nameEUes) {
        this.nameEUes = nameEUes;
    }

    public String getNameUSes() {
        return nameUSes;
    }

    public void setNameUSes(String nameUSes) {
        this.nameUSes = nameUSes;
    }

    public String getNameEUfr() {
        return nameEUfr;
    }

    public void setNameEUfr(String nameEUfr) {
        this.nameEUfr = nameEUfr;
    }

    public String getNameUSfr() {
        return nameUSfr;
    }

    public void setNameUSfr(String nameUSfr) {
        this.nameUSfr = nameUSfr;
    }

    public String getNameEUit() {
        return nameEUit;
    }

    public void setNameEUit(String nameEUit) {
        this.nameEUit = nameEUit;
    }

    public String getNameEUnl() {
        return nameEUnl;
    }

    public void setNameEUnl(String nameEUnl) {
        this.nameEUnl = nameEUnl;
    }

    public String getNameCNzh() {
        return nameCNzh;
    }

    public void setNameCNzh(String nameCNzh) {
        this.nameCNzh = nameCNzh;
    }

    public String getNameTWzh() {
        return nameTWzh;
    }

    public void setNameTWzh(String nameTWzh) {
        this.nameTWzh = nameTWzh;
    }

    public String getNameJPja() {
        return nameJPja;
    }

    public void setNameJPja(String nameJPja) {
        this.nameJPja = nameJPja;
    }

    public String getNameKRko() {
        return nameKRko;
    }

    public void setNameKRko(String nameKRko) {
        this.nameKRko = nameKRko;
    }

    public String getNameEUru() {
        return nameEUru;
    }

    public void setNameEUru(String nameEUru) {
        this.nameEUru = nameEUru;
    }
}
