package com.xllyll.fire.model;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

import java.text.SimpleDateFormat;
import java.util.*;

public class FireSpreadTools {
    public static JSONObject convertToGeoJSON(List<XYCoordinate> XYCoordinates){
        // 将坐标列表转换为GeoJSON格式的多边形
        JSONObject feature = new JSONObject(); // 创建Feature对象
        feature.put("type", "Feature"); // 设置Feature类型
        JSONObject geometry = new JSONObject(); // 创建几何对象
        geometry.put("type", "Polygon"); // 设置几何类型为Polygon
        JSONArray coordsArray = new JSONArray(); // 创建一个数组用于存储多边形坐标
        JSONArray polygon = new JSONArray(); // 创建一个多边形数组

        // 添加坐标到多边形
        for (XYCoordinate coord : XYCoordinates) {
            JSONArray coords = new JSONArray(); // 创建一个数组用于存储经纬度
            coords.add(coord.getLongitude()); // 添加经度
            coords.add(coord.getLatitude()); // 添加纬度
            polygon.add(coords); // 将经纬度数组添加到多边形数组中
        }

        // 闭合多边形
        if (!XYCoordinates.isEmpty()) {
            XYCoordinate firstCoord = XYCoordinates.get(0); // 获取第一个坐标
            JSONArray firstCoords = new JSONArray(); // 创建一个数组用于存储第一个经纬度
            firstCoords.add(firstCoord.getLongitude()); // 添加经度
            firstCoords.add(firstCoord.getLatitude()); // 添加纬度
            polygon.add(firstCoords); // 将第一个经纬度数组添加到多边形数组中，以闭合多边形
        }

        coordsArray.add(polygon); // 将多边形数组添加到坐标数组中
        geometry.put("coordinates", coordsArray); // 将坐标数组添加到几何对象中

        JSONObject properties = new JSONObject(); // 创建属性对象
        properties.put("time", "0"); // 添加时间属性

        double area = calculatePolygonArea(XYCoordinates); // 计算多边形面积
        properties.put("area", area); // 添加面积属性

        feature.put("geometry", geometry); // 将几何对象添加到Feature对象中
        feature.put("properties", properties); // 将属性对象添加到Feature对象中
        return feature; // 返回GeoJSON格式的字符串
    }
    // 将计算结果转换为GeoJSON格式
    public static JSONObject convertToGeoJSON(XYCoordinate startXYCoordinate, Map<String, List<XYCoordinate>> timeBasedCoordinates) {
        JSONObject geoJson = new JSONObject(); // 创建GeoJSON对象
        geoJson.put("type", "FeatureCollection"); // 设置GeoJSON类型为FeatureCollection
        JSONArray features = new JSONArray(); // 创建一个数组用于存储Feature对象
        // 添加初始点
        JSONObject startPointFeature = new JSONObject();
        startPointFeature.put("type", "Feature");
        JSONObject startPointGeometry = new JSONObject();
        startPointGeometry.put("type", "Point");
        JSONArray startPointCoordinates = new JSONArray();
        startPointCoordinates.add(startXYCoordinate.getLongitude());
        startPointCoordinates.add(startXYCoordinate.getLatitude());
        startPointGeometry.put("coordinates", startPointCoordinates);
        startPointFeature.put("geometry", startPointGeometry);
        startPointFeature.put("properties", new JSONObject().fluentPut("description", "起火点"));
        features.add(startPointFeature);

        // 遍历每个时间段的坐标
        for (Map.Entry<String, List<XYCoordinate>> entry : timeBasedCoordinates.entrySet()) {
            String timeLabel = entry.getKey(); // 获取时间标签
            List<XYCoordinate> XYCoordinates = entry.getValue(); // 获取对应的坐标列表

            // 将坐标列表转换为GeoJSON格式的多边形
            JSONObject feature = new JSONObject(); // 创建Feature对象
            feature.put("type", "Feature"); // 设置Feature类型
            JSONObject geometry = new JSONObject(); // 创建几何对象
            geometry.put("type", "Polygon"); // 设置几何类型为Polygon
            JSONArray coordsArray = new JSONArray(); // 创建一个数组用于存储多边形坐标
            JSONArray polygon = new JSONArray(); // 创建一个多边形数组

            // 添加坐标到多边形
            for (XYCoordinate coord : XYCoordinates) {
                JSONArray coords = new JSONArray(); // 创建一个数组用于存储经纬度
                coords.add(coord.getLongitude()); // 添加经度
                coords.add(coord.getLatitude()); // 添加纬度
                polygon.add(coords); // 将经纬度数组添加到多边形数组中
            }

            // 闭合多边形
            if (!XYCoordinates.isEmpty()) {
                XYCoordinate firstCoord = XYCoordinates.get(0); // 获取第一个坐标
                JSONArray firstCoords = new JSONArray(); // 创建一个数组用于存储第一个经纬度
                firstCoords.add(firstCoord.getLongitude()); // 添加经度
                firstCoords.add(firstCoord.getLatitude()); // 添加纬度
                polygon.add(firstCoords); // 将第一个经纬度数组添加到多边形数组中，以闭合多边形
            }

            coordsArray.add(polygon); // 将多边形数组添加到坐标数组中
            geometry.put("coordinates", coordsArray); // 将坐标数组添加到几何对象中

            JSONObject properties = new JSONObject(); // 创建属性对象
            properties.put("time", timeLabel); // 添加时间属性

            double area = calculatePolygonArea(XYCoordinates); // 计算多边形面积
            properties.put("area", area); // 添加面积属性

            feature.put("geometry", geometry); // 将几何对象添加到Feature对象中
            feature.put("properties", properties); // 将属性对象添加到Feature对象中
            features.add(feature); // 将Feature对象添加到Feature数组中
        }

        geoJson.put("features", features); // 将Feature数组添加到GeoJSON对象中
        return geoJson; // 返回GeoJSON格式的字符串
    }

