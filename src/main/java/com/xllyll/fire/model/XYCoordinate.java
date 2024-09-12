package com.xllyll.fire.model;


public class XYCoordinate {

    private double latitude; // 纬度
    private double longitude; // 经度

    public XYCoordinate(double latitude, double longitude) {
        this.latitude = latitude; // 初始化纬度
        this.longitude = longitude; // 初始化经度
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
