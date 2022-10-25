package com.poolHealth.Models;


public class ScanModel {
    int plNo;
    String plName;
    // Constructor
    public ScanModel(){
        plNo = 0;
        plName ="";
    }

    public int getPlNo() {
        return plNo;
    }

    public void setPlNo(int plNo) {
        this.plNo = plNo;
    }

    public String getPlName() {
        return plName;
    }

    public void setPlName(String plName) {
        this.plName = plName;
    }

}
