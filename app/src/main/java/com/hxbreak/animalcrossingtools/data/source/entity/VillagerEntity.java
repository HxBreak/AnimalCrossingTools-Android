package com.hxbreak.animalcrossingtools.data.source.entity;

import com.google.gson.annotations.SerializedName;
import com.hxbreak.animalcrossingtools.adapter.ItemComparable;

public class VillagerEntity implements ItemComparable<Integer> {

    /**
     * id : 1
     * file-name : ant00
     * name : {"name-USen":"Cyrano","name-EUen":"Cyrano","name-EUde":"Theo","name-EUes":"Cirano","name-USes":"Cirano","name-EUfr":"Cyrano","name-USfr":"Cyrano","name-EUit":"Cirano","name-EUnl":"Cyrano","name-CNzh":"阳明","name-TWzh":"陽明","name-JPja":"さくらじま","name-KRko":"사지마","name-EUru":"Сирано"}
     * personality : Cranky
     * birthday-string : March 9th
     * birthday : 9/3
     * species : Anteater
     * gender : Male
     * subtype : B
     * hobby : Education
     * catch-phrase : ah-CHOO
     * icon_uri : https://acnhapi.com/v1/icons/villagers/1
     * image_uri : https://acnhapi.com/v1/images/villagers/1
     * bubble-color : #194c89
     * text-color : #fffad4
     * saying : Don't punch your nose to spite your face.
     * catch-translations : {"catch-USen":"ah-CHOO","catch-EUen":"ah-CHOO","catch-EUde":"schneuf","catch-EUes":"achús","catch-USes":"achús","catch-EUfr":"ATCHOUM","catch-USfr":"ATCHOUM","catch-EUit":"ett-CCIÙ","catch-EUnl":"ha-TSJOE","catch-CNzh":"有的","catch-TWzh":"有的","catch-JPja":"でごわす","catch-KRko":"임돠","catch-EUru":"апчхи"}
     */

    private int id;
    @SerializedName("file-name")
    private String filename;
    private LocalizationName name;
    private String personality;
    @SerializedName("birthday-string")
    private String birthdaystring;
    private String birthday;
    private String species;
    private String gender;
    private String subtype;
    private String hobby;
    @SerializedName("catch-phrase")
    private String catchphrase;
    private String icon_uri;
    private String image_uri;
    @SerializedName("bubble-color")
    private String bubblecolor;
    @SerializedName("text-color")
    private String textcolor;
    private String saying;
    @SerializedName("catch-translations")
    private CatchtranslationsBean catchtranslations;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public LocalizationName getName() {
        return name;
    }

    public void setName(LocalizationName name) {
        this.name = name;
    }

    public String getPersonality() {
        return personality;
    }

    public void setPersonality(String personality) {
        this.personality = personality;
    }

    public String getBirthdaystring() {
        return birthdaystring;
    }

