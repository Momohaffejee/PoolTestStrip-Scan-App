package com.poolHealth;

import android.annotation.SuppressLint;
import android.content.ComponentName;
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
    Button btnSave, btnth0,btnth100, btnth200, btnth400, btnth800, btnbr0,btnbr1,btnbr2, btnbr4, btnbr10, btnbr20, btnfc0, btnfc1, btnfc2, btnfc3, btnfc5, btnfc10;
    Button btnph62, btnph68, btnph72, btnph78, btnph84, btnta0, btnta40, btnta80, btnta120,btnta180, btnta240;
    int scn_no;
    TextView txtTitle;
    EditText txtHardness,txtBromine, txtFC, txtAlkalinity,txtPH;

    PoolReport poolReport;
    String result_data;
    HashMap<String, String> compareResults = new HashMap<String, String>();
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pool_test);
        scn_no = getIntent().getIntExtra("PLNO",0);

        txtTitle = findViewById(R.id.txtTitle);
        txtHardness = findViewById(R.id.txtTotalHardness);
        txtBromine = findViewById(R.id.txtBromine);
        txtFC = findViewById(R.id.txtFreeChlorine);
        txtPH = findViewById(R.id.txtpH);
        txtAlkalinity = findViewById(R.id.txtAlkalinity);



        LabDB db = new LabDB(getApplicationContext());
        poolReport = db.getLastPoolReport(scn_no);

        txtHardness.setText(String.valueOf(poolReport.getTh()));
        txtBromine.setText(String.valueOf(poolReport.getBro()));
        txtFC.setText(String.valueOf(poolReport.getFC()));
        txtPH.setText(String.valueOf(poolReport.getPh()));
        txtAlkalinity.setText(String.valueOf(poolReport.getAlk()));
        btnth0 = findViewById(R.id.btnth0);
        btnth0.setOnClickListener(this::onth0Clicked);
        btnth100 = findViewById(R.id.btnth100);
        btnth100.setOnClickListener(this::onth100Clicked);
        btnth200 = findViewById(R.id.btnth200);
        btnth200.setOnClickListener(this::onth200Clicked);
        btnth400 = findViewById(R.id.btnth400);
        btnth400.setOnClickListener(this::onth400Clicked);
        btnth800 = findViewById(R.id.btnth800);
        btnth800.setOnClickListener(this::onth800Clicked);
        btnbr0 = findViewById(R.id.btnbr0);
        btnbr0.setOnClickListener(this::onbr0Clicked);
        btnbr1 = findViewById(R.id.btnbr1);
        btnbr1.setOnClickListener(this::onbr1Clicked);
        btnbr2 = findViewById(R.id.btnbr2);
        btnbr2.setOnClickListener(this::onbr2Clicked);
        btnbr4 = findViewById(R.id.btnbr4);
        btnbr4.setOnClickListener(this::onbr4Clicked);
        btnbr10 = findViewById(R.id.btnbr10);
        btnbr10.setOnClickListener(this::onbr10Clicked);
        btnbr20 = findViewById(R.id.btnbr20);
        btnbr20.setOnClickListener(this::onbr20Clicked);
        btnfc0 = findViewById(R.id.btnfh0);
        btnfc0.setOnClickListener(this::onfc0Clicked);
        btnfc1 = findViewById(R.id.btnfh1);
        btnfc1.setOnClickListener(this::onfc1Clicked);
        btnfc2 = findViewById(R.id.btnfh2);
        btnfc2.setOnClickListener(this::onfc2Clicked);
        btnfc3 = findViewById(R.id.btnfh3);
        btnfc3.setOnClickListener(this::onfc3Clicked);
        btnfc5 = findViewById(R.id.btnfh5);
        btnfc5.setOnClickListener(this::onfc5Clicked);
        btnfc10 = findViewById(R.id.btnfh10);
        btnfc10.setOnClickListener(this::onfc10Clicked);
        btnph62 = findViewById(R.id.btnph62);
        btnph62.setOnClickListener(this::onph62Clicked);
        btnph68 = findViewById(R.id.btnph68);
        btnph68.setOnClickListener(this::onph68Clicked);
        btnph72 = findViewById(R.id.btnph72);
        btnph72.setOnClickListener(this::onph72Clicked);
        btnph78 = findViewById(R.id.btnph78);
        btnph78.setOnClickListener(this::onph78Clicked);
        btnph84 = findViewById(R.id.btnph84);
        btnph84.setOnClickListener(this::onph84Clicked);
        btnta0 = findViewById(R.id.btnta0);
        btnta0.setOnClickListener(this::onta0Clicked);
        btnta40 = findViewById(R.id.btnta40);
        btnta40.setOnClickListener(this::onta40Clicked);
        btnta80 = findViewById(R.id.btnta80);
        btnta80.setOnClickListener(this::onta80Clicked);
        btnta120 = findViewById(R.id.btnta120);
        btnta120.setOnClickListener(this::onta120Clicked);
        btnta180 = findViewById(R.id.btnta180);
        btnta180.setOnClickListener(this::onta180Clicked);
        btnta240 = findViewById(R.id.btnta240);
        btnta240.setOnClickListener(this::onta240Clicked);



        btnSave = findViewById(R.id.btnSavePool);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Validate Data
                if(txtHardness.getText().toString().equals("")
                        || txtBromine.getText().toString().equals("")
                        || txtFC.getText().toString().equals("")
                        || txtAlkalinity.getText().toString().equals("")
                        || txtPH.getText().toString().equals("")
                        || scn_no==0
                        ){
                    Toast.makeText(getApplicationContext(),"Please Fill Up Data",Toast.LENGTH_LONG).show();
                    return;
                }
                System.out.println(poolReport.getTh());
                System.out.println(poolReport.getBro());
                System.out.println(poolReport.getFC());
                System.out.println(poolReport.getPh());
                System.out.println(poolReport.getAlk());
                System.out.println("Here");
                poolReport.setTh((float) Double.parseDouble(txtHardness.getText().toString()));
                poolReport.setBro((float) Double.parseDouble(txtBromine.getText().toString()));
                poolReport.setFC((float) Double.parseDouble(txtFC.getText().toString()));
                poolReport.setAlk((float) Double.parseDouble(txtAlkalinity.getText().toString()));
                poolReport.setPh((float) Double.parseDouble(txtPH.getText().toString()));

                System.out.println(poolReport.getTh());
                System.out.println(poolReport.getBro());
                System.out.println(poolReport.getFC());
                System.out.println(poolReport.getPh());
                System.out.println(poolReport.getAlk());
                //int saved_row_id = db.SavePoolReport(poolReport);
                //poolReport.setRow_id(saved_row_id);
                LabDB db = new LabDB(getApplicationContext());
                db.SavePoolReport(poolReport);
                String result="OK";
                final Intent chemreportIntent = new Intent();

                chemreportIntent.setComponent(new ComponentName(getApplicationContext(), ChemReport.class));
                chemreportIntent.putExtra("PLNO",scn_no);
                startActivity(chemreportIntent);

                /*Intent returnIntent = getIntent();
                returnIntent.putExtra("result",result);
                setResult(RESULT_OK,returnIntent);*/
                //finish();
            }
        });
    }

    private void onta0Clicked(View view) {
        txtAlkalinity.setText("0.0");
        poolReport.setAlk((float) Double.parseDouble(txtAlkalinity.getText().toString()));
    }
    private void onta40Clicked(View view) {
        txtAlkalinity.setText("40.0");
        poolReport.setAlk((float) Double.parseDouble(txtAlkalinity.getText().toString()));
    }
    private void onta80Clicked(View view) {
        txtAlkalinity.setText("80.0");
        poolReport.setAlk((float) Double.parseDouble(txtAlkalinity.getText().toString()));
    }
    private void onta120Clicked(View view) {
        txtAlkalinity.setText("120.0");
        poolReport.setAlk((float) Double.parseDouble(txtAlkalinity.getText().toString()));
    }
    private void onta180Clicked(View view) {
        txtAlkalinity.setText("180.0");
        poolReport.setAlk((float) Double.parseDouble(txtAlkalinity.getText().toString()));
    }
    private void onta240Clicked(View view) {
        txtAlkalinity.setText("240.0");
        poolReport.setAlk((float) Double.parseDouble(txtAlkalinity.getText().toString()));
    }

    private void onph62Clicked(View view) {
        txtPH.setText("6.2");
        poolReport.setPh((float) Double.parseDouble(txtPH.getText().toString()));
    }
    private void onph68Clicked(View view) {
        txtPH.setText("6.8");
        poolReport.setPh((float) Double.parseDouble(txtPH.getText().toString()));
    }
    private void onph72Clicked(View view) {
        txtPH.setText("7.2");
        poolReport.setPh((float) Double.parseDouble(txtPH.getText().toString()));
    }
    private void onph78Clicked(View view) {
        txtPH.setText("7.8");
        poolReport.setPh((float) Double.parseDouble(txtPH.getText().toString()));
    }
    private void onph84Clicked(View view) {
        txtPH.setText("8.4");
        poolReport.setPh((float) Double.parseDouble(txtPH.getText().toString()));
    }

    private void onfc0Clicked(View view) {
        txtFC.setText("0.0");
        poolReport.setFC((float) Double.parseDouble(txtFC.getText().toString()));
    }
    private void onfc1Clicked(View view) {
        txtFC.setText("1.0");
        poolReport.setFC((float) Double.parseDouble(txtFC.getText().toString()));
    }
    private void onfc2Clicked(View view) {
        txtFC.setText("2.0");
        poolReport.setFC((float) Double.parseDouble(txtFC.getText().toString()));
    }
    private void onfc3Clicked(View view) {
        txtFC.setText("3.0");
        poolReport.setFC((float) Double.parseDouble(txtFC.getText().toString()));
    }
    private void onfc5Clicked(View view) {
        txtFC.setText("5.0");
        poolReport.setFC((float) Double.parseDouble(txtFC.getText().toString()));
    }
    private void onfc10Clicked(View view) {
        txtFC.setText("10.0");
        poolReport.setFC((float) Double.parseDouble(txtFC.getText().toString()));
    }


    private void onbr0Clicked(View view) {
        txtBromine.setText("0.0");
        poolReport.setBro((float) Double.parseDouble(txtBromine.getText().toString()));
    }
    private void onbr1Clicked(View view) {
        txtBromine.setText("1.0");
        poolReport.setBro((float) Double.parseDouble(txtBromine.getText().toString()));
    }
    private void onbr2Clicked(View view) {
        txtBromine.setText("2.0");
        poolReport.setBro((float) Double.parseDouble(txtBromine.getText().toString()));
    }
    private void onbr4Clicked(View view) {
        txtBromine.setText("4.0");
        poolReport.setBro((float) Double.parseDouble(txtBromine.getText().toString()));
    }
    private void onbr10Clicked(View view) {
        txtBromine.setText("10.0");
        poolReport.setBro((float) Double.parseDouble(txtBromine.getText().toString()));
    }
    private void onbr20Clicked(View view) {
        txtBromine.setText("20.0");
        poolReport.setBro((float) Double.parseDouble(txtBromine.getText().toString()));
    }

    private void onth0Clicked(View view) {
        txtHardness.setText("0");
        poolReport.setTh((float) Double.parseDouble(txtHardness.getText().toString()));
    }
    private void onth100Clicked(View view) {
        txtHardness.setText("100");
        poolReport.setTh((float) Double.parseDouble(txtHardness.getText().toString()));
    }
    private void onth200Clicked(View view) {
        txtHardness.setText("200");
        poolReport.setTh((float) Double.parseDouble(txtHardness.getText().toString()));
    }
    private void onth400Clicked(View view) {
        txtHardness.setText("400");
        poolReport.setTh((float) Double.parseDouble(txtHardness.getText().toString()));
    }
    private void onth800Clicked(View view) {
        txtHardness.setText("800");
        poolReport.setTh((float) Double.parseDouble(txtHardness.getText().toString()));
    }


    private PoolReport loadfromdata() {
        PoolReport poolReport = new PoolReport();
        try {
            JSONObject result_object = new JSONObject(result_data);
            JSONObject result_data = new JSONObject();
            result_data = result_object.getJSONObject("data");

            poolReport.setTh(Float.parseFloat(result_data.getString("TH")));
            poolReport.setBro(Float.parseFloat(result_data.getString("br")));
            poolReport.setFC(Float.parseFloat(result_data.getString("fc")));
            poolReport.setAlk(Float.parseFloat(result_data.getString("pro")));
            poolReport.setPh(Float.parseFloat(result_data.getString("ph")));

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return poolReport;

    }
}
