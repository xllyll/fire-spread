package com.xllyll.fire;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.xllyll.fire.model.FireSpreadModel;
import com.xllyll.fire.model.FireSpreadTools;
import com.xllyll.fire.model.XYCoordinate;
import org.geotools.geojson.geom.GeometryJSON;
import org.locationtech.jts.geom.*;
import org.geotools.geometry.jts.JTSFactoryFinder;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GeoJsonControlledFireSpread {
    public static void main(String[] args) {
        try {

            XYCoordinate startPoint = new XYCoordinate(0,0); // 初始化起始点的坐标
            double windSpeed = 10000.0; // 设置风速，单位：公里/小时
            double windDirection = 0; // 设置风向，单位：度（0表示北，90表示东，180表示南，270表示西）
            double terrainFactor = 1; // 设置地形影响因子（假设值）
            double humidity = 1; // 设置湿度，单位：百分比
            double temperature = 25; // 设置温度，单位：摄氏度
            String weatherCondition = "sunny"; // 设置天气情况
            String vegetationType = "normal"; // 设置植被情况
            //List<Coordinate> barriers = query.getBarriers(); // 设置隔离带坐标

            FireSpreadModel model = new FireSpreadModel(windSpeed, windDirection, terrainFactor, humidity, temperature, weatherCondition, vegetationType, null); // 创建火灾蔓延模型实例

            Map<String, List<XYCoordinate>> timeBasedCoordinates = new LinkedHashMap<>(); // 创建一个有序的Map用于存储按时间段分类的火灾影响区域坐标
//            timeBasedCoordinates.put("1小时", model.simulateFireSpread(startPoint, 1)); // 模拟1小时的火灾范围并存储
//            timeBasedCoordinates.put("3小时", model.simulateFireSpread(startPoint, 3)); // 模拟3小时的火灾范围并存储
//            timeBasedCoordinates.put("5小时", model.simulateFireSpread(startPoint, 5)); // 模拟5小时的火灾范围并存储
//            JSONObject geojson = FireSpreadTools.convertToGeoJSON(startPoint,timeBasedCoordinates);


            // 创建一个GeometryFactory实例
            GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory(null);

            // 使用字符串表示的GeoJSON数据
//            String geoJsonA = createCircleGeoJson(0, 0, 5, 32); // 圆形A，表示起火蔓延的范围
            String geoJsonA = FireSpreadTools.convertToGeoJSON(model.simulateFireSpread(startPoint, 1)).toJSONString();
            String geoJsonB = "{ \"type\": \"Feature\", \"geometry\": { \"type\": \"Polygon\", \"coordinates\": [[[2, -2], [2.5, -2], [2.5, 6], [2, 6], [2, -2]]]}, \"properties\": {} }"; // 隔离带B
            String geoJsonC = "{ \"type\": \"Feature\", \"geometry\": { \"type\": \"Polygon\", \"coordinates\": [[[-2, 2], [-2, 2.5], [-6, 2.5], [-6, 2], [-2, 2]]]}, \"properties\": {} }"; // 隔离带B

            // 使用Fastjson2解析字符串为JSONObject
            JSONObject jsonObjectA = JSON.parseObject(geoJsonA);
            JSONObject jsonObjectB = JSON.parseObject(geoJsonB);
            JSONObject jsonObjectC = JSON.parseObject(geoJsonC);

            // 从JSONObject中获取geometry字段

            String geometryAString = jsonObjectA.getJSONObject("geometry").toString();
            String geometryBString = jsonObjectB.getJSONObject("geometry").toString();
            String geometryCString = jsonObjectC.getJSONObject("geometry").toString();

            // 使用GeometryJSON来解析GeoJSON为JTS Geometry
            GeometryJSON geometryJSON = new GeometryJSON();
            Geometry geometryA = geometryJSON.read(geometryAString);
            Geometry geometryB = geometryJSON.read(geometryBString);
            Geometry geometryC = geometryJSON.read(geometryCString);

            // 计算多边形A中不与隔离带B重叠的部分
            Geometry difference = geometryA.difference(geometryB).difference(geometryC);

            // 创建起火点的几何对象（假设起火点在(0,0)）
            Point firePoint = geometryFactory.createPoint(new Coordinate(0, 0));

            // 使用逐点直线检查方法寻找可达区域
            List<Coordinate> reachablePoints = findReachablePoints(difference, firePoint, new Geometry[]{geometryB,geometryC});

            // 将可达点构造成线或区域（根据需要）
            if (!reachablePoints.isEmpty()) {
                Coordinate endCoordinate = new Coordinate(reachablePoints.get(0).x, reachablePoints.get(0).y);
                reachablePoints.add(endCoordinate);
                Coordinate[] cs = reachablePoints.toArray(new Coordinate[0]);
                Geometry reachableArea = geometryFactory.createPolygon(cs);//.convexHull();

                // 将结果转换为GeoJSON格式
                StringWriter writer = new StringWriter();
                geometryJSON.write(reachableArea, writer);
                String resultGeoJson = writer.toString();

                // 构造完整的GeoJSON Feature对象
                JSONObject resultGeoJsonObject = new JSONObject();
                resultGeoJsonObject.put("type", "Feature");
                resultGeoJsonObject.put("geometry", JSON.parseObject(resultGeoJson));
                resultGeoJsonObject.put("properties", new JSONObject()); // 添加空的属性字段

                // 输出结果
                System.out.println("Controlled Fire Spread GeoJSON: " + resultGeoJsonObject.toJSONString());
            } else {
                System.out.println("No reachable points found.");
            }

        } catch (Exception e) {
            System.err.println("An error occurred during the geometry operation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 使用逐点直线检查方法寻找可达点
    private static List<Coordinate> findReachablePoints(Geometry difference, Point firePoint, Geometry[] barriers) {
        List<Coordinate> reachablePoints = new ArrayList<>();
        for (int i = 0; i < difference.getNumGeometries(); i++) {
            Geometry part = difference.getGeometryN(i);
            reachablePoints.addAll(getReachablePointsByLines(part, firePoint, barriers));
        }
        return reachablePoints;
    }


    // 获取一个区域中满足条件的可达点（通过直线与隔离带的关系）
    private static List<Coordinate> getReachablePointsByLines(Geometry area, Point firePoint, Geometry[] barriers) {
        List<Coordinate> reachablePoints = new ArrayList<>();
        Coordinate[] areaCoordinates = area.getCoordinates();
        for (Coordinate coord : areaCoordinates) {
            LineString lineToCoord = createLine(firePoint.getCoordinate(), coord);
            boolean isHav = true;
            for (int i = 0; i< barriers.length; i++) {
                Geometry barrier = barriers[i];
                Geometry intersection = lineToCoord.intersection(barrier);
                // 判断交点数量
                if (intersection.getNumPoints() > 1) {
                    // 如果直线与隔离带B的交点数量少于2个，认为该点是可达的
                    isHav = false;
                }
            }
            if(isHav){
                reachablePoints.add(coord);
            }
        }
        return reachablePoints;
    }

    // 创建从起火点到目标点的直线
    private static LineString createLine(Coordinate origin, Coordinate destination) {
        Coordinate[] coordinates = {origin, destination};
        return new GeometryFactory().createLineString(coordinates);
    }

    // 生成一个近似圆形的GeoJSON字符串
    private static String createCircleGeoJson(double centerX, double centerY, double radius, int sides) {
        StringBuilder geoJsonBuilder = new StringBuilder();
        geoJsonBuilder.append("{ \"type\": \"Feature\", \"geometry\": { \"type\": \"Polygon\", \"coordinates\": [[");

        for (int i = 0; i < sides; i++) {
            double angle = 2 * Math.PI * i / sides;
            double x = centerX + radius * Math.cos(angle);
            double y = centerY + radius * Math.sin(angle);
            geoJsonBuilder.append("[").append(x).append(",").append(y).append("],");
        }

        // 闭合多边形，最后一个点与第一个点相同
        geoJsonBuilder.append("[").append(centerX + radius).append(",").append(centerY).append("]]]}, \"properties\": {} }");

        return geoJsonBuilder.toString();
    }
}
