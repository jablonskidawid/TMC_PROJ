package com.tmc;

import org.apache.commons.io.FilenameUtils;

import java.util.ArrayList;
import java.util.List;

public class TMCImpl {
    public static void main(String[] args) throws Exception {

        List<String> pliki = new ArrayList<>();
        pliki.add("pon1.shp");
        pliki.add("pon2.shp");
        pliki.add("pon3.shp");
        String sourceFilename;

        for (String plik : pliki) {
            sourceFilename = plik;
            String destFilename = FilenameUtils.removeExtension(sourceFilename) + "_out.shp";
            SHPData shpData = new SHPData("plikiOrg\\" + sourceFilename);
            shpData.printList();
            shpData.processPointList();
            shpData.renamePoints();
            shpData.printList();
            shpData.savePointsToSHP("output\\" + destFilename);
        }
    }
}