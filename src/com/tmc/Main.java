package com.tmc;

import org.geotools.swing.data.JFileDataStoreChooser;

import java.io.File;

public class Main {
    public static void main(String[] args) throws Exception {


//        //Open File Dialog do wyboru pliku shp
//        File file = JFileDataStoreChooser.showOpenFile("csv", null);
//        if (file == null) {
//            return;
//        }
//        List<PointFeature> pointList = SHPData.getPointsFromSHP(file.getAbsolutePath());

        String sourceFilename = "example.shp";
        String destFilename = "destexample.shp";


        SHPData shpData = new SHPData(sourceFilename);
        shpData.printList();
        shpData.processPointList();
        shpData.printList();
        shpData.savePointsToSHP(destFilename);
    }
}