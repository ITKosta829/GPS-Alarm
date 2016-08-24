package com.example.deanc.gps_alarm;

/**
 * Created by DeanC on 8/17/2016.
 */
public class TrainStation {

    String NAME;
    String CODE;
    Double LAT;
    Double LON;

    public TrainStation(String NAME, Double LAT, Double LON) {
        this.NAME = NAME;
        this.LAT = LAT;
        this.LON = LON;
    }

    public TrainStation(String NAME, String CODE, Double LAT, Double LON) {
        this(NAME, LAT, LON);
        this.CODE = CODE;
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

    public String getCODE() {
        return CODE;
    }

    public void setCODE(String CODE) {
        this.CODE = CODE;
    }
}
