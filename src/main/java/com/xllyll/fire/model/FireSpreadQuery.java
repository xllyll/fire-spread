package com.xllyll.fire.model;


import java.util.ArrayList;
import java.util.List;

public class FireSpreadQuery {
    private boolean showStartPoint = false; //是否展示起始点
    private XYCoordinate startPoint = new XYCoordinate(29.55,106.65); // 初始化起始点的坐标
    private double windSpeed = 5.0; // 设置风速，单位：公里/小时
    private double windDirection = 90.0; // 设置风向，单位：度（0表示北，90表示东，180表示南，270表示西）
    private double terrainFactor = 1.2; // 设置地形影响因子（假设值）
    private double humidity = 30.0; // 设置湿度，单位：百分比
    private double temperature = 25.0; // 设置温度，单位：摄氏度
    private WeatherCondition weatherCondition = WeatherCondition.SUNNY; // 设置天气情况
    private VegetationType vegetationType = VegetationType.NORMAL; // 设置植被情况
    private List<XYCoordinate> barriers = new ArrayList<>();

    public boolean isShowStartPoint() {
        return showStartPoint;
    }

    public void setShowStartPoint(boolean showStartPoint) {
        this.showStartPoint = showStartPoint;
    }

    public XYCoordinate getStartPoint() {
        return startPoint;
    }

    public void setStartPoint(XYCoordinate startPoint) {
        this.startPoint = startPoint;
    }

    public double getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(double windSpeed) {
        this.windSpeed = windSpeed;
    }

    public double getWindDirection() {
        return windDirection;
    }

    public void setWindDirection(double windDirection) {
        this.windDirection = windDirection;
    }

    public double getTerrainFactor() {
        return terrainFactor;
    }

    public void setTerrainFactor(double terrainFactor) {
        this.terrainFactor = terrainFactor;
    }

    public double getHumidity() {
        return humidity;
    }

    public void setHumidity(double humidity) {
        this.humidity = humidity;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public WeatherCondition getWeatherCondition() {
        return weatherCondition;
    }

    public void setWeatherCondition(WeatherCondition weatherCondition) {
        this.weatherCondition = weatherCondition;
    }

    public VegetationType getVegetationType() {
        return vegetationType;
    }

    public void setVegetationType(VegetationType vegetationType) {
        this.vegetationType = vegetationType;
    }

    public List<XYCoordinate> getBarriers() {
        return barriers;
    }

    public void setBarriers(List<XYCoordinate> barriers) {
        this.barriers = barriers;
    }
}
