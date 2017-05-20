package com.tmc;

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
import org.geotools.referencing.CRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
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

    public SHPData(String filename) throws Exception {
        pointFeatureList = new ArrayList<>();

        File file = new File(filename);
        if (!file.exists()) throw new Exception("File not found");
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

    public void processPointList() {
        //TODO: przetwarzanie punktów na postawie danych z plików csv

        //Wyznaczenie pogody dla parametrów pobranych w plików CSV
        for (PointFeature point : pointFeatureList) {
            point.setWeather();
            point.setTemperature(point.getTemperature() + 2.42);
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
    Funkcja sprawdza, czy istnieją pliki o takiej nazwie, jaką wybraliśmy.
    Jeśli są to przenosi ich do nazwapliku + _old,
    żeby w razie czego nie usunąć danych w przypadku błędu programu
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
}
