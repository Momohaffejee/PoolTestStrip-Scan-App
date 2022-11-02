package com.poolHealth;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.poolHealth.util.PatchClassifier;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PoolConfirmActivity extends AppCompatActivity {
    int plno;
    ImageView mImageView;
    String mCurrentPhotoPath, imageString;
    Bitmap bitmap;
    Button recapture, done;
    TextView tv;
    String selecteditem;
    ArrayList<Float> colour_list;

    {
        selecteditem = "All";
    }

    public Mat original_image, initial_image; //for image



    final Size kernelSize = new Size(7, 7);

    PatchClassifier patchClassifier;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pool_confirm);
        plno = getIntent().getIntExtra("PLNO", 0);
        mImageView = findViewById(R.id.camera_photo);
        tv = findViewById(R.id.text_view);


        recapture = findViewById(R.id.reopencamera);
        done = findViewById(R.id.done);
        if (plno == 0) return;

        if (!OpenCVLoader.initDebug()) {
            Log.e(this.getClass().getSimpleName(), "  OpenCVLoader.initDebug(), not working.");
        } else {
            Log.d(this.getClass().getSimpleName(), "  OpenCVLoader.initDebug(), working.");
        }
        mCurrentPhotoPath = getIntent().getStringExtra("photopath");
        patchClassifier = new PatchClassifier(PoolConfirmActivity.this, plno);





        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);

        bmOptions.inJustDecodeBounds = false;

        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);

        ExifInterface ei = null;
        Bitmap rotatedBitmap = null;
        try {
            ei = new ExifInterface(mCurrentPhotoPath);
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED);

            switch(orientation) {

                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotatedBitmap = rotateImage(bitmap, 90);
                    break;

                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotatedBitmap = rotateImage(bitmap, 180);
                    break;

                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotatedBitmap = rotateImage(bitmap, 270);
                    break;

                case ExifInterface.ORIENTATION_NORMAL:
                default:
                    rotatedBitmap = bitmap;
            }

        } catch (IOException e) {
            e.printStackTrace();
            rotatedBitmap = bitmap;
        }

        bitmap = rotatedBitmap;
        mImageView.setImageBitmap(rotatedBitmap);

        //setPic();


        System.out.println("Picture Loaded");

        recapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
                finish();
            }
        });
        Bitmap finalBitmap = bitmap;
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Python py = Python.getInstance();
                imageString = getStringImage(finalBitmap);
                colour_list = new ArrayList<>();
                List<PyObject> obj = py.getModule("yolo_object_detection").callAttr("main",imageString).asList();
                System.out.println(obj);
                Float val;

                for (PyObject elem : obj){
                    colour_list.add(new Float(elem.toFloat()));
                }
                System.out.println(colour_list);
               for(int i=0;i<obj.size();i++){
                    val = colour_list.get(i);
                    if(val==-1){
                        tv.setText(obj.get(i).toString());
                        break;
                    }
                    if(i==1){

                        tv.setText(obj.get(i).toString());
                    }


                }


                patchClassifier.classifyData(colour_list, PoolConfirmActivity.this, plno);
                //patchClassifier.classifyData(colour_list, PoolTestActivity.this, plno);
                //finish();
                //if(done.getText().equals("OK")){

               // }


               // bitmapToMat();
            }
        });
    }

    private void setPic() {

        if (! Python.isStarted()) {
            Python.start(new AndroidPlatform(this ));
        }


    }

    private String getStringImage(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100,baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = android.util.Base64.encodeToString(imageBytes,Base64.DEFAULT);
        return encodedImage;
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

}




