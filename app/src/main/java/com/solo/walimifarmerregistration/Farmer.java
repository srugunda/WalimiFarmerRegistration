package com.solo.walimifarmerregistration;

public class Farmer {
   private String name;
   private String id;
   private String address;
   private String titleNum;
   private String gps;
   private String description;

    public Farmer(String name, String id, String address, String titleNum, String gps, String description) {
        this.name = name;
        this.id = id;
        this.address = address;
        this.titleNum = titleNum;
        this.gps = gps;
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

    public String getGps() {
        return gps;
    }

    public void setGps(String gps) {
        this.gps = gps;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
