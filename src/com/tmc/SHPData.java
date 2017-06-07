package com.tmc;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;
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
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

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

public class SHPData {

    private SimpleFeatureType simpleFeatureType;
    private List<PointFeature> pointFeatureList;
    private final String TMAX2m = "TMAX2m.csv";
    private final String LOW_CLOUD_FRACTION = "LOW_CLOUD_FRACTION.csv";
    private final String MID_CLOUD_FRACTION = "MID_CLOUD_FRACTION.csv";
    private final String HIGH_CLOUD_FRACTION = "HIGH_CLOUD_FRACTION.csv";
    private final String ACM_TOTAL_PERCIP = "ACM_TOTAL_PERCIP.csv";
    private final String SERVER_ADDRES = "http://www.ksgmet.eti.pg.gda.pl";
    private final String FILE_PATH = "/prognozy/CSV/poland/2017/6/1/18/";

    public SHPData(String filename) throws Exception {
        pointFeatureList = new ArrayList<>();

        File file = new File(filename);
        if (!file.exists())
            throw new Exception("File not found");
        Map<String, URL> map = new HashMap<>();
        map.put("url", file.toURI().toURL());

        DataStore dataStore = DataStoreFinder.getDataStore(map);
        SimpleFeatureSource featureSource = dataStore.getFeatureSource(dataStore.getTypeNames()[0]);
        SimpleFeatureIterator iterator = featureSource.getFeatures().features();
        simpleFeatureType = featureSource.getSchema();
        try {
            while (iterator.hasNext()) {
                SimpleFeature feature = iterator.next();
                PointFeature pointFeature = new PointFeature();
                pointFeature.setSimpleFeature(feature);
                pointFeatureList.add(pointFeature);
            }
        } catch (Exception e) {
            System.err.println("Invalid fields or values in shapefile " + filename);
            e.printStackTrace();
        } finally {
            iterator.close();
        }
    }

