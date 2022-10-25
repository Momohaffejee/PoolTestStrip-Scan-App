package com.poolHealth;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.poolHealth.Models.ScanModel;
import com.poolHealth.Models.PoolReport;
import com.poolHealth.Models.ChemBalance;
import com.poolHealth.util.LabDB;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.Set;
import java.util.UUID;

public class PrintReport extends AppCompatActivity {
    int scn_no;
    ScanModel scanModel;
    ChemBalance chemBalance;
    PoolReport poolReport;
    TextView txtPlNo, txtPlName;
    TextView txtThardness, txtBromine, txtFC, txtAlkanility,txtPH;

    TextView txtReport;
    Button printreport;

    // For Print
    BluetoothAdapter bluetoothAdapter;
    BluetoothSocket socket;
    BluetoothDevice bluetoothDevice;
    OutputStream outputStream;
    InputStream inputStream;
    Thread workerThread;
    byte[] readBuffer;
    int readBufferPosition;
    volatile boolean stopWorker;
    String value = "";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print_report);
        scn_no = getIntent().getIntExtra("PLNO", 0);
        if (scn_no == 0) {
            Toast.makeText(getApplicationContext(), "No Scan Selected", Toast.LENGTH_SHORT).show();
            return;
        }
        // Patient Details
        txtPlNo = findViewById(R.id.txtScanNo);
        txtPlName = findViewById(R.id.txtScanName);
//        txtSex = findViewById(R.id.txtSex);

        // Pool Test
        txtThardness = findViewById(R.id.txtTotalHardness);
        txtBromine = findViewById(R.id.txtBromine);
        txtFC = findViewById(R.id.txtFreeChlorine);
        txtAlkanility = findViewById(R.id.txtAlkalinity);
        txtPH = findViewById(R.id.txtpH);


        txtReport = findViewById(R.id.txtReport);
        printreport = findViewById(R.id.btnSendDataToPrint);
        // Retrive From Database
        LabDB db = new LabDB(getApplicationContext());
        scanModel = db.getScan(scn_no);
        //System.out.println("Patient Sex " + scanModel.getPtSex());
        chemBalance = db.getLastChemBalance(scn_no);

        poolReport = db.getLastPoolReport(scn_no);

        // Set Text
        // Scan Details
        txtPlNo.setText(String.valueOf(scn_no));
        txtPlName.setText(scanModel.getPlName());

//        txtSex.setText(scanModel.getPtSex());


        // Fillup Pool Report
        txtThardness.setText(String.valueOf(poolReport.getThardness()));
        if(poolReport.getThardness()<1){
            txtThardness.setText("Nil");
        }else if(poolReport.getThardness()<16){
            txtThardness.setText("Trace");
        }else if(poolReport.getThardness()<71){
            txtThardness.setText("Small ");
        }else if(poolReport.getThardness()<126){
            txtThardness.setText("Modrate");
        }else if(poolReport.getThardness()<501){
            txtThardness.setText("Large");
        }
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
            Report += "Free Chlorine is Higher than Normal Range \n";
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
        printreport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PrintThisReport();
            }
        });
    }
    void InitPrinter(){
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        try
        {
            if(!bluetoothAdapter.isEnabled())
            {
                Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBluetooth, 0);
                return;
            }

            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

            if(pairedDevices.size() > 0)
            {
                for(BluetoothDevice device : pairedDevices)
                {
                    if(device.getName().equals("MTP-II")) //Note, you will need to change this to match the name of your device
                    {
                        bluetoothDevice = device;
                        break;
                    }
                }

                UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //Standard SerialPortService ID
                Method m = bluetoothDevice.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
                socket = (BluetoothSocket) m.invoke(bluetoothDevice, 1);
                bluetoothAdapter.cancelDiscovery();
                socket.connect();
                outputStream = socket.getOutputStream();
                inputStream = socket.getInputStream();
                beginListenForData();
            }
            else
            {
                value+="No Devices found";
                Toast.makeText(this, value, Toast.LENGTH_LONG).show();
                return;
            }
        }
        catch(Exception ex)
        {
            value+=ex.toString()+ "\n" +" InitPrinter \n";
            Toast.makeText(this, value, Toast.LENGTH_LONG).show();
        }
    }
    void beginListenForData(){
        try {
            final Handler handler = new Handler();

            // this is the ASCII code for a newline character
            final byte delimiter = 10;

            stopWorker = false;
            readBufferPosition = 0;
            readBuffer = new byte[1024];

            workerThread = new Thread(new Runnable() {
                public void run() {

                    while (!Thread.currentThread().isInterrupted() && !stopWorker) {

                        try {

                            int bytesAvailable = inputStream.available();

                            if (bytesAvailable > 0) {

                                byte[] packetBytes = new byte[bytesAvailable];
                                inputStream.read(packetBytes);

                                for (int i = 0; i < bytesAvailable; i++) {

                                    byte b = packetBytes[i];
                                    if (b == delimiter) {

                                        byte[] encodedBytes = new byte[readBufferPosition];
                                        System.arraycopy(
                                                readBuffer, 0,
                                                encodedBytes, 0,
                                                encodedBytes.length
                                        );

                                        // specify US-ASCII encoding
                                        final String data = new String(encodedBytes, "US-ASCII");
                                        readBufferPosition = 0;

                                        // tell the user data were sent to bluetooth printer device
                                        handler.post(new Runnable() {
                                            public void run() {
                                                Log.d("e", data);
                                            }
                                        });

                                    } else {
                                        readBuffer[readBufferPosition++] = b;
                                    }
                                }
                            }

                        } catch (IOException ex) {
                            stopWorker = true;
                        }

                    }
                }
            });

            workerThread.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void PrintThisReport(){
        try{
            byte[] format = {29, 33, 35 }; // manipulate your font size in the second parameter
            byte[] center =  { 0x1b, 'a', 0x01 }; // center alignmen
            byte[] left=new byte[]{0x1B, 'a',0x00};
            byte[] textSize = new byte[]{0x1B,0x21,0x00}; // 2- bold with medium text
            // Start Printer
            InitPrinter();
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
            // End Of Report
            outputStream.close();
            socket.close();


        }catch (Exception e){
            value+=e.toString()+ "\n" +"Excep Print \n";
            Toast.makeText(this, value, Toast.LENGTH_LONG).show();
        }
    }
}
