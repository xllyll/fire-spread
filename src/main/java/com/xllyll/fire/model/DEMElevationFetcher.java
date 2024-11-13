package com.xllyll.fire.model;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.GridCoverage2DReader;
import org.geotools.coverage.grid.io.GridFormatFinder;
import org.geotools.geometry.DirectPosition2D;
import org.opengis.geometry.DirectPosition;
import java.io.File;
import java.io.IOException;

public class DEMElevationFetcher {

    private GridCoverage2D coverage;

    public DEMElevationFetcher(String demFilePath) throws IOException {
        File demFile = new File(demFilePath);
        AbstractGridFormat format = GridFormatFinder.findFormat(demFile);

        if (format == null) {
            throw new IOException("Unsupported DEM file format.");
        }

        GridCoverage2DReader reader = format.getReader(demFile);
        coverage = reader.read(null);
    }

    /**
     * 获取指定经纬度的海拔高度
     * @param longitude 经度
     * @param latitude 纬度
     * @return 海拔高度（米）
     * @throws IOException
     */
    public double getElevation(double longitude, double latitude) throws IOException {
        DirectPosition position = new DirectPosition2D(longitude, latitude);
        double[] elevation = new double[1];
        // 从DEM文件中获取高度值
        coverage.evaluate(position, elevation);
        return elevation[0];
    }

    public static void main(String[] args) {
        try {
            long time = System.currentTimeMillis();
            String demFilePath = "/Users/xllyll/Downloads/chongqi.tif";  // 替换为实际DEM文件路径
            DEMElevationFetcher fetcher = new DEMElevationFetcher(demFilePath);

            double longitude = 106.85;  // 示例经度
            double latitude = 29.55;    // 示例纬度
            double elevation = fetcher.getElevation(longitude, latitude);

            long time2 = System.currentTimeMillis();
            long t = time2 - time;
            System.out.println("Elevation at (" + longitude + ", " + latitude + "): " + elevation + " meters time:"+t);

            longitude = 106.55;  // 示例经度
            latitude = 29.15;    // 示例纬度
            elevation = fetcher.getElevation(longitude, latitude);

            long time3 = System.currentTimeMillis();
            t = time3 - time2;
            System.out.println("Elevation at (" + longitude + ", " + latitude + "): " + elevation + " meters time:"+t);
        } catch (IOException e) {
            System.err.println("Error reading DEM file: " + e.getMessage());
        }
    }
}
