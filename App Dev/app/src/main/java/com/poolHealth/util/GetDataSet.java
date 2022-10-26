package com.poolHealth.util;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;

public class GetDataSet {

    GetDataSet(){

    }

    // Loading the data set from asset
    public String loadJSONFromAsset(Context context) {
        String json = null;
        try {
            InputStream is = context.getAssets().open("patch_color.json");

            int size = is.available();

            byte[] buffer = new byte[size];

            is.read(buffer);

            is.close();

            json = new String(buffer, "UTF-8");


        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;

    }
    public String loadJSONFromDB(Context context){
        String json = null;
        json = new LabDB(context).getPoolData();
        return json;
    }
    public void saveJSONToDB(Context context,String json){
        new LabDB(context).setPoolData(json);
    }


}
