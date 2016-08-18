package com.example.deanc.gps_alarm;

/**
 * Created by DeanC on 8/17/2016.
 */
public class LIRR_Station {

    String NAME;
    Double LAT;
    Double LON;

    public LIRR_Station(String NAME, Double LAT, Double LON) {
        this.NAME = NAME;
        this.LAT = LAT;
        this.LON = LON;
    }

    public String getNAME() {
        return NAME;
    }

    public void setNAME(String NAME) {
        this.NAME = NAME;
    }

    public Double getLAT() {
        return LAT;
    }

    public void setLAT(Double LAT) {
        this.LAT = LAT;
    }

    public Double getLON() {
        return LON;
    }

    public void setLON(Double LON) {
        this.LON = LON;
    }
}
