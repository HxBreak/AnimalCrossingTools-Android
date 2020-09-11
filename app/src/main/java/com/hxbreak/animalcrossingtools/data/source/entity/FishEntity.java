package com.hxbreak.animalcrossingtools.data.source.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class FishEntity {

    private int id;
    @SerializedName("file-name")
    private String filename;
    private CommonName name;
    private ItemAvailability availability;
    private String shadow;
    private int price;
    @SerializedName("price-cj")
    private int priceCj;
    @SerializedName("catch-phrase")
    private String catchphrase;
    @SerializedName("museum-phrase")
    private String museumphrase;
    private String image_uri;
    private String icon_uri;

    @Expose(deserialize = false, serialize = false)
    private String localeName;

    public String getLocaleName() {
        return localeName;
    }

    public void setLocaleName(String localeName) {
        this.localeName = localeName;
    }

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

    public CommonName getName() {
        return name;
    }

    public void setName(CommonName name) {
        this.name = name;
    }

    public ItemAvailability getAvailability() {
        return availability;
    }

    public void setAvailability(ItemAvailability availability) {
        this.availability = availability;
    }

    public String getShadow() {
        return shadow;
    }

    public void setShadow(String shadow) {
        this.shadow = shadow;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getPriceCj() {
        return priceCj;
    }

    public void setPriceCj(int priceCj) {
        this.priceCj = priceCj;
    }

    public String getCatchphrase() {
        return catchphrase;
    }

    public void setCatchphrase(String catchphrase) {
        this.catchphrase = catchphrase;
    }

    public String getMuseumphrase() {
        return museumphrase;
    }

    public void setMuseumphrase(String museumphrase) {
        this.museumphrase = museumphrase;
    }

    public String getImage_uri() {
        return image_uri;
    }

    public void setImage_uri(String image_uri) {
        this.image_uri = image_uri;
    }

    public String getIcon_uri() {
        return icon_uri;
    }

    public void setIcon_uri(String icon_uri) {
        this.icon_uri = icon_uri;
    }

    public static class CommonName {
        @SerializedName("name-USen")
        private String nameUSen;
        @SerializedName("name-EUen")
        private String nameEUen;
        @SerializedName("name-EUde")
        private String nameEUde;
        @SerializedName("name-EUes")
        private String nameEUes;
        @SerializedName("name-USes")
        private String nameUSes;
        @SerializedName("name-EUfr")
        private String nameEUfr;
        @SerializedName("name-USfr")
        private String nameUSfr;
        @SerializedName("name-EUit")
        private String nameEUit;
        @SerializedName("name-EUnl")
        private String nameEUnl;
        @SerializedName("name-CNzh")
        private String nameCNzh;
        @SerializedName("name-TWzh")
        private String nameTWzh;
        @SerializedName("name-JPja")
        private String nameJPja;
        @SerializedName("name-KRko")
        private String nameKRko;
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


}

