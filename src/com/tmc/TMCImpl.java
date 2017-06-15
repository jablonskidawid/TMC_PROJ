package com.tmc;

import org.apache.commons.io.FilenameUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Główna klasa zawierająca funcję main.
 */
public class TMCImpl {
    private final static String DEFAULT_CSV_PATH = "/prognozy/CSV/poland/2017/6/16/6/";
    private final static String SOURCE_SHP_PREFIX = "pon";
    private final static String SOURCE_PATH = "plikiOrg\\";
    private final static String DEST_FILE_SUFFIX = "_out";
    private final static String DEST_PATH = "output\\";

    /**
     * @param args - można podać inną ścieżkę do zasobu z plikami csv. parametr musi być w postaci
     *             RRRR/M/D/H, np 2017/6/16/6
     */
    public static void main(String[] args) {
//      tutaj ustawiamy ścieżkę do plików CSV (wybieramy dany rok, miesiąc, dzień, godzinę
        String csvFilePath = DEFAULT_CSV_PATH;

        if (args.length > 0 && args[0] != null && args[0].length() > 0) {
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
        pliki.add(SOURCE_SHP_PREFIX + "1.shp");
        pliki.add(SOURCE_SHP_PREFIX + "2.shp");
        pliki.add(SOURCE_SHP_PREFIX + "3.shp");
        String sourceFilename;
        for (String plik : pliki) {
            sourceFilename = plik;
            String destFilename = FilenameUtils.removeExtension(sourceFilename) + DEST_FILE_SUFFIX + ".shp";
            try {
//            Utworzenie obiektu SHPData, pobranie danych z shapefile'a
                SHPData shpData = new SHPData(SOURCE_PATH + sourceFilename);
//            Modyfikacja danych
                shpData.processPointList();
//            Usunięcie polskich liter (na potrzeby oprogramowania, które korzysta z wyjściowych plików
                shpData.renamePoints();
//            Wyświetlenie listy punktów
                shpData.printListOfPoints();
//            Zapis do pliku
                Path outputPath = Paths.get(DEST_PATH);
                if (Files.notExists(outputPath)) {
                    Files.createDirectories(outputPath);
                }
                shpData.savePointsToSHP(DEST_PATH + destFilename);
            } catch (Exception e) {
                System.err.println("Wystąpił błąd podczas przetwarzania pliku " + sourceFilename + ". Plik " + destFilename + " nie został utworzony.");
            }
        }
    }
}