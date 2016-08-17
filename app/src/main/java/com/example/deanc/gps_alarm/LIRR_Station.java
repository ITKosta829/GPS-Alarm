package com.example.deanc.gps_alarm;

/**
 * Created by DeanC on 8/17/2016.
 */
public class LIRR_Station {

    String NAME;
    String LAT;
    String LON;

    public LIRR_Station(String NAME, String LAT, String LON) {
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

    public String getLAT() {
        return LAT;
    }

    public void setLAT(String LAT) {
        this.LAT = LAT;
    }

    public String getLON() {
        return LON;
    }

    public void setLON(String LON) {
        this.LON = LON;
    }
}
