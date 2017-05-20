package com.tmc;

import com.vividsolutions.jts.geom.Point;
import org.opengis.feature.simple.SimpleFeature;

/**
 * Created by User on 2017-05-18.
 */
public class PointFeature {

    public final double INVALID_VALUE = -999;


    private SimpleFeature simpleFeature;
    private WeatherParams weatherParams;
    private WeatherEnum weatherEnum;

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

    public WeatherParams getWeatherParams() {
        if (weatherParams == null) {
            weatherParams = new WeatherParams();
        }
        return weatherParams;
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
        String tempstr = String.valueOf(temperature);
        tempstr = tempstr.substring(0, tempstr.indexOf(".") + 3);
        simpleFeature.setAttribute("temp", tempstr);
    }

    public Point getPoint() {
        return (Point) simpleFeature.getAttribute("the_geom");
    }

    public void setPoint(Point point) {
        simpleFeature.setAttribute("the_geom", point);
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

    public void setWeather(WeatherEnum weather) {
        simpleFeature.setAttribute("weather", (long) weather.ordinal());
    }

    public void setWeather() {
        //TODO: obliczanie pogody na podstawie parametrów w WeatherParams...
        if (getWeather() < 4) {
            setWeather(WeatherEnum.values()[getWeather() + 1]);
        } else {
            setWeather(WeatherEnum.values()[0]);
        }
    }


    public void print() {
        System.out.println("Name: " + getName() + "   Lon: " + getLon() + "   Lat: " + getLat() + "   Temp: " + getTemperature() + "   Weather: " + getWeather());
    }
}
