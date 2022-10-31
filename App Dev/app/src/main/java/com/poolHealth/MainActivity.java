package com.poolHealth;

import android.app.Activity;
import android.content.ComponentName;
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
import android.widget.TextView;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.poolHealth.util.LabDB;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


public class MainActivity extends AppCompatActivity {
    Button btnNewScan, about_button, btnInstructions;
    String server_url = "";
    TextView tv;
    SharedPreferences sharedPreferences;
    public static final int PICK_FILE= 101;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (! Python.isStarted()) {
            Python.start(new AndroidPlatform(this ));
        }

        tv = (TextView)findViewById(cR.id.text_view);
        server_url = getString(R.string.server_address);
        sharedPreferences = this.getSharedPreferences("poolhealth", Context.MODE_PRIVATE);
        String urlofserver = sharedPreferences.getString("SERVERADDRESS",server_url);
        server_url = urlofserver;
        about_button = findViewById(R.id.about_button);
        about_button.setOnClickListener(this::onAboutButtonClicked);
        btnInstructions = findViewById(R.id.btnInstructions);
        btnInstructions.setOnClickListener(this::onInstructionsClicked);

        Python py = Python.getInstance();
        PyObject pyobj = py.getModule("myscript");

        PyObject obj = pyobj.callAttr("main");
        tv.setText(obj.toString());


        btnNewScan = findViewById(R.id.btnNewScan);
        btnNewScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent poolIntent = new Intent(getApplicationContext(), ScanDetails.class);
                startActivity(poolIntent);
            }
        });

    }

    private void onAboutButtonClicked(View v){
        final Intent aboutIntent = new Intent();
        aboutIntent.setComponent(new ComponentName(this, AboutActivity.class));
        startActivity(aboutIntent);
    }

    private void onInstructionsClicked(View v){
        final Intent aboutIntent = new Intent();
        aboutIntent.setComponent(new ComponentName(this, Instructions.class));
        startActivity(aboutIntent);
    }

    private void onpytest(View v){


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
                Intent intent = new Intent();
                intent.setType("*/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setAction(Intent.ACTION_OPEN_DOCUMENT);

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
