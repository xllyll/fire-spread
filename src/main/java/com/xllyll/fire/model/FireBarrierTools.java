package com.xllyll.fire.model;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.geotools.geojson.geom.GeometryJSON;
import org.locationtech.jts.geom.*;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public class FireBarrierTools {


    public static Geometry findReachablePoints(Geometry fire, Point firePoint, JSONObject barrierJSON) {
        String type = barrierJSON.getString("type");
        List<Geometry> geometryList = new ArrayList<>();
        // 使用GeometryJSON来解析GeoJSON为JTS Geometry
        GeometryJSON geometryJSON = new GeometryJSON();
        if (type.equals("FeatureCollection")){
            JSONArray features = barrierJSON.getJSONArray("features");
            if (features!=null&&features.size()>0){
                for (int i = 0; i < features.size(); i++) {
                    try {
                        JSONObject feature = features.getJSONObject(i);
                        Geometry geometry = geometryJSON.read(feature.getString("geometry"));
                        geometryList.add(geometry);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }else if (type.equals("Feature")) {
            try {
                Geometry geometry = geometryJSON.read(barrierJSON.getString("geometry"));
                geometryList.add(geometry);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return findReachablePoints(fire,firePoint,geometryList.toArray(new Geometry[0]));
    }

    // 使用逐点直线检查方法寻找可达点
    public static Geometry findReachablePoints(Geometry fire, Point firePoint, Geometry[] barriers) {

        Geometry difference = fire;
        for (Geometry barr:barriers){
            int belt = analyzeIntersection(difference,barr);
            if (belt==0){
                //无交集
                continue;
            } else if (belt==2) {
                // 有交集【完全穿过火势蔓延区域】
                difference  = difference.difference(barr);
                // 如果结果是一个 GeometryCollection，遍历每个几何对象
                if (difference instanceof GeometryCollection) {
                    GeometryCollection geometryCollection = (GeometryCollection) difference;
                    // 遍历所有几何对象
                    for (int i = 0; i < geometryCollection.getNumGeometries(); i++) {
                        Geometry geometry = geometryCollection.getGeometryN(i);
                        // 判断每个面是否包含起火点
                        if (geometry instanceof Polygon) {
                            Polygon polygon = (Polygon) geometry;
                            if (polygon.contains(firePoint)) {
//                            containingFirePoint.add(polygon); // 如果包含起火点，保留该面
                                difference = polygon;
                                break;
                            }
                        }
                    }
                }
            } else if (belt==3) {
                // 有交集（部分穿过火势蔓延区域）
                //difference  = difference.difference(barr);
                Geometry inter = difference.intersection(barr);
                List<Coordinate> reachablePoints = new ArrayList<>();
                for (int i = 0; i < difference.getNumGeometries(); i++) {
                    Geometry part = difference.getGeometryN(i);
                    reachablePoints.addAll(getReachablePointsByLines(part, firePoint, inter));
                }
                // 创建 LinearRing 和 Polygon
                GeometryFactory geometryFactory = new GeometryFactory();
                difference = geometryFactory.createPolygon(reachablePoints.toArray(new Coordinate[0]));
            } else {
                // 有交集（完全包含在火势蔓延区域）
                difference  = difference.difference(barr);
                List<Coordinate> reachablePoints = new ArrayList<>();
                for (int i = 0; i < difference.getNumGeometries(); i++) {
                    Geometry part = difference.getGeometryN(i);
                    reachablePoints.addAll(getReachablePointsByLines(part, firePoint, barr));
                }
                // 创建 LinearRing 和 Polygon
                GeometryFactory geometryFactory = new GeometryFactory();

                difference = geometryFactory.createPolygon(reachablePoints.toArray(new Coordinate[0]));
//                return reachablePoints;
            }

        }
        GeometryJSON differenceJSON = new GeometryJSON();
        StringWriter differenceWriter = new StringWriter();
        try {
            differenceJSON.write(difference, differenceWriter);
            String geojson = differenceWriter.toString();
            System.out.println("====>>>:"+geojson);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return difference;
    }

    private static List<Coordinate> getReachablePointsByLines(Geometry area, Point firePoint, Geometry barrier) {
        return getReachablePointsByLines(area,firePoint,new Geometry[]{barrier});
    }

    /**
     * 获取一个区域中满足条件的可达点（通过直线与隔离带的关系）
     * @param area 区域
     * @param firePoint 起火点
     * @param barriers 隔离带
     * @return
     */
    private static List<Coordinate> getReachablePointsByLines(Geometry area, Point firePoint, Geometry[] barriers) {
        List<Coordinate> reachablePoints = new ArrayList<>();
        Coordinate[] areaCoordinates = area.getCoordinates();
        for (Coordinate coord : areaCoordinates) {
            LineString lineToCoord = createLine(firePoint.getCoordinate(), coord);
            //boolean isHav = true;
            Coordinate addCoord = coord;
            for (int i = 0; i< barriers.length; i++) {
                Geometry barrier = barriers[i];
                Geometry intersection = lineToCoord.intersection(barrier);
                // 判断交点数量
                if (intersection.getNumPoints() > 1) {
                    // 获取相交点中，最靠近起火点的点数据
                    if (intersection.getCoordinates()[0].distance(firePoint.getCoordinate()) < intersection.getCoordinates()[1].distance(firePoint.getCoordinate())) {
                        addCoord = intersection.getCoordinates()[0];
                    } else {
                        addCoord = intersection.getCoordinates()[1];
                    }
                }
            }
            reachablePoints.add(addCoord);

        }
        return reachablePoints;
    }

    // 创建从起火点到目标点的直线
    private static LineString createLine(Coordinate origin, Coordinate destination) {
        Coordinate[] coordinates = {origin, destination};
        return new GeometryFactory().createLineString(coordinates);
    }

    // 分析隔离带与火势蔓延区域的交集情况
    public static int analyzeIntersection(Geometry fireArea, Geometry isolationBelt) {

        Geometry fireBoundary = fireArea.getBoundary();
        Geometry beltBoundary = isolationBelt.getBoundary();

        Geometry intersectionPolygon = fireArea.intersection(isolationBelt);
        Geometry intersectionLine = fireBoundary.intersection(beltBoundary);

        if (intersectionPolygon.getCoordinates().length>0){
            if(intersectionLine.getCoordinates().length>0){
                if (intersectionLine.getCoordinates().length==2){
                    System.out.println("隔离带 仅一边穿过火势蔓延区域");
                    return 1;
                }else{
                    System.out.println("隔离带 完全穿过火势蔓延区域");
                    return 2;
                }
            }else{
                System.out.println("隔离带 包含在火势蔓延区域");
                return 3;
            }
        }
        System.out.println("隔离带 没有穿过火势蔓延区域");
        return 0;
    }

}
