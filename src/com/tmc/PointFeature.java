package com.tmc;

import com.vividsolutions.jts.geom.Point;
import org.opengis.feature.simple.SimpleFeature;

public class PointFeature {

    public final double INVALID_VALUE = -999;

    private SimpleFeature simpleFeature;
    private WeatherParams weatherParams;

    public SimpleFeature getSimpleFeature() {
        return simpleFeature;
    }

    public void setSimpleFeature(SimpleFeature simpleFeature) {
        this.simpleFeature = simpleFeature;
    }

    public String getName() {
        return (String) simpleFeature.getAttribute("name");
    }

    public void setName(String name) {
        simpleFeature.setAttribute("name", name);
    }

    public void setWeatherParams(WeatherParams weatherParams) {
        this.weatherParams = weatherParams;
    }

    public double getTemperature() {
        String tempstr = (String) simpleFeature.getAttribute("temp");
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
        simpleFeature.setAttribute("temp", tempstr);
    }

    public Point getPoint() {
        return (Point) simpleFeature.getAttribute("the_geom");
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
        String weatherstr = (String) simpleFeature.getAttribute("weather");
        if (!weatherstr.isEmpty()) {
            return Integer.parseInt(weatherstr);
        }
        return (int) INVALID_VALUE;
    }

    private void setWeather(WeatherEnum weather) {
        simpleFeature.setAttribute("weather", String.valueOf(weather.ordinal()));
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
        System.out.println("Name: " + getName() + "   Lon: " + getLon() + "   Lat: " + getLat() + "   Temp: " + getTemperature() + "   Weather: " + getWeather());
    }

    public void renamePoint() {
        String name = getName();
        name = name.replaceAll(String.valueOf((char) 185), "a"); //ą
        name = name.replaceAll(String.valueOf((char) 230), "c"); //ć
        name = name.replaceAll(String.valueOf((char) 234), "e"); //ę
        name = name.replaceAll(String.valueOf((char) 163), "L"); //duże ł
        name = name.replaceAll(String.valueOf((char) 179), "l"); //ł
        name = name.replaceAll(String.valueOf((char) 241), "n"); //ń
        name = name.replaceAll(String.valueOf((char) 243), "o"); //ó
        name = name.replaceAll(String.valueOf((char) 140), "S"); //duże ś
        name = name.replaceAll(String.valueOf((char) 156), "s"); //ś
        name = name.replaceAll(String.valueOf((char) 191), "z"); //ż
        name = name.replaceAll(String.valueOf((char) 175), "Z"); //Ż
        name = name.replaceAll(String.valueOf((char) 159), "z"); //ź
        setName(name);
    }
}
