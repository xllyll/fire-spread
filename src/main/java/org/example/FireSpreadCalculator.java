package org.example;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class FireSpreadCalculator {

    private static final double EARTH_RADIUS = 6371; // 地球半径，单位：公里

    public static void main(String[] args) {
        // 示例起始点（纬度，经度）
        double startLat = 40.7128;
        double startLon = -74.0060;

        // 火灾蔓延参数
        double temperature = 27; // 温度
        double humidity = 36; // 湿度
        String terrain = "上坡"; // 地形
        String vegetation = "一般"; // 植被情况
        String weather = "多云"; // 天气情况
        int windForce = 18; // 风力等级
        String windDirection = "西"; // 风向

        // 计算火灾蔓延速率
        double spreadRate = calculateSpreadRate(temperature, humidity, terrain, vegetation, weather, windForce);

        // 火灾蔓延半径，单位：公里（1小时、3小时、5小时）
        double[] radii = {spreadRate * 1, spreadRate * 3, spreadRate * 5};

        // 生成GeoJSON对象
        JSONObject geojson = new JSONObject();
        geojson.put("type", "FeatureCollection");
        JSONArray features = new JSONArray();

        // 添加初始点
        JSONObject startPointFeature = new JSONObject();
        startPointFeature.put("type", "Feature");
        JSONObject startPointGeometry = new JSONObject();
        startPointGeometry.put("type", "Point");
        JSONArray startPointCoordinates = new JSONArray();
        startPointCoordinates.add(startLon);
        startPointCoordinates.add(startLat);
        startPointGeometry.put("coordinates", startPointCoordinates);
        startPointFeature.put("geometry", startPointGeometry);
        startPointFeature.put("properties", new JSONObject().put("description", "Initial Point"));
        features.add(startPointFeature);

        // 计算火灾蔓延区域并生成GeoJSON
        for (double radius : radii) {
            List<double[]> coordinates = calculateFireSpreadArea(startLat, startLon, radius, windDirection, windForce);
            JSONObject feature = new JSONObject();
            feature.put("type", "Feature");
            JSONObject geometry = new JSONObject();
            geometry.put("type", "Polygon");
            JSONArray polygon = new JSONArray();
            JSONArray ring = new JSONArray();

            for (double[] coord : coordinates) {
                JSONArray point = new JSONArray();
                point.add(coord[1]); // 经度
                point.add(coord[0]); // 纬度
                ring.add(point);
            }
            // 闭合环，确保第一个点和最后一个点相同，但不是通过引用
            if (!coordinates.isEmpty()) {
                double[] firstCoord = coordinates.get(0);
                JSONArray firstPoint = new JSONArray();
                firstPoint.add(firstCoord[1]);
                firstPoint.add(firstCoord[0]);
                ring.add(firstPoint);
            }

            polygon.add(ring);
            geometry.put("coordinates", polygon);
            feature.put("geometry", geometry);
            JSONObject properties = new JSONObject();
            properties.put("radius", radius);
            feature.put("properties", properties);
            features.add(feature);
        }

        geojson.put("features", features);

        // 输出GeoJSON
        System.out.println(geojson.toJSONString());
    }

    // 根据输入参数计算火灾蔓延速率
    private static double calculateSpreadRate(double temperature, double humidity, String terrain, String vegetation, String weather, int windForce) {
        double baseRate = 5; // 基本蔓延速率，单位：公里/小时

        // 根据温度调整蔓延速率
        if (temperature > 25) {
            baseRate += 1;
        }
        // 根据湿度调整蔓延速率
        if (humidity < 40) {
            baseRate += 1;
        }
        // 根据地形调整蔓延速率
        switch (terrain) {
            case "上坡":
                baseRate += 1;
                break;
            case "下坡":
                baseRate -= 0.5;
                break;
            case "平":
                baseRate += 0;
                break;
        }
        // 根据植被情况调整蔓延速率
        switch (vegetation) {
            case "密集":
                baseRate += 1.5;
                break;
            case "稀疏":
                baseRate += 0.5;
                break;
            case "一般":
                baseRate += 1;
                break;
            case "无":
                baseRate -= 1;
                break;
        }
        // 根据天气情况调整蔓延速率
        if ("晴朗".equals(weather)) {
            baseRate += 1;
        }
        // 根据风力等级调整蔓延速率
        baseRate += windForce * 0.5;

        return baseRate;
    }

    // 计算火灾蔓延区域的坐标点
    private static List<double[]> calculateFireSpreadArea(double lat, double lon, double radius, String windDirection, int windForce) {
        List<double[]> coordinates = new ArrayList<>();
        int numPoints = 72; // 生成更多点数，增加平滑度

        // 计算长轴和短轴
        double majorAxis = radius + windForce * 0.5; // 长轴
        double minorAxis = radius - windForce * 0.5; // 短轴

        // 获取风向对应的旋转角度
        double rotationAngle = getWindDirectionAngle(windDirection);

        for (int i = 0; i < numPoints; i++) {
            double angle = Math.toRadians((360.0 / numPoints) * i);
            double adjustedRadius = calculateAdjustedRadius(majorAxis, minorAxis, angle, rotationAngle);
            double[] adjustedCoords = adjustCoordinates(lat, lon, adjustedRadius, angle, rotationAngle);
            coordinates.add(adjustedCoords);
        }

        return coordinates;
    }

    // 计算根据风向和角度调整后的半径
    private static double calculateAdjustedRadius(double majorAxis, double minorAxis, double angle, double rotationAngle) {
        double relativeAngle = angle - rotationAngle;
        double cosRelativeAngle = Math.cos(relativeAngle);
        double sinRelativeAngle = Math.sin(relativeAngle);
        double baseRadius = (majorAxis * minorAxis) / Math.sqrt((minorAxis * cosRelativeAngle) * (minorAxis * cosRelativeAngle) + (majorAxis * sinRelativeAngle) * (majorAxis * sinRelativeAngle));
        double windEffect = calculateWindEffect(cosRelativeAngle, sinRelativeAngle, majorAxis, minorAxis);
        return baseRadius + windEffect;
    }

    // 根据相对角度计算风向对半径的影响
    private static double calculateWindEffect(double cosRelativeAngle, double sinRelativeAngle, double majorAxis, double minorAxis) {
        double windEffect = majorAxis - minorAxis;
        double effectFactor = (cosRelativeAngle * cosRelativeAngle) - (sinRelativeAngle * sinRelativeAngle);
        return windEffect * effectFactor;
    }

    // 根据旋转角度和角度调整坐标
    private static double[] adjustCoordinates(double lat, double lon, double radius, double angle, double rotationAngle) {
        double offsetLat = (radius / EARTH_RADIUS) * (180 / Math.PI) * Math.cos(angle - rotationAngle);
        double offsetLon = (radius / EARTH_RADIUS) * (180 / Math.PI) * Math.sin(angle - rotationAngle) / Math.cos(lat * Math.PI / 180);
        double newLat = lat + offsetLat;
        double newLon = lon + offsetLon;

        return new double[]{newLat, newLon};
    }

    // 获取风向对应的角度
    private static double getWindDirectionAngle(String windDirection) {
        switch (windDirection) {
            case "北":
                return Math.PI / 2;
            case "南":
                return 3 * Math.PI / 2;
            case "东":
                return 0;
            case "西":
                return Math.PI;
            case "东北":
                return Math.PI / 4;
            case "东南":
                return 7 * Math.PI / 4;
            case "西北":
                return 3 * Math.PI / 4;
            case "西南":
                return 5 * Math.PI / 4;
            default:
                return 0;
        }
    }
}
