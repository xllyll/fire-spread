package org.example;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.text.SimpleDateFormat;
import java.util.*;

class FireSpreadModel {
    private double windSpeed; // 风速，单位：公里/小时
    private double windDirection; // 风向，单位：度（0-360）
    private double terrainFactor; // 地形影响因子
    private double humidity; // 湿度，单位：百分比（0-100）
    private double temperature; // 温度，单位：摄氏度
    private String weatherCondition; // 天气情况
    private String vegetationType; // 植被情况
    private List<Coordinate> barriers; // 隔离带坐标列表

    // 构造方法，用于初始化风速、风向、地形因子、湿度、温度、天气情况、植被情况和隔离带
    public FireSpreadModel(double windSpeed, double windDirection, double terrainFactor, double humidity, double temperature, String weatherCondition, String vegetationType, List<Coordinate> barriers) {
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
    public List<Coordinate> simulateFireSpread(Coordinate startPoint, double durationHours) {
        List<Coordinate> affectedArea = new ArrayList<>(); // 创建一个列表用于存储火灾影响区域的坐标

        double baseSpreadRate = 0.5; // 基础蔓延速率，单位：公里/小时

        // 遍历每个角度，计算每个方向上的蔓延距离
        for (double angle = 0; angle < 360; angle += 10) { // 每10度计算一个点
            double radians = Math.toRadians(angle); // 将角度转换为弧度
            double spreadFactor = calculateSpreadFactor(angle); // 计算基于风向的蔓延因子
            double weatherFactor = calculateWeatherFactor(); // 计算天气影响因子
            double vegetationFactor = calculateVegetationFactor(); // 计算植被影响因子
            double barrierFactor = calculateBarrierFactor(startPoint); // 计算隔离带影响因子
            double spreadDistance = baseSpreadRate * spreadFactor * weatherFactor * vegetationFactor * barrierFactor * durationHours * terrainFactor * calculateHumidityFactor() * calculateTemperatureFactor(); // 计算火灾在该方向的蔓延距离

            double newLat = startPoint.latitude + (spreadDistance / 111.32) * Math.cos(radians); // 计算新的纬度（每度纬度大约等于111.32公里）
            double newLon = startPoint.longitude + (spreadDistance / (111.32 * Math.cos(Math.toRadians(startPoint.latitude)))) * Math.sin(radians); // 计算新的经度（考虑纬度变化的影响）
            affectedArea.add(new Coordinate(newLat, newLon)); // 将新的坐标添加到影响区域列表中
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
            case "heavy rain":
                return 0.5; // 大雨显著减缓火灾蔓延
            case "snow":
                return 0.6; // 小雪减缓火灾蔓延
            case "heavy snow":
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
    private double calculateBarrierFactor(Coordinate point) {
        for (Coordinate barrier : barriers) {
            if (distance(point, barrier) < 0.5) { // 假设0.5公里内有隔离带影响
                return 0.5; // 有隔离带，蔓延速率显著降低
            }
        }
        return 1.0; // 无隔离带影响
    }

    // 计算两点间的距离
    private double distance(Coordinate p1, Coordinate p2) {
        double earthRadius = 6371; // 地球半径，单位：公里
        double dLat = Math.toRadians(p2.latitude - p1.latitude);
        double dLon = Math.toRadians(p2.longitude - p1.longitude);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(p1.latitude)) * Math.cos(Math.toRadians(p2.latitude)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadius * c; // 返回距离，单位：公里
    }

    // 坐标类，用于存储和表示经纬度信息
    static class Coordinate {
        double latitude; // 纬度
        double longitude; // 经度

        Coordinate(double latitude, double longitude) {
            this.latitude = latitude; // 初始化纬度
            this.longitude = longitude; // 初始化经度
        }
    }

    // 将计算结果转换为GeoJSON格式
    public static String convertToGeoJSON(Coordinate startCoordinate, Map<String, List<Coordinate>> timeBasedCoordinates) {
        JSONObject geoJson = new JSONObject(); // 创建GeoJSON对象
        geoJson.put("type", "FeatureCollection"); // 设置GeoJSON类型为FeatureCollection
        JSONArray features = new JSONArray(); // 创建一个数组用于存储Feature对象
        // 添加初始点
        JSONObject startPointFeature = new JSONObject();
        startPointFeature.put("type", "Feature");
        JSONObject startPointGeometry = new JSONObject();
        startPointGeometry.put("type", "Point");
        JSONArray startPointCoordinates = new JSONArray();
        startPointCoordinates.add(startCoordinate.longitude);
        startPointCoordinates.add(startCoordinate.latitude);
        startPointGeometry.put("coordinates", startPointCoordinates);
        startPointFeature.put("geometry", startPointGeometry);
        startPointFeature.put("properties", new JSONObject().fluentPut("description", "起火点"));
        features.add(startPointFeature);

        // 遍历每个时间段的坐标
        for (Map.Entry<String, List<Coordinate>> entry : timeBasedCoordinates.entrySet()) {
            String timeLabel = entry.getKey(); // 获取时间标签
            List<Coordinate> coordinates = entry.getValue(); // 获取对应的坐标列表

            // 将坐标列表转换为GeoJSON格式的多边形
            JSONObject feature = new JSONObject(); // 创建Feature对象
            feature.put("type", "Feature"); // 设置Feature类型
            JSONObject geometry = new JSONObject(); // 创建几何对象
            geometry.put("type", "Polygon"); // 设置几何类型为Polygon
            JSONArray coordsArray = new JSONArray(); // 创建一个数组用于存储多边形坐标
            JSONArray polygon = new JSONArray(); // 创建一个多边形数组

            // 添加坐标到多边形
            for (Coordinate coord : coordinates) {
                JSONArray coords = new JSONArray(); // 创建一个数组用于存储经纬度
                coords.add(coord.longitude); // 添加经度
                coords.add(coord.latitude); // 添加纬度
                polygon.add(coords); // 将经纬度数组添加到多边形数组中
            }

            // 闭合多边形
            if (!coordinates.isEmpty()) {
                Coordinate firstCoord = coordinates.get(0); // 获取第一个坐标
                JSONArray firstCoords = new JSONArray(); // 创建一个数组用于存储第一个经纬度
                firstCoords.add(firstCoord.longitude); // 添加经度
                firstCoords.add(firstCoord.latitude); // 添加纬度
                polygon.add(firstCoords); // 将第一个经纬度数组添加到多边形数组中，以闭合多边形
            }

            coordsArray.add(polygon); // 将多边形数组添加到坐标数组中
            geometry.put("coordinates", coordsArray); // 将坐标数组添加到几何对象中

            JSONObject properties = new JSONObject(); // 创建属性对象
            properties.put("time", timeLabel); // 添加时间属性

            double area = calculatePolygonArea(coordinates); // 计算多边形面积
            properties.put("area", area); // 添加面积属性

            feature.put("geometry", geometry); // 将几何对象添加到Feature对象中
            feature.put("properties", properties); // 将属性对象添加到Feature对象中
            features.add(feature); // 将Feature对象添加到Feature数组中
        }

        geoJson.put("features", features); // 将Feature数组添加到GeoJSON对象中
        return geoJson.toJSONString(); // 返回GeoJSON格式的字符串
    }

    // 将计算结果转换为CZML格式
    public static String convertToCZML(Coordinate startCoordinate,int duration, Map<String, List<Coordinate>> timeBasedCoordinates) {
        JSONArray czml = new JSONArray(); // 创建CZML数组
        JSONObject document = new JSONObject(); // 创建CZML文档对象
        document.put("id", "document");
        document.put("name", "Sequential Polygon Display Example");
        document.put("version", "1.0");

        // 获取当前日期并设置为当天的00:00:00
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date currentTime = calendar.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String startTimeString = sdf.format(currentTime);

        // 计算总时长
        int totalDuration = timeBasedCoordinates.size() * duration; // 每个多边形展示3秒
        calendar.add(Calendar.SECOND, totalDuration);
        String endTimeString = sdf.format(calendar.getTime());

        // 设置时钟信息
        JSONObject clock = new JSONObject();
        clock.put("interval", startTimeString + "/" + endTimeString);
        clock.put("currentTime", startTimeString);
        clock.put("multiplier", 1);
        clock.put("range", "CLAMPED");
        clock.put("step", "SYSTEM_CLOCK_MULTIPLIER");
        document.put("clock", clock);
        czml.add(document); // 添加文档对象到CZML数组

        // 添加初始点
        JSONObject startPoint = new JSONObject();
        startPoint.put("id", "startPoint");
        startPoint.put("name", "起火点");
        JSONObject startPointPosition = new JSONObject();
        startPointPosition.put("cartographicDegrees", Arrays.asList(startCoordinate.longitude, startCoordinate.latitude, 0));
        startPoint.put("position", startPointPosition);
        JSONObject startPointPoint = new JSONObject();
        startPointPoint.put("color", new JSONObject().fluentPut("rgba", Arrays.asList(255, 0, 0, 255)));
        startPointPoint.put("pixelSize", 10);
        startPoint.put("point", startPointPoint);
        czml.add(startPoint);

        // 动态多边形动画设置
        calendar.setTime(currentTime); // 重置时间为起始时间

        int polygonIndex = 1;
        for (Map.Entry<String, List<Coordinate>> entry : timeBasedCoordinates.entrySet()) {
            String timeLabel = entry.getKey();
            List<Coordinate> coordinates = entry.getValue();

            JSONObject polygon = new JSONObject();
            polygon.put("id", "fire-polygon-" + polygonIndex);
            polygon.put("name", timeLabel);

            JSONArray polygonCoordinates = new JSONArray();
            for (Coordinate coord : coordinates) {
                polygonCoordinates.addAll(Arrays.asList(coord.longitude, coord.latitude, 0));
            }
            if (!coordinates.isEmpty()) {
                Coordinate firstCoord = coordinates.get(0);
                polygonCoordinates.addAll(Arrays.asList(firstCoord.longitude, firstCoord.latitude, 0));
            }

            calendar.add(Calendar.SECOND, duration);
            String startInterval = sdf.format(calendar.getTime());
            String availability = startInterval + "/" + endTimeString;
            polygon.put("availability", availability);

            JSONObject polygonGraphics = new JSONObject();
            polygonGraphics.put("positions", new JSONObject().fluentPut("cartographicDegrees", polygonCoordinates));
            polygonGraphics.put("material", new JSONObject().fluentPut("solidColor", new JSONObject()
                    .fluentPut("color", new JSONObject().fluentPut("rgba", Arrays.asList(255, 165, 0, 128)))));
            polygonGraphics.put("outline", true);
            polygonGraphics.put("outlineColor", new JSONObject().fluentPut("rgba", Arrays.asList(255, 0, 0, 255)));
            polygon.put("polygon", polygonGraphics);

            // 计算多边形的中心点
            Coordinate center = calculateCentroid(coordinates);

            // 添加标签
            JSONObject label = new JSONObject();
            label.put("text", timeLabel);
            label.put("font", "24px sans-serif");
            label.put("fillColor", new JSONObject().fluentPut("rgba", Arrays.asList(255, 255, 255, 255)));
            label.put("outlineColor", new JSONObject().fluentPut("rgba", Arrays.asList(0, 0, 0, 255)));
            label.put("outlineWidth", 2);
            label.put("style", "FILL_AND_OUTLINE");
            label.put("horizontalOrigin", "CENTER");
            label.put("verticalOrigin", "BOTTOM");

            polygon.put("label", label);

            JSONObject labelPosition = new JSONObject();
            labelPosition.put("cartographicDegrees", Arrays.asList(center.longitude, center.latitude, 0));
            polygon.put("position", labelPosition);
            czml.add(polygon);

            polygonIndex++;
        }

        return czml.toJSONString(); // 返回CZML格式的字符串
    }

    // 计算多边形的中心点
    public static Coordinate calculateCentroid(List<Coordinate> coordinates) {
        double centroidX = 0, centroidY = 0;
        int numPoints = coordinates.size();

        for (Coordinate coord : coordinates) {
            centroidX += coord.longitude;
            centroidY += coord.latitude;
        }

        return new Coordinate(centroidY / numPoints, centroidX / numPoints);
    }


    // 计算多边形的面积，单位：平方公里
    public static double calculatePolygonArea(List<Coordinate> coordinates) {
        int n = coordinates.size();
        if (n < 3) return 0.0; // 多边形顶点数少于3，面积为0
        double area = 0.0;
        for (int i = 0; i < n; i++) {
            Coordinate p1 = coordinates.get(i);
            Coordinate p2 = coordinates.get((i + 1) % n);
            area += Math.toRadians(p2.longitude - p1.longitude) * (2 + Math.sin(Math.toRadians(p1.latitude)) + Math.sin(Math.toRadians(p2.latitude)));
        }
        area = area * 6371 * 6371 / 2.0;
        return Math.abs(area);
    }

    public static void main(String[] args) {
        Coordinate startPoint = new Coordinate(29.55,106.65); // 初始化起始点的坐标
        double windSpeed = 10.0; // 设置风速，单位：公里/小时
        double windDirection = 90.0; // 设置风向，单位：度（0表示北，90表示东，180表示南，270表示西）
        double terrainFactor = 1.2; // 设置地形影响因子（假设值）
        double humidity = 30.0; // 设置湿度，单位：百分比
        double temperature = 25.0; // 设置温度，单位：摄氏度
        String weatherCondition = "sunny"; // 设置天气情况
        String vegetationType = "dense"; // 设置植被情况
        List<Coordinate> barriers = Arrays.asList(
                new Coordinate(30.1, 114.1),
                new Coordinate(30.2, 114.2)
        ); // 设置隔离带坐标

        FireSpreadModel model = new FireSpreadModel(windSpeed, windDirection, terrainFactor, humidity, temperature, weatherCondition, vegetationType, barriers); // 创建火灾蔓延模型实例

        Map<String, List<Coordinate>> timeBasedCoordinates = new LinkedHashMap<>(); // 创建一个有序的Map用于存储按时间段分类的火灾影响区域坐标
        timeBasedCoordinates.put("1 hour", model.simulateFireSpread(startPoint, 1)); // 模拟1小时的火灾范围并存储
        timeBasedCoordinates.put("3 hours", model.simulateFireSpread(startPoint, 3)); // 模拟3小时的火灾范围并存储
        timeBasedCoordinates.put("5 hours", model.simulateFireSpread(startPoint, 5)); // 模拟5小时的火灾范围并存储

        String geoJson = convertToGeoJSON(startPoint, timeBasedCoordinates); // 将火灾范围结果转换为GeoJSON格式
        System.out.println("GeoJSON:"); // 输出GeoJSON前的提示
        System.out.println(geoJson); // 输出GeoJSON格式的结果

        String czml = convertToCZML(startPoint, 3,timeBasedCoordinates); // 将火灾范围结果转换为CZML格式
        System.out.println("CZML:"); // 输出CZML前的提示
        System.out.println(czml); // 输出CZML格式的结果

        // 计算并输出每个时间段的多边形面积
        for (Map.Entry<String, List<Coordinate>> entry : timeBasedCoordinates.entrySet()) {
            String timeLabel = entry.getKey();
            List<Coordinate> coordinates = entry.getValue();
            double area = calculatePolygonArea(coordinates); // 计算多边形面积
            System.out.println("Area after " + timeLabel + ": " + area + " square kilometers"); // 输出面积
        }
    }
}
