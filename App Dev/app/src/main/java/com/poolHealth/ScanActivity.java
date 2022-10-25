package com.poolHealth;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.poolHealth.util.LabDB;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


public class ScanActivity extends AppCompatActivity {
    Button btnNewScan;
    String server_url = "";
    SharedPreferences sharedPreferences;
    public static final int PICK_FILE= 101;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        server_url = getString(R.string.server_address);
        sharedPreferences = this.getSharedPreferences("poolhealth", Context.MODE_PRIVATE);
        String urlofserver = sharedPreferences.getString("SERVERADDRESS",server_url);
        server_url = urlofserver;
        btnNewScan = findViewById(R.id.btnNewScan);
        btnNewScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent patientIntent = new Intent(getApplicationContext(), ScanDetails.class);
                startActivity(patientIntent);
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.setting:
                //startActivity(new Intent(this, About.class));
//                AlertDialog.Builder builder = new AlertDialog.Builder(this);
//                builder.setTitle("Server Address");
//
//// Set up the input
//                final EditText input = new EditText(this);
//// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
//                input.setInputType(InputType.TYPE_CLASS_TEXT);
//                input.setText(server_url);
//                builder.setView(input);
//
//// Set up the buttons
//                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        server_url = input.getText().toString();
//                        sharedPreferences.edit().putString("SERVERADDRESS",server_url).apply();
//                    }
//                });
//                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.cancel();
//                    }
//                });
//
//                builder.show();
                Intent intent = new Intent();
                intent.setType("*/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                startActivityForResult(Intent.createChooser(intent, "Select Json File"), PICK_FILE);

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_FILE && resultCode == Activity.RESULT_OK){
            Uri uri = data.getData();
            String fileContent = readTextFile(uri);
            Log.d("Selected file content ",fileContent);
            // Replace data
            LabDB db = new LabDB(this);
            db.setPoolData(fileContent);
        }
    }
    private String readTextFile(Uri uri)
    {
        BufferedReader reader = null;
        StringBuilder builder = new StringBuilder();
        try
        {
            reader = new BufferedReader(new InputStreamReader(getContentResolver().openInputStream(uri)));

            String line = "";
            while ((line = reader.readLine()) != null)
            {
                builder.append(line);
            }
            reader.close();
        }
        catch (IOException e) {e.printStackTrace();}
        return builder.toString();
    }

}
