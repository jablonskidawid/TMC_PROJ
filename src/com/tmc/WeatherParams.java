package com.tmc;

/**
 * Created by User on 2017-05-18.
 */
public class WeatherParams {
    private float lowCloudFrac;
    private float medCloudFrac;
    private float highCloudFrac;
    private float acmTotalPercip;

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

    public float getAcmTotalPercip() {
        return acmTotalPercip;
    }

    public void setAcmTotalPercip(float acmTotalPercip) {
        this.acmTotalPercip = acmTotalPercip;
    }
}
