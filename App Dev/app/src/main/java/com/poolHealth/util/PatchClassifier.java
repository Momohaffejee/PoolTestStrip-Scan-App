package com.poolHealth.util;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import com.poolHealth.Models.CalculatedDataModelClass;
import com.poolHealth.Models.PoolReport;
import com.poolHealth.PoolTestActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.core.Scalar;

import java.util.ArrayList;

public class PatchClassifier{
    Context thisContext;
    int scn_no;
    public PatchClassifier(Context context,int scn_no){
        thisContext = context;
        this.scn_no = scn_no;
    }

    public void classifyData(ArrayList<Scalar> detectedPatchColor, Context context,int pt_no) {

        // model class for required data
        RequiredData requiredData = new RequiredData();

        // setting color and context
        requiredData.setColors(detectedPatchColor);
        requiredData.setContext(context);
        requiredData.setPt_no(pt_no);
        thisContext  = context;
        System.out.println("Detected Values " + detectedPatchColor);
        new CompareClass().execute(requiredData);
    }

}

class CompareClass extends AsyncTask<RequiredData, Integer, RequiredData>{

    GetDataSet getDataSet = new GetDataSet();

    // JSONArray to get respective array
    JSONArray thard_value_array = new JSONArray();
    JSONArray bro_value_array = new JSONArray();
    JSONArray fc_value_array = new JSONArray();
    JSONArray pro_value_array = new JSONArray();
    JSONArray ph_value_array = new JSONArray();
    JSONArray blo_value_array = new JSONArray();
    JSONArray glu_value_array = new JSONArray();
    JSONArray asc_value_array = new JSONArray();


    // declaring modal class
    CalculatedDataModelClass calculatedDataModelClass;

    // ArrayList of type modal class
    ArrayList<CalculatedDataModelClass> thard_result = new ArrayList<>();
    ArrayList<CalculatedDataModelClass> bro_result = new ArrayList<>();
    ArrayList<CalculatedDataModelClass> fc_result = new ArrayList<>();
    ArrayList<CalculatedDataModelClass> pro_result = new ArrayList<>();
    ArrayList<CalculatedDataModelClass> ph_result = new ArrayList<>();
    ArrayList<CalculatedDataModelClass> blo_result = new ArrayList<>();
    ArrayList<CalculatedDataModelClass> glu_result = new ArrayList<>();
    ArrayList<CalculatedDataModelClass> asc_result = new ArrayList<>();

    // hashmap type variable to store the final results after comparing
//    HashMap<String, String> compareResults = new HashMap<String, String>();


    @Override
    protected void onPreExecute() {
        //Setup precondition to execute some task

    }

