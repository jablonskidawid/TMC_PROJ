package com.tmc;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;
import org.apache.commons.io.FilenameUtils;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.Transaction;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Klasa służąca do przetwarzania danych w plikach shapefile
 */
public class SHPData {
    //  schemat danych w pliku shp
    private SimpleFeatureType simpleFeatureType;
    //  lista punktów
    private List<PointFeature> pointFeatureList;

    //  parametry wykorzystywane do pobierania danych pogodowych
    private final static String SOURCE_CRS = "EPSG:3857";
    private final static String TARGET_CRS = "EPSG:3857";
    private final static String SERVER_ADDRES = "http://www.ksgmet.eti.pg.gda.pl";
    private static final String TMAX2m = "TMAX2m.csv";
    private static final String LOW_CLOUD_FRACTION = "LOW_CLOUD_FRACTION.csv";
    private static final String MID_CLOUD_FRACTION = "MID_CLOUD_FRACTION.csv";
    private static final String HIGH_CLOUD_FRACTION = "HIGH_CLOUD_FRACTION.csv";
    private static final String ACM_TOTAL_PERCIP = "ACM_TOTAL_PERCIP.csv";
    private static final String TEMP_NO_VALUE = "-9.99e+08";
    private static final int CSV_ARRAY_ROWS = 170;
    private static final int CSV_ARRAY_COLUMNS = 325;
    private static final double CSV_LON_OFFSET = 48.802824;
    private static final double CSV_LAT_OFFSET = 13.236774;
    private static final double CSV_SIZE_OF_PIXEL = 0.0378444945891919;


    /**
     * Funkcja pobierająca dane z pliku shapefile
     *
     * @param filename
     * @throws Exception
     */
    public SHPData(String filename) throws Exception {
        pointFeatureList = new ArrayList<>();
        Map<String, URL> map = new HashMap<>();

        File file = new File(filename);
        if (!file.exists()) {
            throw new Exception("Plik " + filename + " nie został znaleziony.");
        }
        map.put("url", file.toURI().toURL());

        DataStore dataStore = DataStoreFinder.getDataStore(map);
        SimpleFeatureSource featureSource = dataStore.getFeatureSource(dataStore.getTypeNames()[0]);
        SimpleFeatureIterator iterator = featureSource.getFeatures().features();
//        pobranie schematu danych
        simpleFeatureType = featureSource.getSchema();
        try {
            while (iterator.hasNext()) {
                SimpleFeature feature = iterator.next();
                PointFeature pointFeature = new PointFeature();
//                pobranie danych punktu i dodanie ich do własnej listy
                pointFeature.setSimpleFeature(feature);
                pointFeatureList.add(pointFeature);
            }
        } catch (Exception e) {
            System.err.println("Niepoprawna zawartość pliku " + filename);
            e.printStackTrace();
        } finally {
            iterator.close();
        }
    }

