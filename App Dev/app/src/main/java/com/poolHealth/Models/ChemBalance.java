package com.poolHealth.Models;


public class ChemBalance {
    int scn_no;
    int row_id;

    public ChemBalance(){
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
}
