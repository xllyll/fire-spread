package com.xllyll.fire.model;

import java.util.ArrayList;
import java.util.List;

public class FireSpreadModel {
    private double windSpeed; // 风速，单位：公里/小时
    private double windDirection; // 风向，单位：度（0-360）
    private double terrainFactor; // 地形影响因子
    private double humidity; // 湿度，单位：百分比（0-100）
    private double temperature; // 温度，单位：摄氏度
    private String weatherCondition; // 天气情况
    private String vegetationType; // 植被情况
    private List<XYCoordinate> barriers; // 隔离带坐标列表

    // 构造方法，用于初始化风速、风向、地形因子、湿度、温度、天气情况、植被情况和隔离带
    public FireSpreadModel(double windSpeed, double windDirection, double terrainFactor, double humidity, double temperature, String weatherCondition, String vegetationType, List<XYCoordinate> barriers) {
        this.windSpeed = windSpeed; // 初始化风速
        this.windDirection = windDirection; // 初始化风向
        this.terrainFactor = terrainFactor; // 初始化地形因子
        this.humidity = humidity; // 初始化湿度
        this.temperature = temperature; // 初始化温度
        this.weatherCondition = weatherCondition; // 初始化天气情况
        this.vegetationType = vegetationType; // 初始化植被情况
        this.barriers = barriers; // 初始化隔离带
    }

    // 模拟火灾传播的主要方法
    public List<XYCoordinate> simulateFireSpread(XYCoordinate startPoint, double durationHours) {
        List<XYCoordinate> affectedArea = new ArrayList<>(); // 创建一个列表用于存储火灾影响区域的坐标

        double baseSpreadRate = 0.5; // 基础蔓延速率，单位：公里/小时

        // 遍历每个角度，计算每个方向上的蔓延距离
        for (double angle = 0; angle < 360; angle += 3) { // 每5度计算一个点
            double radians = Math.toRadians(angle); // 将角度转换为弧度
            double spreadFactor = calculateSpreadFactor(angle); // 计算基于风向的蔓延因子
            double weatherFactor = calculateWeatherFactor(); // 计算天气影响因子
            double vegetationFactor = calculateVegetationFactor(); // 计算植被影响因子
            double barrierFactor = calculateBarrierFactor(startPoint); // 计算隔离带影响因子
            double spreadDistance = baseSpreadRate * spreadFactor * weatherFactor * vegetationFactor * barrierFactor * durationHours * terrainFactor * calculateHumidityFactor() * calculateTemperatureFactor(); // 计算火灾在该方向的蔓延距离

            double newLat = startPoint.getLatitude() + (spreadDistance / 111.32) * Math.cos(radians); // 计算新的纬度（每度纬度大约等于111.32公里）
            double newLon = startPoint.getLongitude() + (spreadDistance / (111.32 * Math.cos(Math.toRadians(startPoint.getLatitude())))) * Math.sin(radians); // 计算新的经度（考虑纬度变化的影响）
            affectedArea.add(new XYCoordinate(newLat, newLon)); // 将新的坐标添加到影响区域列表中
        }

        return affectedArea; // 返回影响区域的坐标列表
    }

    // 计算基于风向的蔓延因子
    private double calculateSpreadFactor(double angle) {
        double angleDifference = Math.abs(angle - windDirection); // 计算当前方向与风向的夹角
        if (angleDifference > 180) {
            angleDifference = 360 - angleDifference; // 确保角度差在0到180度之间
        }
        double spreadFactor = 1 + (windSpeed / 10) * Math.cos(Math.toRadians(angleDifference)); // 计算蔓延因子
        return Math.max(spreadFactor, 0); // 确保蔓延因子为非负值
    }

    // 计算基于湿度的影响因子
    private double calculateHumidityFactor() {
        return 1 - (humidity / 100); // 湿度越高，蔓延速率越低
    }

    // 计算基于温度的影响因子
    private double calculateTemperatureFactor() {
        return 1 + (temperature / 30); // 温度越高，蔓延速率越高（假设30摄氏度为基准）
    }

    // 计算基于天气的影响因子
    private double calculateWeatherFactor() {
        switch (weatherCondition.toLowerCase()) {
            case "sunny":
                return 1.2; // 晴朗天气加速火灾蔓延
            case "cloudy":
                return 1.0; // 阴天对火灾蔓延无显著影响
            case "rain":
                return 0.7; // 下雨减缓火灾蔓延
            case "heavyRain":
                return 0.5; // 大雨显著减缓火灾蔓延
            case "snow":
                return 0.6; // 小雪减缓火灾蔓延
            case "heavySnow":
                return 0.4; // 大雪显著减缓火灾蔓延
            default:
                return 1.0; // 默认无显著影响
        }
    }

    // 计算基于植被的影响因子
    private double calculateVegetationFactor() {
        switch (vegetationType.toLowerCase()) {
            case "none":
                return 0.5; // 无植被，蔓延速率较低
            case "sparse":
                return 0.8; // 稀疏植被，蔓延速率中等
            case "normal":
                return 1.0; // 一般植被，蔓延速率正常
            case "dense":
                return 1.5; // 密集植被，蔓延速率较高
            default:
                return 1.0; // 默认正常蔓延速率
        }
    }

    // 计算基于隔离带的影响因子
    private double calculateBarrierFactor(XYCoordinate point) {
        if(barriers==null){
            return 1.0;
        }
        for (XYCoordinate barrier : barriers) {
            if (distance(point, barrier) < 0.5) { // 假设0.5公里内有隔离带影响
                return 0.5; // 有隔离带，蔓延速率显著降低
            }
        }
        return 1.0; // 无隔离带影响
    }

    // 计算两点间的距离
    private double distance(XYCoordinate p1, XYCoordinate p2) {
        double earthRadius = 6371; // 地球半径，单位：公里
        double dLat = Math.toRadians(p2.getLatitude() - p1.getLatitude());
        double dLon = Math.toRadians(p2.getLongitude() - p1.getLongitude());
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(p1.getLatitude())) * Math.cos(Math.toRadians(p2.getLatitude())) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadius * c; // 返回距离，单位：公里
    }



}
