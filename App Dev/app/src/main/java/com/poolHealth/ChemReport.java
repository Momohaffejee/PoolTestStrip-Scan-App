package com.poolHealth;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.poolHealth.Models.PoolReport;
import com.poolHealth.Models.ScanModel;
import com.poolHealth.util.LabDB;

import java.io.OutputStream;

public class ChemReport extends AppCompatActivity {
    int scn_no;
    ScanModel scanModel;

    PoolReport poolReport;
    TextView txtPlNo, txtPlName;
    TextView txtHardness, txtBromine, txtFC, txtAlk,txtPH;
    Button restart;
    TextView txtReport;
    Button chemreport;

    OutputStream outputStream;

    String value = "";

    @SuppressLint({"MissingInflatedId", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chem_report);
        scn_no = getIntent().getIntExtra("PLNO", 0);
        System.out.println(scn_no);
        if (scn_no == 0) {
            Toast.makeText(getApplicationContext(), "No Scan Selected", Toast.LENGTH_SHORT).show();
            return;
        }

        txtPlNo = findViewById(R.id.txtScanNo);
        txtPlName = findViewById(R.id.txtScanName);

        // Pool Test
        txtHardness = findViewById(R.id.txtTotalHardness);
        txtBromine = findViewById(R.id.txtBromine);
        txtFC = findViewById(R.id.txtFreeChlorine);
        txtAlk = findViewById(R.id.txtAlkalinity);
        txtPH = findViewById(R.id.txtpH);


        txtReport = findViewById(R.id.txtReport);
        chemreport = findViewById(R.id.btnSendDataToPrint);


        // Local Database
        LabDB db = new LabDB(getApplicationContext());
        scanModel = db.getScan(scn_no);
        poolReport = db.getLastPoolReport(scn_no);

        // Pool Details
        txtPlNo.setText(String.valueOf(scn_no));
        txtPlName.setText(scanModel.getPlName());



        String Report = "Your Report Summary : \n";
        int toreport = 0;
        int Hard;
        Hard = (int) poolReport.getTh();
        System.out.println(Hard + "lol");
        if (Hard < 200) {
            if(Hard==0){
                txtHardness.setText("0 = Very Low");
            }
            else{
                txtHardness.setText("100 = Low");
            }
            toreport++;
            Report += " Add Calcium Chloride to Pool to increase Total Hardness \n";
        }
        else if(Hard < 800){
            txtHardness.setText("800 = High");
            toreport++;
            Report += " Partially pump or drain Pool Water and replace with fresh tap or rain water to reduce Total Hardness\n";
        }
        else{
            if(Hard==200)
            {
                txtHardness.setText("200 = IDEAL");
            }
            else{
                txtHardness.setText("400 = IDEAL");
            }
            toreport++;
            Report += " Total Hardness(Calcium) level in Pool is Optimal\n";
        }
        int brom;
        brom = (int) poolReport.getBro();
        System.out.println(brom + "lol2");
        if(brom<2){
            if(brom==0){
                txtBromine.setText("0 = Very Low");
            }
            else{
                txtBromine.setText("1 = Low");
            }
            toreport++;
            Report += " Total Bromine level is Low: Add Bromine tablets or dispensers to Pool.\n Note: Optimal Bromine Levels are more crucial for Spa pools";
        }else if (brom > 10) {
            txtBromine.setText("20 = High");
            toreport++;
            Report += " Total Bromine level is High: 1) Remove any floating bromine dispensers \n 2) Partially drain pool water and add fresh tap water to dilute bromine\n";
        }else{
            txtBromine.setText(brom + " = IDEAL");
            toreport++;
            Report += " Total Bromine level in Pool is Optimal \n Note: Optimal Bromine level is more crucial in Spa pools\n";
        }
        int Free;
        Free = (int) poolReport.getFC();
        System.out.println(Free + "lol3");
        if(Free ==0 ){
            txtFC.setText("0 = Low");
            toreport++;
            Report += " Free Chlorine Level is Low: Add Chlorine Powder or Dispenser to Pool according to Pool Size \n Check Chlorine Control System if in use\n";
        }else if(Free <5 ){
            txtFC.setText(Free + " = IDEAL");
            toreport++;
            Report += " Free Chlorine Level in Pool is Optimal \n  ";

        }
        else{
            txtFC.setText(Free + " = High");
            if(Free == 5){
                toreport++;
                Report += " Free Chlorine Level is Slightly High: Remove any Chlorine dispensers or turn off Chlorine Control System (If Applicable)\n";
            }
            toreport++;
            Report += " Free Chlorine Level is VERY High: WARNING-Avoid using Pool Until Chlorine level decreases \n Remove any Chlorine dispensers or turn off Chlorine Control System (If Applicable)\n";

        }
        float pH;
        pH = (float) poolReport.getPh();
        System.out.println(pH + "lol4");
        if (pH <= 7.1) {

            if(pH == 6.2 ){
                txtPH.setText("6.2 = Very Low");
                toreport++;
                Report += " pH Level is Very Low: Add Pool Soda Ash/pH Increaser to Pool immediately\n";
            }
            else{
                txtPH.setText("6.8 = Low");
                toreport++;
                Report += " pH Level is Low: Add Pool Soda Ash/pH Increaser to Pool\n";
            }

        }
        else if (pH == 8.4){
            txtPH.setText("8.4 = High");
            toreport++;
            Report += " pH Level is High: Add Pool Acid to Pool\n";
        }
        else if(pH >= 7.2){
            txtPH.setText(pH + " = IDEAL");
            toreport++;
            Report += " pH Level in Pool is Optimal\n";
        }


        int Alky;
        Alky = (int) poolReport.getAlk();
        if(Alky<120){
            txtAlk.setText(Alky + " = Low");
            toreport++;
            Report += " Total Alkalinity is Low: Add Pool Alkaline Increaser to Pool \n";
        }else if(Alky==120){
            txtAlk.setText("120 = IDEAL");
            toreport++;
            Report += " Total Alkalinity in Pool is Optimal \n";
        } else {
            txtAlk.setText(Alky + " = High");
            toreport++;
            Report += " Total Alkalinity is High: Add Pool Acid to Pool \n Note: Dilute Pool Acid before adding\n";

        }


        txtReport.setText(Report);
        restart = findViewById(R.id.btnNewScan);

    }

}
