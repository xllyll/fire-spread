package com.xllyll.fire;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.xllyll.fire.model.FireBarrierTools;
import com.xllyll.fire.model.FireSpreadModel;
import com.xllyll.fire.model.FireSpreadTools;
import com.xllyll.fire.model.XYCoordinate;
import org.geotools.geojson.geom.GeometryJSON;
import org.locationtech.jts.geom.*;
import org.geotools.geometry.jts.JTSFactoryFinder;

import java.io.StringWriter;
import java.util.*;

public class GeoJsonControlledFireSpread {


    public static void main1(String[] args) {
        try {

            XYCoordinate startPoint = new XYCoordinate(29.55,106.65); // 初始化起始点的坐标
            double windSpeed = 0.5; // 设置风速，单位：公里/小时
            double windDirection = 0; // 设置风向，单位：度（0表示北，90表示东，180表示南，270表示西）
            double terrainFactor = 1; // 设置地形影响因子（假设值）
            double humidity = 1; // 设置湿度，单位：百分比
            double temperature = 25; // 设置温度，单位：摄氏度
            String weatherCondition = "sunny"; // 设置天气情况
            String vegetationType = "normal"; // 设置植被情况
            //List<Coordinate> barriers = query.getBarriers(); // 设置隔离带坐标

            FireSpreadModel model = new FireSpreadModel(windSpeed, windDirection, terrainFactor, humidity, temperature, weatherCondition, vegetationType, null); // 创建火灾蔓延模型实例

            Map<String, List<XYCoordinate>> timeBasedCoordinates = new LinkedHashMap<>(); // 创建一个有序的Map用于存储按时间段分类的火灾影响区域坐标
            timeBasedCoordinates.put("1小时", model.simulateFireSpread(startPoint, 0.5,1)); // 模拟1小时的火灾范围并存储
            timeBasedCoordinates.put("3小时", model.simulateFireSpread(startPoint, 0.45,3)); // 模拟3小时的火灾范围并存储
            timeBasedCoordinates.put("5小时", model.simulateFireSpread(startPoint, 0.4,5)); // 模拟5小时的火灾范围并存储
            JSONObject geojson = FireSpreadTools.convertToGeoJSON(startPoint,timeBasedCoordinates);
            String json = geojson.toJSONString();
            System.out.println(json);



        } catch (Exception e) {
            System.err.println("An error occurred during the geometry operation: " + e.getMessage());
            e.printStackTrace();
        }
    }



    public static void main(String[] args) {
        try {
            // 创建一个GeometryFactory实例
            GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory(null);

            // 通过 GeometryFactory 创建 Point 对象
            Coordinate coordinate = new Coordinate(0, 0); // 起火点的坐标
            Point firePoint = geometryFactory.createPoint(coordinate);

            // 使用字符串表示的GeoJSON数据
            String geoJsonFire = createCircleGeoJson(0, 0, 10, 120); // 圆形A，表示起火蔓延的范围

            String geoJsonA = "{ \"type\": \"Feature\", \"geometry\": { \"type\": \"MultiPolygon\", \"coordinates\": [[[-2, 2], [-2, 2.5], [-6, 2.5], [-6, 2], [-2, 2]]]}, \"properties\": {} }";

            String geoJsonB = "{ \"type\": \"Feature\", \"geometry\": { \"type\": \"MultiPolygon\", \"coordinates\": [[[2.5, -10], [0, -8], [5, -5], [5, 0], [8,10],[8.1,10],[5.1,0],[5.1,-5],[0.1,-8],[2.6,-10],[2.5,-10]]]}, \"properties\": {} }"; // 隔离带B

//            String geoJsonC = "{ \"type\": \"Feature\", \"geometry\": { \"type\": \"Polygon\", \"coordinates\": [[[-5,-5], [-5.5, -5],[-5.5,0], [-10.5, 10], [-10.5, 10],[-5,0],[-5,-5]]]}, \"properties\": {} }"; // 隔离带B
            String geoJsonC = "{ \"type\": \"Feature\", \"geometry\": { \"type\": \"MultiPolygon\", \"coordinates\": [[[-5,0], [-5.5, 0], [-10.5, 10], [-10.5, 10],[-5,0]]]}, \"properties\": {} }"; // 隔离带B



            // 使用Fastjson2解析字符串为JSONObject
            JSONObject jsonObjectFire = JSON.parseObject(geoJsonFire);

            JSONObject jsonObjectA = JSON.parseObject(geoJsonA);
            JSONObject jsonObjectB = JSON.parseObject(geoJsonB);
            JSONObject jsonObjectC = JSON.parseObject(geoJsonC);




            JSONArray features = new JSONArray();
            features.add(jsonObjectA);
            features.add(jsonObjectB);
            features.add(jsonObjectC);

            JSONObject barrierJson = new JSONObject();
            barrierJson.put("type","FeatureCollection");
            barrierJson.put("features",features);

            // 从JSONObject中获取geometry字段
            String geometryFireString = jsonObjectFire.getJSONObject("geometry").toString();
//            String geometryAString = jsonObjectA.getJSONObject("geometry").toString();
//            String geometryBString = jsonObjectB.getJSONObject("geometry").toString();
//            String geometryCString = jsonObjectC.getJSONObject("geometry").toString();

            // 使用GeometryJSON来解析GeoJSON为JTS Geometry
            GeometryJSON geometryJSON = new GeometryJSON();

            Geometry geometryFire = geometryJSON.read(geometryFireString);


//            Geometry geometryA = geometryJSON.read(geometryAString);
//            Geometry geometryB = geometryJSON.read(geometryBString);
//            Geometry geometryC = geometryJSON.read(geometryCString);

            // 使用逐点直线检查方法寻找可达区域
//            Geometry reachableGeometry = FireBarrierTools.findReachablePoints(geometryFire, firePoint, new Geometry[]{geometryA,geometryB,geometryC});
            Geometry reachableGeometry = FireBarrierTools.findReachablePoints(geometryFire, firePoint, barrierJson);

            // 将可达点构造成线或区域（根据需要）
            if (!reachableGeometry.isEmpty()) {
                Geometry reachableArea = reachableGeometry;
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
                System.out.println("GeoJSON: \n\n" + resultGeoJsonObject.toJSONString()+"\n");
            } else {
                System.out.println("No reachable points found.");
            }
        }catch (Exception e){
            e.printStackTrace();
        }
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
