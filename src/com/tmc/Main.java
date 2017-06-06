package com.tmc;

import org.apache.commons.io.FilenameUtils;

public class Main {
    public static void main(String[] args) throws Exception {

        String sourceFilename = "pon1.shp";
        String destFilename = FilenameUtils.removeExtension(sourceFilename)+"_out.shp";

        SHPData shpData = new SHPData(sourceFilename);
        shpData.printList();
        shpData.processPointList();
        shpData.printList();
        shpData.savePointsToSHP(destFilename);
    }
}