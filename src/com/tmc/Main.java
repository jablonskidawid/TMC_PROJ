package com.tmc;

public class Main {
    public static void main(String[] args) throws Exception {

        String sourceFilename = "pon1.shp";
        String destFilename = "destexample.shp";


        SHPData shpData = new SHPData(sourceFilename);
        shpData.printList();
        shpData.processPointList();
        shpData.printList();
        shpData.savePointsToSHP(destFilename);
    }
}