    public void setBirthdaystring(String birthdaystring) {
        this.birthdaystring = birthdaystring;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getSpecies() {
        return species;
    }

    public void setSpecies(String species) {
        this.species = species;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getSubtype() {
        return subtype;
    }

    public void setSubtype(String subtype) {
        this.subtype = subtype;
    }

    public String getHobby() {
        return hobby;
    }

    public void setHobby(String hobby) {
        this.hobby = hobby;
    }

    public String getCatchphrase() {
        return catchphrase;
    }

    public void setCatchphrase(String catchphrase) {
        this.catchphrase = catchphrase;
    }

    public String getIcon_uri() {
        return icon_uri;
    }

    public void setIcon_uri(String icon_uri) {
        this.icon_uri = icon_uri;
    }

    public String getImage_uri() {
        return image_uri;
    }

    public void setImage_uri(String image_uri) {
        this.image_uri = image_uri;
    }

    public String getBubblecolor() {
        return bubblecolor;
    }

    public void setBubblecolor(String bubblecolor) {
        this.bubblecolor = bubblecolor;
    }

    public String getTextcolor() {
        return textcolor;
    }

    public void setTextcolor(String textcolor) {
        this.textcolor = textcolor;
    }

    public String getSaying() {
        return saying;
    }

    public void setSaying(String saying) {
        this.saying = saying;
    }

    public CatchtranslationsBean getCatchtranslations() {
        return catchtranslations;
    }

    public void setCatchtranslations(CatchtranslationsBean catchtranslations) {
        this.catchtranslations = catchtranslations;
    }

    @Override
    public Integer id() {
        return id;
    }

    public static class CatchtranslationsBean {
        /**
         * catch-USen : ah-CHOO
         * catch-EUen : ah-CHOO
         * catch-EUde : schneuf
         * catch-EUes : achús
         * catch-USes : achús
         * catch-EUfr : ATCHOUM
         * catch-USfr : ATCHOUM
         * catch-EUit : ett-CCIÙ
         * catch-EUnl : ha-TSJOE
         * catch-CNzh : 有的
         * catch-TWzh : 有的
         * catch-JPja : でごわす
         * catch-KRko : 임돠
         * catch-EUru : апчхи
         */

        @SerializedName("catch-USen")
        private String catchUSen;
        @SerializedName("catch-EUen")
        private String catchEUen;
        @SerializedName("catch-EUde")
        private String catchEUde;
        @SerializedName("catch-EUes")
        private String catchEUes;
        @SerializedName("catch-USes")
        private String catchUSes;
        @SerializedName("catch-EUfr")
        private String catchEUfr;
        @SerializedName("catch-USfr")
        private String catchUSfr;
        @SerializedName("catch-EUit")
        private String catchEUit;
        @SerializedName("catch-EUnl")
        private String catchEUnl;
        @SerializedName("catch-CNzh")
        private String catchCNzh;
        @SerializedName("catch-TWzh")
        private String catchTWzh;
        @SerializedName("catch-JPja")
        private String catchJPja;
        @SerializedName("catch-KRko")
        private String catchKRko;
        @SerializedName("catch-EUru")
        private String catchEUru;

        public String getCatchUSen() {
            return catchUSen;
        }

        public void setCatchUSen(String catchUSen) {
            this.catchUSen = catchUSen;
        }

        public String getCatchEUen() {
            return catchEUen;
        }

        public void setCatchEUen(String catchEUen) {
            this.catchEUen = catchEUen;
        }

        public String getCatchEUde() {
            return catchEUde;
        }

        public void setCatchEUde(String catchEUde) {
            this.catchEUde = catchEUde;
        }

        public String getCatchEUes() {
            return catchEUes;
        }

        public void setCatchEUes(String catchEUes) {
            this.catchEUes = catchEUes;
        }

        public String getCatchUSes() {
            return catchUSes;
        }

        public void setCatchUSes(String catchUSes) {
            this.catchUSes = catchUSes;
        }

        public String getCatchEUfr() {
            return catchEUfr;
        }

        public void setCatchEUfr(String catchEUfr) {
            this.catchEUfr = catchEUfr;
        }

        public String getCatchUSfr() {
            return catchUSfr;
        }

        public void setCatchUSfr(String catchUSfr) {
            this.catchUSfr = catchUSfr;
        }

        public String getCatchEUit() {
            return catchEUit;
        }

        public void setCatchEUit(String catchEUit) {
            this.catchEUit = catchEUit;
        }

        public String getCatchEUnl() {
            return catchEUnl;
        }

        public void setCatchEUnl(String catchEUnl) {
            this.catchEUnl = catchEUnl;
        }

        public String getCatchCNzh() {
            return catchCNzh;
        }

        public void setCatchCNzh(String catchCNzh) {
            this.catchCNzh = catchCNzh;
        }

        public String getCatchTWzh() {
            return catchTWzh;
        }

        public void setCatchTWzh(String catchTWzh) {
            this.catchTWzh = catchTWzh;
        }

        public String getCatchJPja() {
            return catchJPja;
        }

        public void setCatchJPja(String catchJPja) {
            this.catchJPja = catchJPja;
        }

        public String getCatchKRko() {
            return catchKRko;
        }

        public void setCatchKRko(String catchKRko) {
            this.catchKRko = catchKRko;
        }

        public String getCatchEUru() {
            return catchEUru;
        }

        public void setCatchEUru(String catchEUru) {
            this.catchEUru = catchEUru;
        }
    }
}
