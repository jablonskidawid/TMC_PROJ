package com.tmc;

/**
 * Created by User on 2017-05-18.
 */
public class WeatherInfo {
    private float temperature;
    private float lowCloudFrac;
    private float medCloudFrac;
    private float highCloudFrac;
    private WeatherEnum weather;
    //TODO: dowiedzieć się skąd brać info o opadach i dołożyć obsługę danych z odpowiednich plików

    public float getTemperature() {
        return temperature;
    }

    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }

    public float getLowCloudFrac() {
        return lowCloudFrac;
    }

    public void setLowCloudFrac(float lowCloudFrac) {
        this.lowCloudFrac = lowCloudFrac;
    }

    public float getMedCloudFrac() {
        return medCloudFrac;
    }

    public void setMedCloudFrac(float medCloudFrac) {
        this.medCloudFrac = medCloudFrac;
    }

    public float getHighCloudFrac() {
        return highCloudFrac;
    }

    public void setHighCloudFrac(float highCloudFrac) {
        this.highCloudFrac = highCloudFrac;
    }

    public WeatherEnum getWeather() {
        return weather;
    }

    public void setWeather(WeatherEnum weather) {
        this.weather = weather;
    }
}
