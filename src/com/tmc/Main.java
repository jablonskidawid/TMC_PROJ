package com.tmc;

import org.apache.commons.io.FilenameUtils;

public class Main {
    public static void main(String[] args) throws Exception {

        String sourceFilename = "pon3.shp";
        String destFilename = FilenameUtils.removeExtension(sourceFilename)+"_bezPol.shp";

        SHPData shpData = new SHPData(sourceFilename);
        shpData.printList();
        shpData.processPointList();
        shpData.renamePoints();
        shpData.printList();
        shpData.savePointsToSHP(destFilename);
    }
}