    @Override
    protected RequiredData doInBackground(RequiredData... requiredData) {

        try {
            JSONObject obj = new JSONObject(getDataSet.loadJSONFromAsset(requiredData[0].getContext()));
            thard_value_array = obj.getJSONArray("th_value");
            bro_value_array = obj.getJSONArray("bro_value");
            fc_value_array = obj.getJSONArray("fc_value");
            pro_value_array = obj.getJSONArray("pro_value");
            ph_value_array = obj.getJSONArray("ph_value");

            System.out.println("Preset Data of TH " + thard_value_array);

            // euclidian distance for TH
            for (int i = 0; i< thard_value_array.length(); i++){
                JSONObject jsonObject = thard_value_array.getJSONObject(i);
                JSONArray jsonArray = jsonObject.getJSONArray("min");
                calculatedDataModelClass = new CalculatedDataModelClass(EuclidianDistance(requiredData[0].getColors().get(0), jsonArray), jsonObject.getString("value"), "TH_value");

                thard_result.add(calculatedDataModelClass);

            }

            // euclidian distance for nit
            for (int i = 0; i< bro_value_array.length(); i++){
                JSONObject jsonObject = bro_value_array.getJSONObject(i);
                JSONArray jsonArray = jsonObject.getJSONArray("min");
                calculatedDataModelClass = new CalculatedDataModelClass(EuclidianDistance(requiredData[0].getColors().get(1), jsonArray), jsonObject.getString("value"), "nit_value");

                bro_result.add(calculatedDataModelClass);

            }

            // euclidian distance for uro
            for (int i = 0; i< fc_value_array.length(); i++){
                JSONObject jsonObject = fc_value_array.getJSONObject(i);
                JSONArray jsonArray = jsonObject.getJSONArray("min");
                calculatedDataModelClass = new CalculatedDataModelClass(EuclidianDistance(requiredData[0].getColors().get(2), jsonArray), jsonObject.getString("value"), "fc_value");

                fc_result.add(calculatedDataModelClass);

            }

            // euclidian distance for pro
            for (int i=0; i<pro_value_array.length(); i++){
                JSONObject jsonObject = pro_value_array.getJSONObject(i);
                JSONArray jsonArray = jsonObject.getJSONArray("min");
                calculatedDataModelClass = new CalculatedDataModelClass(EuclidianDistance(requiredData[0].getColors().get(3), jsonArray), jsonObject.getString("value"), "pro_value");

                pro_result.add(calculatedDataModelClass);
            }

            // euclidian distance for ph
            for (int i=0; i<ph_value_array.length(); i++){
                JSONObject jsonObject = ph_value_array.getJSONObject(i);
                JSONArray jsonArray = jsonObject.getJSONArray("min");
                calculatedDataModelClass = new CalculatedDataModelClass(EuclidianDistance(requiredData[0].getColors().get(4), jsonArray), jsonObject.getString("value"), "ph_value");

                ph_result.add(calculatedDataModelClass);
            }

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        return requiredData[0];
    }

    @Override
    protected void onPostExecute(RequiredData requiredData) {
        super.onPostExecute(requiredData);
        //Setup precondition to execute some task

        // using bubble sort algorithm to fing the minimum distance
        // initializing smalles with the first indexed value of each array
        double smallest_TH = thard_result.get(0).getEuclidian_disance();
        double smallest_bro = bro_result.get(0).getEuclidian_disance();
        double smallest_fc = fc_result.get(0).getEuclidian_disance();
        double smallest_pro = pro_result.get(0).getEuclidian_disance();
        double smallest_ph = ph_result.get(0).getEuclidian_disance();

        String TH = thard_result.get(0).getTag_value();
        String bro = bro_result.get(0).getTag_value();
        String fc = fc_result.get(0).getTag_value();
        String pro = pro_result.get(0).getTag_value();
        String ph = ph_result.get(0).getTag_value();


        // calculating the minimum euclidian distance for leu
        for (int i=0; i<thard_result.size()-1; i++){
            System.out.println("result TH = "+ thard_result.get(i).getEuclidian_disance());
            if (smallest_TH > thard_result.get(i+1).getEuclidian_disance()){
                smallest_TH = thard_result.get(i+1).getEuclidian_disance();
                TH = thard_result.get(i+1).getTag_value();

            }
        }

        // calculating the minimum euclidian distance for nit
        for (int i = 0; i< bro_result.size()-1; i++){
            System.out.println("result bro = "+ bro_result.get(i).getEuclidian_disance());
            if (smallest_bro > bro_result.get(i+1).getEuclidian_disance()){
                smallest_bro = bro_result.get(i+1).getEuclidian_disance();
                bro = bro_result.get(i+1).getTag_value();
            }
        }

        // calculating the minimum euclidian distance for uro
        for (int i = 0; i< fc_result.size()-1; i++){
            System.out.println("result fc = "+ fc_result.get(i).getEuclidian_disance());
            if (smallest_fc > fc_result.get(i+1).getEuclidian_disance()){
                smallest_fc = fc_result.get(i+1).getEuclidian_disance();
                fc = fc_result.get(i+1).getTag_value();
            }
        }

        // calculating the minimum euclidian distance for pro
        for (int i=0; i<pro_result.size()-1; i++){
            System.out.println("result pro = "+ pro_result.get(i).getEuclidian_disance());
            if (smallest_pro > pro_result.get(i+1).getEuclidian_disance()){
                smallest_pro = pro_result.get(i+1).getEuclidian_disance();
                pro = pro_result.get(i+1).getTag_value();
            }
        }

        // calculating the minimum euclidian distance for ph
        for (int i=0; i<ph_result.size()-1; i++){
            System.out.println("result ph = "+ ph_result.get(i).getEuclidian_disance());
            if (smallest_ph > ph_result.get(i+1).getEuclidian_disance()){
                smallest_ph = ph_result.get(i+1).getEuclidian_disance();
                ph = ph_result.get(i+1).getTag_value();
            }
        }



//        System.out.println("Smallest leu = "+smallest_leu);
//        System.out.println("Smallest nit = "+smallest_nit);
//        System.out.println("Smallest uro = "+smallest_uro);
//        System.out.println("Smallest pro = "+smallest_pro);
//        System.out.println("Smallest ph = "+smallest_ph);
//        System.out.println("Smallest blo = "+smallest_blo);
//        System.out.println("Smallest sg = "+smallest_sg);
//        System.out.println("Smallest ket = "+smallest_ket);
//        System.out.println("Smallest bil = "+smallest_bil);
//        System.out.println("Smallest glu = "+smallest_glu);
//        System.out.println("Smallest asc = "+smallest_asc);

        // setting the final results in the HashMap compareResults
        PoolReport poolReport = new PoolReport();
        poolReport.setScn_no(requiredData.getPt_no());

        poolReport.setThardness(Float.parseFloat(TH));
        poolReport.setBro(Float.parseFloat(bro));
        poolReport.setFC(Float.parseFloat(fc));
        poolReport.setAlkanility(Float.parseFloat(pro));
        poolReport.setPh(Float.parseFloat(ph));



//        compareResults.put("Leu", leu);
//        compareResults.put("Nit", nit);
//        compareResults.put("Uro", uro);
//        compareResults.put("Pro", pro);
//        compareResults.put("Ph", ph);
//        compareResults.put("Blo", blo);
//        compareResults.put("Sg", sg);
//        compareResults.put("Ket", ket);
//        compareResults.put("Bil", bil);
//        compareResults.put("Glu", glu);
//        compareResults.put("Asc", asc);

        // printing the final result
//        System.out.println("Final compared result = "+compareResults);
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

    ArrayList<Scalar> colors = new ArrayList<>();
    Context context;

    public int getPt_no() {
        return pt_no;
    }

    public void setPt_no(int pt_no) {
        this.pt_no = pt_no;
    }

    int pt_no;

    public ArrayList<Scalar> getColors() {
        return colors;
    }

    public void setColors(ArrayList<Scalar> colors) {
        this.colors = colors;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

}