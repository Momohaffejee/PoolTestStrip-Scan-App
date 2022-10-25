package com.poolHealth;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.poolHealth.Models.PoolReport;
import com.poolHealth.util.LabDB;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class PoolTestActivity extends AppCompatActivity {
    Button btnSave;
    int scn_no;
    TextView txtTitle;
    EditText txtThardness,txtBromine, txtFC, txtAlkalinity,txtPH;

    PoolReport poolReport;
    String result_data;
    HashMap<String, String> compareResults = new HashMap<String, String>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pool_test);
        scn_no = getIntent().getIntExtra("PLNO",0);
//        result_data = getIntent().getStringExtra("RESULT");

        txtTitle = findViewById(R.id.txtTitle);
        txtThardness = findViewById(R.id.txtTotalHardness);
        txtBromine = findViewById(R.id.txtBromine);
        txtFC = findViewById(R.id.txtFreeChlorine);
        txtAlkalinity = findViewById(R.id.txtAlkalinity);
        txtPH = findViewById(R.id.txtpH);

        // Load Data
        final LabDB db = new LabDB(getApplicationContext());
        poolReport = db.getLastPoolReport(scn_no);
//        poolReport = loadfromdata();
        txtThardness.setText(String.valueOf(poolReport.getThardness()));
        txtBromine.setText(String.valueOf(poolReport.getBro()));
        txtFC.setText(String.valueOf(poolReport.getFC()));
        txtAlkalinity.setText(String.valueOf(poolReport.getAlkanility()));
        txtPH.setText(String.valueOf(poolReport.getPh()));


        btnSave = findViewById(R.id.btnSavePool);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Validate Data
                if(txtThardness.getText().toString().equals("")
                        || txtBromine.getText().toString().equals("")
                        || txtFC.getText().toString().equals("")
                        || txtAlkalinity.getText().toString().equals("")
                        || txtPH.getText().toString().equals("")
                        || scn_no==0
                        ){
                    Toast.makeText(getApplicationContext(),"Please Fill Up Data",Toast.LENGTH_LONG).show();
                    return;
                }
                poolReport.setThardness((float) Double.parseDouble(txtThardness.getText().toString()));
                poolReport.setBro((float) Double.parseDouble(txtBromine.getText().toString()));
                poolReport.setFC((float) Double.parseDouble(txtFC.getText().toString()));
                poolReport.setAlkanility((float) Double.parseDouble(txtAlkalinity.getText().toString()));
                poolReport.setPh((float) Double.parseDouble(txtPH.getText().toString()));
                int saved_row_id = db.SavePoolReport(poolReport);
                poolReport.setRow_id(saved_row_id);

                String result="OK";
                Intent returnIntent = getIntent();
                returnIntent.putExtra("result",result);
                setResult(RESULT_OK,returnIntent);
                finish();
            }
        });
    }

    private PoolReport loadfromdata() {
        PoolReport poolReport = new PoolReport();
        try {
            JSONObject result_object = new JSONObject(result_data);
            JSONObject result_data = new JSONObject();
            result_data = result_object.getJSONObject("data");

            poolReport.setThardness(Float.parseFloat(result_data.getString("TH")));
            poolReport.setBro(Float.parseFloat(result_data.getString("br")));
            poolReport.setFC(Float.parseFloat(result_data.getString("fc")));
            poolReport.setAlkanility(Float.parseFloat(result_data.getString("pro")));
            poolReport.setPh(Float.parseFloat(result_data.getString("ph")));

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return poolReport;

    }
}
