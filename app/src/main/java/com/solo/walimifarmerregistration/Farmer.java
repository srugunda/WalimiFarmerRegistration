package com.solo.walimifarmerregistration;

import android.graphics.Bitmap;

public class Farmer {
   private String name;
   private String id;
   private String address;
   private String titleNum;
   private String latitude;
   private String longitude;
   private String description;
   public Bitmap imageBitmap;


    public Farmer(String name, String id, String address, String titleNum, String latitude, String longitude, String description, Bitmap bitmap) {
        this.name = name;
        this.id = id;
        this.address = address;
        this.titleNum = titleNum;
        this.latitude = latitude;
        this.longitude = longitude;
        this.description = description;
        this.imageBitmap = bitmap;
    }

    public Farmer(String name, String id, String address, String titleNum, String latitude, String longitude, String description) {
        this.name = name;
        this.id = id;
        this.address = address;
        this.titleNum = titleNum;
        this.latitude = latitude;
        this.longitude = longitude;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTitleNum() {
        return titleNum;
    }

    public void setTitleNum(String titleNum) {
        this.titleNum = titleNum;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