    /**
     * Funkcja aktualizująca wartości pogodowe dla wszystkich punktów w liście
     *
     * @throws Exception
     */
    public void processPointList() throws Exception {
        String[][] temperature = new String[CSV_ARRAY_ROWS][CSV_ARRAY_COLUMNS];
        String[][] lowCloud = new String[CSV_ARRAY_ROWS][CSV_ARRAY_COLUMNS];
        String[][] midCloud = new String[CSV_ARRAY_ROWS][CSV_ARRAY_COLUMNS];
        String[][] highCloud = new String[CSV_ARRAY_ROWS][CSV_ARRAY_COLUMNS];
        String[][] acmTotal = new String[CSV_ARRAY_ROWS][CSV_ARRAY_COLUMNS];
//        załadowanie danych z uprzednio pobranych plików CSV
        fillCloudAndAcmArrays(temperature, lowCloud, midCloud, highCloud, acmTotal);

        for (PointFeature point : pointFeatureList) {
            Point p = point.getPoint();

            // konwersja miedzy projekcjami
            CoordinateReferenceSystem sourceCRS = CRS.decode(SOURCE_CRS);
            CoordinateReferenceSystem targetCRS = CRS.decode("EPSG:4326");
            MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS, false);
            GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
            Point point2 = geometryFactory.createPoint(new Coordinate(p.getX(), p.getY()));
            Point targetPoint = (Point) JTS.transform(point2, transform);

            // x, y to wyliczone indeksy dla csv na podstawie wspolrzednych
            int x = (int) ((targetPoint.getX() - CSV_LON_OFFSET) / CSV_SIZE_OF_PIXEL);
            int y = (int) ((targetPoint.getY() - CSV_LAT_OFFSET) / CSV_SIZE_OF_PIXEL);

            // temperaturka, wolalem zabezpieczyc zeby sie nic nie wysypalo
            if (!temperature[x][y].equals(TEMP_NO_VALUE)) {
                point.setTemperature(Double.parseDouble(temperature[x][y]) - 273);
            } else {
                point.setTemperature(Double.parseDouble(TEMP_NO_VALUE));
            }

            // chmurki i opad
            WeatherParams wp = new WeatherParams();
            wp.setLowCloudFrac(Float.parseFloat(lowCloud[x][y]));
            wp.setMedCloudFrac(Float.parseFloat(midCloud[x][y]));
            wp.setHighCloudFrac(Float.parseFloat(highCloud[x][y]));
            wp.setAcmTotalPercip(Float.parseFloat(acmTotal[x][y]));
            point.setWeatherParams(wp);

            //kalkulacja ogolnej pogody
            point.setWeather();
        }
    }

    /**
     * Funkcja pobierające pliki CSV
     *
     * @param saveDir   - katalog zapisu plików
     * @param FILE_PATH - ścieżka plików na serwerze ksg
     */
    public static boolean downloadAllCsv(String saveDir, String FILE_PATH) {
        System.out.println("Pobieranie plików CSV");
        if (
                HttpCsvDownloader.downloadFile(SERVER_ADDRES + FILE_PATH + TMAX2m, saveDir) &&
                        HttpCsvDownloader.downloadFile(SERVER_ADDRES + FILE_PATH + LOW_CLOUD_FRACTION, saveDir) &&
                        HttpCsvDownloader.downloadFile(SERVER_ADDRES + FILE_PATH + MID_CLOUD_FRACTION, saveDir) &&
                        HttpCsvDownloader.downloadFile(SERVER_ADDRES + FILE_PATH + HIGH_CLOUD_FRACTION, saveDir) &&
                        HttpCsvDownloader.downloadFile(SERVER_ADDRES + FILE_PATH + ACM_TOTAL_PERCIP, saveDir)) {
            System.out.println("Pobieranie zakonczone");
            return true;
        } else {
            System.err.println("Wystąpił błąd podczas pobierania plików csv.");
            return false;
        }
    }

    /**
     * Funkcja wypełniające dwuwymiarowe tablice w oparciu o dane w plikach CSV. Tablice muszą mieć wymiary [170][325]
     *
     * @param temperature
     * @param lowCloud
     * @param midCloud
     * @param highCloud
     * @param acmTotal
     * @throws IOException
     */
    public void fillCloudAndAcmArrays(String[][] temperature, String[][] lowCloud, String[][] midCloud, String[][] highCloud, String[][] acmTotal) throws IOException {
        String cvsSplitBy = ",";
        BufferedReader br = new BufferedReader(new FileReader(TMAX2m));
        int i = 0;

        String line;
        while ((line = br.readLine()) != null) {
            String[] splitedLine = line.split(cvsSplitBy);
            temperature[i] = splitedLine;
            i++;
        }

        br = new BufferedReader(new FileReader(LOW_CLOUD_FRACTION));
        i = 0;

        while ((line = br.readLine()) != null) {
            String[] splitedLine = line.split(cvsSplitBy);
            lowCloud[i] = splitedLine;
            i++;
        }

        br = new BufferedReader(new FileReader(MID_CLOUD_FRACTION));
        i = 0;

        while ((line = br.readLine()) != null) {
            String[] splitedLine = line.split(cvsSplitBy);
            midCloud[i] = splitedLine;
            i++;
        }

        br = new BufferedReader(new FileReader(HIGH_CLOUD_FRACTION));
        i = 0;

        while ((line = br.readLine()) != null) {
            String[] splitedLine = line.split(cvsSplitBy);
            highCloud[i] = splitedLine;
            i++;
        }

        br = new BufferedReader(new FileReader(ACM_TOTAL_PERCIP));
        i = 0;

        while ((line = br.readLine()) != null) {
            String[] splitedLine = line.split(cvsSplitBy);
            acmTotal[i] = splitedLine;
            i++;
        }
    }

    /**
     * Funkcja zapisująca do shapefile'a
     *
     * @param filename - nazwa pliku, do którego zapisujemy shp
     */
    public void savePointsToSHP(String filename) {
        try {
            File file = new File(filename);
            if (file.exists()) {
                copyAndRemoveSHP(filename);
            }
//            utworzenie kolekcji w oparciu o listę punktów
            SimpleFeatureCollection collection = FeatureCollections.newCollection();
            SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(simpleFeatureType);
            for (PointFeature point : pointFeatureList) {
                collection.add(point.getSimpleFeature());
            }

            ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();
            Map<String, Serializable> params = new HashMap<>();
            params.put("url", file.toURL());
            params.put("create spatial index", Boolean.TRUE);
            ShapefileDataStore newDataStore;
            newDataStore = (ShapefileDataStore) dataStoreFactory.createNewDataStore(params);
            newDataStore.createSchema(simpleFeatureType);
            newDataStore.forceSchemaCRS(CRS.decode(TARGET_CRS, false));

            Transaction transaction = new DefaultTransaction("create");
            String typeName = newDataStore.getTypeNames()[0];
            SimpleFeatureSource featureSource = newDataStore.getFeatureSource(newDataStore.getTypeNames()[0]);
            if (featureSource != null) {
                SimpleFeatureStore featureStore = (SimpleFeatureStore) featureSource;
                featureStore.setTransaction(transaction);
                try {
                    featureStore.addFeatures(collection);
                    transaction.commit();
                } catch (Exception problem) {
                    problem.printStackTrace();
                    transaction.rollback();
                } finally {
                    transaction.close();
                }
            } else {
                System.out.println("Wystąpił błąd podczas zapisu danych");
                System.exit(1);
            }
        } catch (Exception e) {
            System.err.println("Wystąpił błąd podczas zapisu shapefile " + filename);
            e.printStackTrace();
        }
    }

    /*
     * Funkcja sprawdza, czy istnieją pliki o takiej nazwie, jaką wybraliśmy.
     * Jeśli są to przenosi ich do nazwapliku + _old, żeby w razie czego nie
     * usunąć danych w przypadku błędu programu
     */
    private static void copyAndRemoveSHP(String filename) throws IOException {
        List<String> extensionList = new ArrayList<String>();
        extensionList.add(".dbf");
        extensionList.add(".prj");
        extensionList.add(".shp");
        extensionList.add(".shx");
        extensionList.add(".qpj");
        extensionList.add(".qix");
        for (String extension : extensionList) {
            String nameWithoutExtension = FilenameUtils.removeExtension(filename);
            Path filepath = Paths.get(nameWithoutExtension + extension);
            File file = new File(nameWithoutExtension + extension);
            if (file.exists()) {
                Files.copy(filepath, Paths.get(filepath + "_old"), StandardCopyOption.REPLACE_EXISTING);
                Files.delete(filepath);
            }
        }
    }

    public void printListOfPoints() {
        System.out.println("List of points:");
        for (PointFeature pf : pointFeatureList) {
            pf.print();
        }
    }

    public void renamePoints() {
        for (PointFeature pf : pointFeatureList) {
            pf.renamePoint();
        }
    }
}
