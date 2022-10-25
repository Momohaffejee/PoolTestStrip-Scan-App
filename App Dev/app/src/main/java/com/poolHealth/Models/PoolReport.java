package com.poolHealth.Models;


public class PoolReport {
    int scn_no;
    int row_id;
    float thardness;
    float bro;
    float fc;
    float alkalinity;
    float ph;

    public PoolReport(){
        scn_no = 0;
        row_id =0;
    }

    public int getScn_no() {
        return scn_no;
    }

    public void setScn_no(int scn_no) {
        this.scn_no = scn_no;
    }

    public int getRow_id() {
        return row_id;
    }

    public void setRow_id(int row_id) {
        this.row_id = row_id;
    }

    public float getThardness() {
        return thardness;
    }

    public void setThardness(float thardness) {
        this.thardness = thardness;
    }

    public float getBro() {
        return bro;
    }

    public void setBro(float bro) {
        this.bro = bro;
    }

    public float getFC() {
        return fc;
    }

    public void setFC(float fc) {
        this.fc = fc;
    }

    public float getAlkanility() {
        return alkalinity;
    }

    public void setAlkanility(float alkanility) {
        this.alkalinity = alkanility;
    }

    public float getPh() {
        return ph;
    }

    public void setPh(float ph) {
        this.ph = ph;
    }


}
