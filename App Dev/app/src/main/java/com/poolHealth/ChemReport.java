package com.poolHealth;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.poolHealth.Models.ChemBalance;
import com.poolHealth.Models.PoolReport;
import com.poolHealth.Models.ScanModel;
import com.poolHealth.util.LabDB;

import java.io.OutputStream;
import java.util.Calendar;

public class ChemReport extends AppCompatActivity {
    int scn_no;
    ScanModel scanModel;
    ChemBalance chemBalance;
    PoolReport poolReport;
    TextView txtPlNo, txtPlName;
    TextView txtThardness, txtBromine, txtFC, txtAlkanility,txtPH;

    TextView txtReport;
    Button chemreport;

    OutputStream outputStream;

    String value = "";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chem_report);
        scn_no = getIntent().getIntExtra("PLNO", 0);
        if (scn_no == 0) {
            Toast.makeText(getApplicationContext(), "No Scan Selected", Toast.LENGTH_SHORT).show();
            return;
        }

        txtPlNo = findViewById(R.id.txtScanNo);
        txtPlName = findViewById(R.id.txtScanName);

        // Pool Test
        txtThardness = findViewById(R.id.txtTotalHardness);
        txtBromine = findViewById(R.id.txtBromine);
        txtFC = findViewById(R.id.txtFreeChlorine);
        txtAlkanility = findViewById(R.id.txtAlkalinity);
        txtPH = findViewById(R.id.txtpH);


        txtReport = findViewById(R.id.txtReport);
        chemreport = findViewById(R.id.btnSendDataToPrint);


        // Local Database
        LabDB db = new LabDB(getApplicationContext());
        scanModel = db.getScan(scn_no);
        chemBalance = db.getLastChemBalance(scn_no);
        poolReport = db.getLastPoolReport(scn_no);

        // Pool Details
        txtPlNo.setText(String.valueOf(scn_no));
        txtPlName.setText(scanModel.getPlName());


        txtThardness.setText(String.valueOf(poolReport.getThardness()));
        String Report = "Your Report Summary : \n";
        int toreport = 0;
        if (poolReport.getThardness() > 0) {
            toreport++;
            Report += txtThardness.getText() +  " Total Hardness of Pool Water \n";
        }

        txtBromine.setText(String.valueOf(poolReport.getBro()));
        if(poolReport.getBro()<2){
            txtBromine.setText("Negative");
        }else if (poolReport.getBro() < 2) {
            toreport++;
            Report += "Small amount of Bromine detected \n";
        }else if(poolReport.getBro()>1){
            toreport++;
            Report += "Bromine levels in Pool Water \n";
        }
        if(poolReport.getFC()<5){
            txtFC.setText("Normal");
        }else {
            txtFC.setText(String.valueOf(poolReport.getFC()));
        }
        if (poolReport.getFC() > 3) {
            Report += "Free Chlorine is High \n";
        }
        if(poolReport.getAlkanility()<120){
            txtAlkanility.setText("Negative");
        }else if(poolReport.getAlkanility()<180){
            txtAlkanility.setText("Trace");
        } else {
            txtAlkanility.setText(String.valueOf(poolReport.getAlkanility()));
        }
        if (poolReport.getAlkanility() > 120) {
            Report += "Pool Water is more Alkaline than ideal \n";
        }
        txtPH.setText(String.valueOf(poolReport.getPh()));
        if (poolReport.getPh() > 7.8) {
            Report += "Your pool is more acidic than Normal Range \n";
        }

        txtReport.setText(Report);
        chemreport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FinalReport();
            }
        });
    }

    void FinalReport(){
        try{
            byte[] format = {29, 33, 35 };
            byte[] center =  { 0x1b, 'a', 0x01 };
            byte[] left=new byte[]{0x1B, 'a',0x00};
            byte[] textSize = new byte[]{0x1B,0x21,0x00};

            outputStream.write(center);
            outputStream.write(format);
            String reporttoprint= "";
            reporttoprint = getString(R.string.app_name).toUpperCase();
            reporttoprint +="\n";
            outputStream.write(textSize);
            outputStream.write(reporttoprint.getBytes());
            outputStream.write(left);
            outputStream.write("Scan Detail\n".getBytes());
            outputStream.write("--------------\n".getBytes());
            String ptLine ="Scan :";
            ptLine += String.valueOf(scanModel.getPlNo());
            ptLine += ", ";
            ptLine += scanModel.getPlName();
            ptLine += "\n";
            outputStream.write(ptLine.getBytes());
            outputStream.write("\n".getBytes());
            outputStream.write("Chem Balance\n".getBytes());
            outputStream.write("----------\n".getBytes());
            outputStream.write("\n".getBytes());
            outputStream.write("------------\n".getBytes());
            outputStream.write("\n".getBytes());
            outputStream.write("Pool Report\n".getBytes());
            outputStream.write("------------\n".getBytes());

            outputStream.write(("Total Hardness : " + txtThardness.getText() + "\n").getBytes());
            outputStream.write(("Bromine : " + txtBromine.getText() + "\n").getBytes());
            outputStream.write(("Free Chlorine : " + txtFC.getText() + "\n").getBytes());
            outputStream.write(("Alkalinity : " + txtAlkanility.getText() + "\n").getBytes());
            outputStream.write(("pH : " + txtPH.getText() + "\n").getBytes());

            outputStream.write("\n".getBytes());
            String ReportSummary = txtReport.getText().toString();
            ReportSummary = ReportSummary.replace("\n","\n");
            outputStream.write(txtReport.getText().toString().getBytes());
            outputStream.write("\n".getBytes());
            outputStream.write(("Printed Date " + Calendar.getInstance().getTime()).getBytes());
            outputStream.write("\n\n\n".getBytes());

            outputStream.close();


        }catch (Exception e){
            value+=e.toString()+ "\n" +"Invalid report \n";
            Toast.makeText(this, value, Toast.LENGTH_LONG).show();
        }
    }
}
