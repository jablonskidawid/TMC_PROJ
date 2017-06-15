package com.tmc;

import com.vividsolutions.jts.geom.Point;
import org.opengis.feature.simple.SimpleFeature;

/**
 * Klasa reprezentująca punkt z shapefile'a
 */
public class PointFeature {
    private final int INVALID_VALUE = -999;

    //Nazwy pól w simpleFeature
    private static final String TEMP_KEY = "temp";
    private static final String NAME_KEY = "name";
    private static final String WEATHER_KEY = "weather";
    private static final String COORDINATE_KEY = "the_geom";
    //Obiekt kolekcji zawartej w shapefile - przechowuje wszystkie wartości danego punktu
    private SimpleFeature simpleFeature;
    //Wartości pogodowe
    private WeatherParams weatherParams;

    public SimpleFeature getSimpleFeature() {
        return simpleFeature;
    }

    public void setSimpleFeature(SimpleFeature simpleFeature) {
        this.simpleFeature = simpleFeature;
    }

    public String getName() {
        return (String) simpleFeature.getAttribute(NAME_KEY);
    }

    public void setName(String name) {
        simpleFeature.setAttribute(NAME_KEY, name);
    }

    public void setWeatherParams(WeatherParams weatherParams) {
        this.weatherParams = weatherParams;
    }

    public double getTemperature() {
        String tempstr = (String) simpleFeature.getAttribute(TEMP_KEY);
        if (!tempstr.isEmpty()) {
            return Double.parseDouble(tempstr);
        }
        return INVALID_VALUE;
    }

    public void setTemperature(double temperature) {
        double tempRound = (double) Math.round(temperature);
        String tempstr = "0";
        if (tempRound >= -40 && tempRound <= 40) {
            tempstr = String.valueOf(tempRound);
        }
        simpleFeature.setAttribute(TEMP_KEY, tempstr);
    }

    public Point getPoint() {
        return (Point) simpleFeature.getAttribute(COORDINATE_KEY);
    }

    public double getLon() {
        if (getPoint() != null) {
            return getPoint().getX();
        } else {
            return INVALID_VALUE;
        }
    }

    public double getLat() {
        if (getPoint() != null) {
            return getPoint().getY();
        } else {
            return INVALID_VALUE;
        }
    }

    public int getWeather() {
        String weatherstr = (String) simpleFeature.getAttribute(WEATHER_KEY);
        if (!weatherstr.isEmpty()) {
            return Integer.parseInt(weatherstr);
        }
        return INVALID_VALUE;
    }

    private void setWeather(WeatherEnum weather) {
        simpleFeature.setAttribute(WEATHER_KEY, String.valueOf(weather.ordinal()));
    }

    public void setWeather() {
        float lowCloudFrac = weatherParams.getLowCloudFrac();
        float medCloudFrac = weatherParams.getMedCloudFrac();
        float highCloudFrac = weatherParams.getHighCloudFrac();
        float acmTotalPercip = weatherParams.getAcmTotalPercip();

        if (lowCloudFrac < 10.0 && medCloudFrac < 10.0 && highCloudFrac < 10.0 && acmTotalPercip == 0.0) {
            setWeather(WeatherEnum.values()[0]);
        } else if (lowCloudFrac < 40.0 && medCloudFrac < 40.0 && highCloudFrac < 40.0 && acmTotalPercip == 0.0) {
            setWeather(WeatherEnum.values()[1]);
        } else if (acmTotalPercip == 0.0) {
            setWeather(WeatherEnum.values()[2]);
        } else if (acmTotalPercip < 0.5) {
            setWeather(WeatherEnum.values()[3]);
        } else {
            setWeather(WeatherEnum.values()[4]);
        }
    }

    public void print() {
        System.out.println(NAME_KEY + ": " + getName() + "   Lon: " + getLon() + "   Lat: " + getLat() + "   " + TEMP_KEY +
                ": " + getTemperature() + "   " + WEATHER_KEY + ": " + getWeather());
    }

    public void renamePoint() {
        String name = getName();
        name = name.replaceAll(String.valueOf((char) 165), "A"); //Ą
        name = name.replaceAll(String.valueOf((char) 185), "a"); //ą
        name = name.replaceAll(String.valueOf((char) 198), "c"); //Ć
        name = name.replaceAll(String.valueOf((char) 230), "c"); //ć
        name = name.replaceAll(String.valueOf((char) 202), "E"); //Ę
        name = name.replaceAll(String.valueOf((char) 234), "e"); //ę
        name = name.replaceAll(String.valueOf((char) 163), "L"); //Ł
        name = name.replaceAll(String.valueOf((char) 179), "l"); //ł
        name = name.replaceAll(String.valueOf((char) 209), "N"); //Ń
        name = name.replaceAll(String.valueOf((char) 241), "n"); //ń
        name = name.replaceAll(String.valueOf((char) 211), "O"); //Ó
        name = name.replaceAll(String.valueOf((char) 243), "o"); //ó
        name = name.replaceAll(String.valueOf((char) 140), "S"); //Ś
        name = name.replaceAll(String.valueOf((char) 156), "s"); //ś
        name = name.replaceAll(String.valueOf((char) 175), "Z"); //Ż
        name = name.replaceAll(String.valueOf((char) 191), "z"); //ż
        name = name.replaceAll(String.valueOf((char) 143), "Z"); //Ź
        name = name.replaceAll(String.valueOf((char) 159), "z"); //ź
        setName(name);
    }
}