    public void processPointList()
            throws IOException, FactoryException, Exception, TransformException {

        //tu trzeba sciezke bezwgledna niestety podac, przynajmniej mi sie nie udalo ze wzgledna
//    	String saveDir = "E:/Projekty/TMC/projekt/tmc";
        String saveDir = ".";

        downloadAllCsv(saveDir);

        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";

        String[][] temperature = new String[170][325];
        String[][] lowCloud = new String[170][325];
        String[][] midCloud = new String[170][325];
        String[][] highCloud = new String[170][325];
        String[][] acmTotal = new String[170][325];

        fillCloudAndAcmArrays(br, TMAX2m, LOW_CLOUD_FRACTION, MID_CLOUD_FRACTION, HIGH_CLOUD_FRACTION,
                ACM_TOTAL_PERCIP, line, cvsSplitBy, temperature, lowCloud, midCloud,
                highCloud, acmTotal);

        for (PointFeature point : pointFeatureList) {
            Point p = point.getPoint();

            // konwersja miedzy projekcjami
            CoordinateReferenceSystem sourceCRS = CRS.decode("EPSG:3857");
            CoordinateReferenceSystem targetCRS = CRS.decode("EPSG:4326");
            MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS, false);
            GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
            Point point2 = geometryFactory.createPoint(new Coordinate(p.getX(), p.getY()));
            Point targetPoint = (Point) JTS.transform(point2, transform);

            // x, y to wyliczone indeksy dla csv na podstawie wspolrzednych
            int x = (int) ((targetPoint.getX() - 48.802824) / 0.0378444945891919);
            int y = (int) ((targetPoint.getY() - 13.236774) / 0.0378444945891919);

            // temperaturka, wolalem zabezpieczyc zeby sie nic nie wysypalo
            if (!temperature[x][y].equals("-9.99e+08")) {
                point.setTemperature(Double.parseDouble(temperature[x][y]) - 273);
            } else {
                point.setTemperature(Double.parseDouble("-9.99e+08"));
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

    public void downloadAllCsv(String saveDir) {
        try {
            HttpCsvDownloader.downloadFile(SERVER_ADDRES + FILE_PATH + TMAX2m, saveDir);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        try {
            HttpCsvDownloader.downloadFile(SERVER_ADDRES + FILE_PATH + LOW_CLOUD_FRACTION, saveDir);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        try {
            HttpCsvDownloader.downloadFile(SERVER_ADDRES + FILE_PATH + MID_CLOUD_FRACTION, saveDir);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        try {
            HttpCsvDownloader.downloadFile(SERVER_ADDRES + FILE_PATH + HIGH_CLOUD_FRACTION, saveDir);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        try {
            HttpCsvDownloader.downloadFile(SERVER_ADDRES + FILE_PATH + ACM_TOTAL_PERCIP, saveDir);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void fillCloudAndAcmArrays(BufferedReader br, String tempMax2m, String lowCloudFrac, String midCloudFrac,
                                      String highCloudFrac, String acmTotalPercip, String line, String cvsSplitBy, String[][] temperature,
                                      String[][] lowCloud, String[][] midCloud, String[][] highCloud, String[][] acmTotal) throws IOException {

        br = new BufferedReader(new FileReader(tempMax2m));
        int i = 0;

        while ((line = br.readLine()) != null) {
            String[] splitedLine = line.split(cvsSplitBy);
            temperature[i] = splitedLine;
            i++;
        }

        br = new BufferedReader(new FileReader(lowCloudFrac));
        i = 0;

        while ((line = br.readLine()) != null) {
            String[] splitedLine = line.split(cvsSplitBy);
            lowCloud[i] = splitedLine;
            i++;
        }

        br = new BufferedReader(new FileReader(midCloudFrac));
        i = 0;

        while ((line = br.readLine()) != null) {
            String[] splitedLine = line.split(cvsSplitBy);
            midCloud[i] = splitedLine;
            i++;
        }

        br = new BufferedReader(new FileReader(highCloudFrac));
        i = 0;

        while ((line = br.readLine()) != null) {
            String[] splitedLine = line.split(cvsSplitBy);
            highCloud[i] = splitedLine;
            i++;
        }

        br = new BufferedReader(new FileReader(acmTotalPercip));
        i = 0;

        while ((line = br.readLine()) != null) {
            String[] splitedLine = line.split(cvsSplitBy);
            acmTotal[i] = splitedLine;
            i++;
        }
    }

    public void savePointsToSHP(String filename) {
        try {
            File file = new File(filename);
            if (file.exists()) {
                copyAndRemoveSHP(filename);
            }

            SimpleFeatureCollection collection = FeatureCollections.newCollection();
            SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(simpleFeatureType);

            for (PointFeature point : pointFeatureList) {
                collection.add(point.getSimpleFeature());
            }

            ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();
            Map<String, Serializable> params = new HashMap<>();
            params.put("url", file.toURL());
            params.put("create spatial index", Boolean.TRUE);
            ShapefileDataStore newDataStore = null;
            newDataStore = (ShapefileDataStore) dataStoreFactory.createNewDataStore(params);
            newDataStore.createSchema(simpleFeatureType);
            newDataStore.forceSchemaCRS(CRS.decode("EPSG:3857", false));

            Transaction transaction = new DefaultTransaction("create");
            String typeName = newDataStore.getTypeNames()[0];
            SimpleFeatureSource featureSource = newDataStore.getFeatureSource(newDataStore.getTypeNames()[0]);
            if (featureSource instanceof SimpleFeatureStore) {
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
                System.exit(0); // success!
            } else {
                System.out.println(typeName + " does not support read/write access");
                System.exit(1);
            }
        } catch (Exception e) {
            System.err.println("Error during saving shapefile " + filename);
            e.printStackTrace();
        }
    }

    /*
     * Funkcja sprawdza, czy istnieją pliki o takiej nazwie, jaką wybraliśmy.
     * Jeśli są to przenosi ich do nazwapliku + _old, żeby w razie czego nie
     * usunąć danych w przypadku błędu programu
     */
    public static void copyAndRemoveSHP(String filename) throws IOException {
        List<String> extensionList = new ArrayList<String>();
        extensionList.add(".dbf");
        extensionList.add(".prj");
        extensionList.add(".shp");
        extensionList.add(".shx");
        extensionList.add(".qpj");
        extensionList.add(".qix");
        for (String extension : extensionList) {
            String nameWithoutExtension = filename.substring(0, filename.length() - 4);
            Path filepath = Paths.get(nameWithoutExtension + extension);
            File file = new File(nameWithoutExtension + extension);
            if (file.exists()) {
                Files.copy(filepath, Paths.get(filepath + "_old"), StandardCopyOption.REPLACE_EXISTING);
                Files.delete(filepath);
            }
        }
    }

    public void printList() {
        System.out.println("List of points:");
        for (PointFeature pf : pointFeatureList) {
            pf.print();
        }
    }
    public void renamePoints(){
        for (PointFeature pf : pointFeatureList) {
            pf.renamePoint();
        }
    }
}
