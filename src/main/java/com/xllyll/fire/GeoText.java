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
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GeoText {
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

    private static Geometry test(FireSpreadModel model,Coordinate[] cds){
        List<Geometry> geometries = new ArrayList<>();
        for (Coordinate cd:cds){
            XYCoordinate coordinate = new XYCoordinate(cd.y,cd.x);
            List<XYCoordinate> coordinates = model.simulateFireSpreadByMinute(coordinate, 0.5,120.0);
            JSONObject geojson = FireSpreadTools.convertToGeoJSON(coordinates);
            String geometryAString = geojson.getJSONObject("geometry").toString();
            GeometryJSON geometryJSON = new GeometryJSON();
            Geometry geometry = null;
            try {
                geometry = geometryJSON.read(geometryAString);
            } catch (IOException e) {
                e.printStackTrace();
            }
            geometries.add(geometry);
        }
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
        return all;
    }
    public static void main(String[] args) {
        XYCoordinate startPoint = new XYCoordinate(30.149904,106.659964); // 初始化起始点的坐标
        double windSpeed = 0.8; // 设置风速，单位：公里/小时
        double windDirection = 90; // 设置风向，单位：度（0表示北，90表示东，180表示南，270表示西）
        double terrainFactor = 1; // 设置地形影响因子（假设值）
        double humidity = 1; // 设置湿度，单位：百分比
        double temperature = 25; // 设置温度，单位：摄氏度
        String weatherCondition = "sunny"; // 设置天气情况
        String vegetationType = "normal"; // 设置植被情况

        FireSpreadModel model = new FireSpreadModel(windSpeed, windDirection, terrainFactor, humidity, temperature, weatherCondition, vegetationType, null); // 创建火灾蔓延模型实例

        List<XYCoordinate> s1s = model.simulateFireSpread(startPoint, 0.5,1);
        JSONObject geojson1 = FireSpreadTools.convertToGeoJSON(s1s);
        String geometryAString1 = geojson1.getJSONObject("geometry").toString();
        GeometryJSON geometryJSON1 = new GeometryJSON();
        Geometry geometry1 = null;
        try {
            geometry1 = geometryJSON1.read(geometryAString1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Geometry geometry2 = test(model,geometry1.getCoordinates());
        Geometry geometry3 = test(model,geometry2.getCoordinates());
        List<Geometry> glist = new ArrayList<>();
        glist.add(geometry1);
        glist.add(geometry2);
        glist.add(geometry3);

        String json = convertGeometriesToGeoJSON(glist);

        System.out.println(json);

    }

}
