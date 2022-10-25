package com.poolHealth;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.poolHealth.Models.ScanModel;
import com.poolHealth.Models.ChemBalance;
import com.poolHealth.util.LabDB;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ScanDetails extends AppCompatActivity {
    String TAG = "SCANDETAIL";
    Button btnSave;
    private ProgressDialog dialog;

    Button btnPoolTest;
    TextView txtPlno;
    ScanModel newScan = new ScanModel();
    ChemBalance chemBalance = new ChemBalance();
    EditText txtPlName;
    EditText txtAge;
    RadioButton optMale,optFemale,optOther;
    private  static  int TAKE_PICTURE = 111;
    String mCurrentPhotoPath;
    Context thisContext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_details);
        thisContext = ScanDetails.this;
        dialog = new ProgressDialog(this);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);

        txtPlno = findViewById(R.id.txtScanNo);

//        isStoragePermissionGranted();
        isCameraPermissionGranted();
        btnSave = findViewById(R.id.btnSave);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(datavalidation()){
                    Toast.makeText(getApplicationContext(),"Starting",Toast.LENGTH_LONG).show();
                    new savedata().execute();
                }
            }
        });

        btnPoolTest = findViewById(R.id.btnPoolTest);
        btnPoolTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(txtPlno.getText().toString().equals("")){
                    // Show Message to Save Record
                    Toast.makeText(getApplicationContext(),"Please Click New Pool to continue",Toast.LENGTH_LONG).show();
                    return;
                }

                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    // Create the File where the photo should go
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    // Continue only if the File was successfully created
                    if (photoFile != null) {
//                        Uri photoURI  = Uri.fromFile(photoFile);
                        Uri photoURI  = FileProvider.getUriForFile(ScanDetails.this,
                                "com.poolhealth.fileprovider",
                                photoFile);

                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                        startActivityForResult(takePictureIntent, TAKE_PICTURE);
                    }
                }
            }
        });

    }
    private class savedata extends AsyncTask<String,Void,String>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.setMessage("Starting");
            dialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
//            newPatient = new ScanModel();



            // Save Patient Detail
            LabDB db = new LabDB(getApplicationContext());

            int np = db.SaveScan(newScan);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            newScan.setPlNo(np);
            // Save Vital Sign
            chemBalance.setScn_no(newScan.getPlNo());


            int last_chembalance_row_id = db.SaveChemBalance(chemBalance);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            chemBalance.setRow_id(last_chembalance_row_id);
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if(dialog.isShowing())
                dialog.dismiss();
            txtPlno.setText(String.valueOf(newScan.getPlNo()));
            Toast.makeText(getApplicationContext(),"Pool Saved " + newScan.getPlNo() + " V " + chemBalance.getRow_id(),Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==TAKE_PICTURE && resultCode == Activity.RESULT_OK){
            if(mCurrentPhotoPath!=null) {
                int plno = Integer.parseInt(txtPlno.getText().toString());
                Intent pool_test_comfirm = new Intent(ScanDetails.this, PoolConfirmActivity.class);
                pool_test_comfirm.putExtra("PLNO", plno);
                pool_test_comfirm.putExtra("photopath", mCurrentPhotoPath);
                startActivity(pool_test_comfirm);
            }else {
                Toast.makeText(ScanDetails.this,"Couldn't Take photo,Please Try Again",Toast.LENGTH_LONG).show();
            }
        }
    }
    public  boolean isCameraPermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"Permission is granted");
                return true;
            } else {

                Log.v(TAG,"Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Permission is granted");
            return true;
        }
    }

    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"Permission is granted");
                return true;
            } else {

                Log.v(TAG,"Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Permission is granted");
            return true;
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
            Log.v(TAG,"Permission: "+permissions[0]+ "was "+grantResults[0]);
            //resume tasks needing this permission
        }

    }

    /** Create a File for saving an image or video */
    private File getOutputMediaFile(){

        File mediaStorageDir = new File(String.valueOf(getApplicationContext().getExternalFilesDir("scn_photos")));
        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("poolHealth", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                "IMG_"+ txtPlno.getText().toString() + ".jpg");
        return mediaFile;
    }
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "POOL_" + txtPlno.getText().toString() + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }
    private boolean datavalidation(){
        boolean result = true;




        return result;
    }
}