    // 将计算结果转换为CZML格式
    public static JSONArray convertToCZML(XYCoordinate startXYCoordinate, int duration, Map<String, List<XYCoordinate>> timeBasedCoordinates) {
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

        if (startXYCoordinate != null) {
            // 添加初始点
            JSONObject startPoint = new JSONObject();
            startPoint.put("id", "startPoint");
            startPoint.put("name", "起火点");
            JSONObject startPointPosition = new JSONObject();
            startPointPosition.put("cartographicDegrees", Arrays.asList(startXYCoordinate.getLongitude(), startXYCoordinate.getLatitude(), 0));
            startPoint.put("position", startPointPosition);
            JSONObject startPointPoint = new JSONObject();
            startPointPoint.put("color", new JSONObject().fluentPut("rgba", Arrays.asList(255, 0, 0, 255)));
            startPointPoint.put("pixelSize", 6);
            startPoint.put("point", startPointPoint);
            czml.add(startPoint);
        }

        // 动态多边形动画设置
        calendar.setTime(currentTime); // 重置时间为起始时间

        int polygonIndex = 1;

        for (Map.Entry<String, List<XYCoordinate>> entry : timeBasedCoordinates.entrySet()) {
            String timeLabel = entry.getKey();
            List<XYCoordinate> XYCoordinates = entry.getValue();

            JSONObject polygon = new JSONObject();
            polygon.put("id", "fire-polygon-" + polygonIndex);
            polygon.put("name", timeLabel);

            JSONArray polygonCoordinates = new JSONArray();
            for (XYCoordinate coord : XYCoordinates) {
                polygonCoordinates.addAll(Arrays.asList(coord.getLongitude(), coord.getLatitude(), 0));
            }
            if (!XYCoordinates.isEmpty()) {
                XYCoordinate firstCoord = XYCoordinates.get(0);
                polygonCoordinates.addAll(Arrays.asList(firstCoord.getLongitude(), firstCoord.getLatitude(), 0));
            }

            calendar.add(Calendar.SECOND, duration);
            String startInterval = sdf.format(calendar.getTime());
            String availability = startInterval + "/" + endTimeString;
            polygon.put("availability", availability);

            JSONObject polygonGraphics = new JSONObject();
            polygonGraphics.put("positions", new JSONObject().fluentPut("cartographicDegrees", polygonCoordinates));
            polygonGraphics.put("height", 50 * (3 - polygonIndex));
            polygonGraphics.put("material", new JSONObject().fluentPut("solidColor", new JSONObject()
                    .fluentPut("color", new JSONObject().fluentPut("rgba", Arrays.asList(255, 0, 0, 64)))));
            polygonGraphics.put("outline", true);
            polygonGraphics.put("outlineColor", new JSONObject().fluentPut("rgba", Arrays.asList(255, 0, 0, 255)));
            polygonGraphics.put("style", "FILL_AND_OUTLINE");
            polygon.put("polygon", polygonGraphics);

            // 计算多边形的中心点
            XYCoordinate center = calculateCentroid(XYCoordinates);

            // 添加标签
            JSONObject label = new JSONObject();
            label.put("text", timeLabel);
            label.put("font", "20px sans-serif");
            label.put("fillColor", new JSONObject().fluentPut("rgba", Arrays.asList(255, 255, 255, 255)));
            label.put("outlineColor", new JSONObject().fluentPut("rgba", Arrays.asList(64, 64, 64, 255)));
            label.put("outlineWidth", 1);
            label.put("style", "FILL_AND_OUTLINE");
            label.put("horizontalOrigin", "CENTER");
            label.put("verticalOrigin", "BOTTOM");

            polygon.put("label", label);

            JSONObject labelPosition = new JSONObject();
            labelPosition.put("cartographicDegrees", Arrays.asList(center.getLongitude(), center.getLatitude(), 50 * (3 - polygonIndex)));
            polygon.put("position", labelPosition);
            czml.add(polygon);

            polygonIndex++;
        }

        return czml;
    }

    // 计算多边形的label位置
    public static XYCoordinate calculateCentroid(List<XYCoordinate> XYCoordinates) {
        if (XYCoordinates.isEmpty()) {
            return null; // 如果坐标列表为空，返回null
        }
        XYCoordinate eastMost = XYCoordinates.get(0); // 初始化东边最大的坐标为列表的第一个坐标
        for (XYCoordinate coord : XYCoordinates) {
            if (coord.getLatitude() > eastMost.getLongitude()) {
                eastMost = coord; // 更新东边最大的坐标
            }
        }
        return eastMost;
    }


    // 计算多边形的面积，单位：平方公里
    public static double calculatePolygonArea(List<XYCoordinate> XYCoordinates) {
        int n = XYCoordinates.size();
        if (n < 3) return 0.0; // 多边形顶点数少于3，面积为0
        double area = 0.0;
        for (int i = 0; i < n; i++) {
            XYCoordinate p1 = XYCoordinates.get(i);
            XYCoordinate p2 = XYCoordinates.get((i + 1) % n);
            area += Math.toRadians(p2.getLongitude() - p1.getLongitude()) * (2 + Math.sin(Math.toRadians(p1.getLatitude())) + Math.sin(Math.toRadians(p2.getLatitude())));
        }
        area = area * 6371 * 6371 / 2.0;
        return Math.abs(area);
    }

}
