package com.xllyll.fire;

import org.geotools.geojson.geom.GeometryJSON;
import org.locationtech.jts.geom.*;

public class IsolationBeltAnalysis {

    public static void main(String[] args) {
        try {
            // 示例 GeoJSON 数据（火势蔓延区域和隔离带）
            String fireAreaString = "{\"type\":\"Polygon\", \"coordinates\": [[[0, 0], [5, 0], [5, 5], [0, 5], [0, 0]]]}"; // 火势蔓延区域
            String isolationBeltString1 = "{\"type\":\"Polygon\", \"coordinates\": [[[1, 1], [4, 1], [4, 4], [1, 4], [1, 1]]]}"; // 隔离带1，完全穿过
            String isolationBeltString2 = "{\"type\":\"Polygon\", \"coordinates\": [[[2, 2], [3, 2], [3, 3], [2, 3], [2, 2]]]}"; // 隔离带2，部分穿过
            String isolationBeltString3 = "{\"type\":\"Polygon\", \"coordinates\": [[[6, 6], [7, 6], [7, 7], [6, 7], [6, 6]]]}"; // 隔离带3，未穿过

            // 解析 GeoJSON 数据为 Geometry 对象
            GeometryJSON geometryJSON = new GeometryJSON();
            Geometry fireArea = geometryJSON.read(fireAreaString);  // 火势蔓延区域
            Geometry isolationBelt1 = geometryJSON.read(isolationBeltString1);  // 隔离带1
            Geometry isolationBelt2 = geometryJSON.read(isolationBeltString2);  // 隔离带2
            Geometry isolationBelt3 = geometryJSON.read(isolationBeltString3);  // 隔离带3

            // 判断隔离带与火势蔓延区域的交集
            analyzeIntersection(fireArea, isolationBelt1, 1); // 隔离带1
            analyzeIntersection(fireArea, isolationBelt2, 2); // 隔离带2
            analyzeIntersection(fireArea, isolationBelt3, 3); // 隔离带3

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 分析隔离带与火势蔓延区域的交集情况
    public static void analyzeIntersection(Geometry fireArea, Geometry isolationBelt, int isolationBeltIndex) {
        // 获取交集区域
        Geometry intersection = fireArea.intersection(isolationBelt);

        if (intersection.isEmpty()) {
            System.out.println("隔离带" + isolationBeltIndex + " 没有穿过火势蔓延区域");
        } else {
            // 获取交集区域的面积
            double fireAreaSize = fireArea.getArea();
            double intersectionSize = intersection.getArea();

            // 完全穿过：如果交集区域面积大于火势蔓延区域的 50%
            if (intersectionSize > fireAreaSize * 0.5) {
                System.out.println("隔离带" + isolationBeltIndex + " 完全穿过火势蔓延区域");
            }
            // 仅一边穿过：如果交集区域比较小，并且形状接近细长（通过长宽比判断）
            else if (intersectionSize > fireAreaSize * 0.05) {
                // 判断交集的长宽比来区分细长区域
                if (isLongShape(intersection)) {
                    System.out.println("隔离带" + isolationBeltIndex + " 仅一边穿过火势蔓延区域");
                } else {
                    System.out.println("隔离带" + isolationBeltIndex + " 部分穿过火势蔓延区域");
                }
            }
            // 其他情况：部分穿过
            else {
                System.out.println("隔离带" + isolationBeltIndex + " 部分穿过火势蔓延区域");
            }
        }
    }

    // 判断交集区域是否接近细长形状
    public static boolean isLongShape(Geometry intersection) {
        Envelope envelope = intersection.getEnvelope().getEnvelopeInternal();
        double width = envelope.getWidth();
        double height = envelope.getHeight();

        // 判断长宽比是否大于某个阈值（例如 2:1），表示细长区域
        return width > height * 2;
    }
}
