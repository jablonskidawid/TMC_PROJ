package com.tmc;

import org.apache.commons.io.FilenameUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class TMCImpl {
    public static void main(String[] args) {
//      tutaj ustawiamy ścieżkę do plików CSV (wybieramy dany rok, miesiąc, dzień, godzinę
        String csvFilePath = "/prognozy/CSV/poland/2017/6/1/18/";

        if (args.length > 0 && args[0] != null && args[0].length() >= 10) {
//          Podczas uruchomienia aplikacji możemy podać inną ścieżkę, tutaj ją nadpisujemy
            csvFilePath = "/prognozy/CSV/poland/" + args[0] + "/";
        }
//      Pobieranie pliki CSV
        if (!SHPData.downloadAllCsv(".", csvFilePath)) {
            System.err.println("Nie można pobrać danych z sieci.");
            System.exit(1);
        }

//      Zdefiniowanie listy plików shp, które będziemy modyfikować. Pliki muszą być w folderze plikiOrg.
//      Wymagane są następujące pliki shapefile, czyli .shp, .dbf, .prj, .shx, .qpj
        List<String> pliki = new ArrayList<>();
        pliki.add("pon1.shp");
        pliki.add("pon2.shp");
        pliki.add("pon3.shp");
        String sourceFilename;
        for (String plik : pliki) {
            sourceFilename = plik;
            String destFilename = FilenameUtils.removeExtension(sourceFilename) + "_out.shp";
            try {
//            Utworzenie obiektu SHPData, pobranie danych z shapefile'a
                SHPData shpData = new SHPData("plikiOrg\\" + sourceFilename);
//            Modyfikacja danych
                shpData.processPointList();
//            Usunięcie polskich liter (na potrzeby oprogramowania, które korzysta z wyjściowych plików
                shpData.renamePoints();
//            Wyświetlenie listy punktów
                shpData.printListOfPoints();
//            Zapis do pliku
                Path outputPath = Paths.get("output");
                if (Files.notExists(outputPath)) {
                    Files.createDirectories(outputPath);
                }
                shpData.savePointsToSHP("output\\" + destFilename);
            } catch (Exception e) {
                System.err.println("Wystąpił błąd podczas przetwarzania pliku " + sourceFilename + ". Plik " + destFilename + " nie został utworzony.");
            }
        }
    }
}