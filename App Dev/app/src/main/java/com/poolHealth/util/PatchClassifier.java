package com.poolHealth.util;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import com.poolHealth.Models.PoolReport;
import com.poolHealth.PoolTestActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.opencv.core.Scalar;

import java.util.ArrayList;

public class PatchClassifier{
    Context thisContext;
    int scn_no;
    public PatchClassifier(Context context,int scn_no){
        thisContext = context;
        this.scn_no = scn_no;
    }

    public void classifyData(ArrayList<Float> detectedPatchColor, Context context,int pl_no) {

        // model class for required data
        RequiredData requiredData = new RequiredData();

        // setting color and context
        requiredData.setColors(detectedPatchColor);
        requiredData.setContext(context);
        requiredData.setPl_no(pl_no);
        thisContext  = context;

        System.out.println("Detected Values " + detectedPatchColor);
        new ResultClass().execute(requiredData);
    }

}

class ResultClass extends AsyncTask<RequiredData, Integer, RequiredData>{

    //GetDataSet getDataSet = new GetDataSet();

    // JSONArray to get respective array
    /* JSONArray thard_value_array = new JSONArray();
    JSONArray bro_value_array = new JSONArray();
    JSONArray fc_value_array = new JSONArray();
    JSONArray pro_value_array = new JSONArray();
    JSONArray ph_value_array = new JSONArray();


    \*/
    // ArrayList of type modal class
    Float th_result;
    Float bro_result ;
    Float fc_result;
    Float ph_result;
    Float ta_result;



    @Override
    protected void onPreExecute() {
        //Setup precondition to execute some task

    }

    @Override
    protected RequiredData doInBackground(RequiredData... requiredData) {


        th_result = requiredData[0].getColors().get(0);
        System.out.println(th_result);
        bro_result = requiredData[0].getColors().get(1);
        System.out.println(bro_result);
        fc_result = requiredData[0].getColors().get(2);
        System.out.println(fc_result);
        ph_result = requiredData[0].getColors().get(3);
        System.out.println(ph_result);
        ta_result = requiredData[0].getColors().get(4);
        System.out.println(ta_result);


        return requiredData[0];
    }

    @Override
    protected void onPostExecute(RequiredData requiredData) {
        super.onPostExecute(requiredData);
        //Setup precondition to execute some task


        String TH = th_result.toString();
        String bro = bro_result.toString();
        String fc = fc_result.toString();
        String ph = ph_result.toString();
        String alk = ta_result.toString();


        // setting the final results in the HashMap compareResults
        PoolReport poolReport = new PoolReport();
        poolReport.setScn_no(requiredData.getPt_no());

        poolReport.setTh(Float.parseFloat(TH));
        poolReport.setBro(Float.parseFloat(bro));
        poolReport.setFC(Float.parseFloat(fc));
        poolReport.setPh(Float.parseFloat(ph));
        poolReport.setAlk(Float.parseFloat(alk));


        LabDB db = new LabDB(requiredData.getContext());
        db.SavePoolReport(poolReport);

        Intent resultintent = new Intent(requiredData.getContext(), PoolTestActivity.class);
        resultintent.putExtra("PLNO",requiredData.getPt_no());
        requiredData.getContext().startActivity(resultintent);



    }

    public double EuclidianDistance(Scalar current_data, JSONArray match_data){
        double redDifference=0.0, greenDifference=0.0, blueDifference=0.0;
        System.out.println("-----current_data " + current_data);
        System.out.println("-----Match Data " + match_data.toString());

        try {
            redDifference = current_data.val[2] - (int) match_data.get(2);
            greenDifference = current_data.val[1] - (int) match_data.get(1);
            blueDifference = current_data.val[0] - (int) match_data.get(0);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        double val = Math.sqrt(redDifference * redDifference + greenDifference * greenDifference + blueDifference * blueDifference);
        System.out.println("-----equili dist " + val);
        return val;
    }
}


class RequiredData{

    ArrayList<Float> colors = new ArrayList<>();
    Context context;

    public int getPt_no() {
        return pt_no;
    }

    public void setPl_no(int pt_no) {
        this.pt_no = pt_no;
    }

    int pt_no;

    public ArrayList<Float> getColors() {
        return colors;
    }

    public void setColors(ArrayList<Float> colors) {
        this.colors = colors;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

}