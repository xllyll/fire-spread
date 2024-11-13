package com.xllyll.fire;

import com.alibaba.fastjson2.JSONObject;
import com.xllyll.fire.model.FireSpreadModel;
import com.xllyll.fire.model.FireSpreadTools;
import com.xllyll.fire.model.XYCoordinate;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.geojson.geom.GeometryJSON;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        /**
         *
         */
        XYCoordinate startPoint = new XYCoordinate(29.55,106.65); // 初始化起始点的坐标
        double windSpeed = 5.0; // 设置风速，单位：公里/小时
        double windDirection = 90; // 设置风向，单位：度（0表示北，90表示东，180表示南，270表示西）
        double terrainFactor = 1.5; // 设置地形影响因子（假设值）
        double humidity = 1; // 设置湿度，单位：百分比
        double temperature = 25; // 设置温度，单位：摄氏度
        String weatherCondition = "sunny"; // 设置天气情况
        String vegetationType = "normal"; // 设置植被情况
        //List<Coordinate> barriers = query.getBarriers(); // 设置隔离带坐标

        FireSpreadModel model = new FireSpreadModel(windSpeed, windDirection, terrainFactor, humidity, temperature, weatherCondition, vegetationType, null); // 创建火灾蔓延模型实例
//        List<XYCoordinate> coordinates = model.simulateFireSpread(startPoint,0.5,1);
//        String geojson = FireSpreadTools.convertToGeoJSON(coordinates).toJSONString();
//        System.out.println(geojson);

        long time1 = System.currentTimeMillis();
        List<Geometry> geometries = new ArrayList<>();
        buildGeometry(model,geometries,startPoint,0.5,0,300.0,20.0);
        System.out.println("");

        // 创建一个GeometryFactory实例
        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory(null);
        Geometry all = null;
        for (Geometry geometry:geometries){
            if (all==null){
                all = geometry;
            }else{
                all = all.union(geometry);
            }
        }
        String geo = convertToGeoJSON(all);

        long time2 = System.currentTimeMillis();
        long t=time2-time1;
//        String geo = convertGeometriesToGeoJSON(geometries);
        System.out.println(geo);

    }

    private static void buildGeometry(FireSpreadModel model,List<Geometry> geometries,XYCoordinate coordinate,double baseSpreadRate,double beginTime,double endTime,double nodeTime){

        List<XYCoordinate> coordinates = model.simulateFireSpreadByMinute(coordinate, baseSpreadRate,nodeTime);
        JSONObject geojson = FireSpreadTools.convertToGeoJSON(coordinates);
        String geometryAString = geojson.getJSONObject("geometry").toString();
        GeometryJSON geometryJSON = new GeometryJSON();
        try {
            Geometry geometry = geometryJSON.read(geometryAString);
            geometries.add(geometry);
            if (beginTime<endTime){
                for (XYCoordinate xyCoordinate:coordinates){
                    buildGeometry(model,geometries,xyCoordinate,baseSpreadRate+0.1,beginTime+nodeTime,endTime,nodeTime);
                }
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public static String convertToGeoJSON(Geometry geometry) {
        GeometryJSON geometryJSON = new GeometryJSON();
        StringWriter writer = new StringWriter();
        try {
            geometryJSON.write(geometry, writer); // 将几何对象写入字符串
        } catch (Exception e) {
            e.printStackTrace();
        }
        return writer.toString();
    }

    public static String convertGeometriesToGeoJSON(List<Geometry> geometries) {
        try {
            // 创建一个 FeatureCollection 来包含所有的几何对象
            DefaultFeatureCollection featureCollection = new DefaultFeatureCollection();

            // 构建 SimpleFeatureType
            SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();
            typeBuilder.setName("Feature");
            typeBuilder.add("geometry", Geometry.class);
            SimpleFeatureType featureType = typeBuilder.buildFeatureType();

            // 将每个 Geometry 对象添加为 Feature
            for (Geometry geometry : geometries) {
                SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(featureType);
                featureBuilder.add(geometry);
                SimpleFeature feature = featureBuilder.buildFeature(null);
                featureCollection.add(feature);
            }

            // 将 FeatureCollection 转换为 GeoJSON
            FeatureJSON featureJSON = new FeatureJSON();
            StringWriter writer = new StringWriter();
            featureJSON.writeFeatureCollection(featureCollection, writer);

            return writer.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
