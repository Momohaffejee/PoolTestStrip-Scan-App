package com.poolHealth;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PoolConfirmActivity extends AppCompatActivity {
    int plno;
    ImageView mImageView;
    String mCurrentPhotoPath;
    Bitmap currentpict;
    Button recapture, done;


    public Mat original_image, initial_image; //for image

    ArrayList<Scalar> colour_list; //colour patches

    final Size kernelSize = new Size(7, 7);

    PatchClassifier patchClassifier;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pool_confirm);
        plno = getIntent().getIntExtra("PLNO", 0);
        mImageView = findViewById(R.id.camera_photo);
        recapture = findViewById(R.id.reopencamera);
        done = findViewById(R.id.done);
        if (plno == 0) return;

        if (!OpenCVLoader.initDebug()) {
            Log.e(this.getClass().getSimpleName(), "  OpenCVLoader.initDebug(), not working.");
        } else {
            Log.d(this.getClass().getSimpleName(), "  OpenCVLoader.initDebug(), working.");
        }

        patchClassifier = new PatchClassifier(PoolConfirmActivity.this, plno);

        mCurrentPhotoPath = getIntent().getStringExtra("photopath");



        setPic();


        System.out.println("Picture Loaded");

        recapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
                finish();
            }
        });
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentpict == null) {

                    return;
                } else {

                }
                if(done.getText().equals("OK")){
                    patchClassifier.classifyData(colour_list, PoolConfirmActivity.this, plno);
                    finish();
                }


                bitmapToMat();
            }
        });
    }

    private void setPic() {

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

        currentpict = rotatedBitmap;
        mImageView.setImageBitmap(rotatedBitmap);
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    public void bitmapToMat() {

        initial_image = new Mat();


        BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();
        bmpFactoryOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;



        Utils.bitmapToMat(currentpict, initial_image);

        getrectangle(initial_image);
    }


    public void getrectangle(Mat matImage) {

        try {
            original_image = new Mat();
            matImage.copyTo(original_image);

            Scalar min_white = new Scalar(0, 0, 50);
            Scalar max_white = new Scalar(255, 255, 180);

            // converting BGR to RGB
//        Imgproc.cvtColor(matImage, matImage, Imgproc.COLOR_BGR2RGB);

            System.out.println("Type here = " + matImage.type());

            int x1 = original_image.width() / 2;
            x1 += 200;
            int y1 = 0;
            int x2 = original_image.width();
            int y2 = original_image.height();
            Rect roi = new Rect(x1, y1, x2 - x1, y2);
            original_image.copyTo(matImage);
            Imgproc.cvtColor(matImage, matImage, Imgproc.COLOR_BGR2GRAY);
            Imgproc.medianBlur(matImage, matImage, 7);
            Imgproc.Canny(matImage, matImage, 12, 8);


            RotatedRect rect = find_rectangle_contour(matImage);

            position_of_first_and_last_patches(original_image, rect);
        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(PoolConfirmActivity.this,"Error Processing Image1, Please Try with another one",Toast.LENGTH_LONG).show();
        }
    }

    public void position_of_first_and_last_patches(Mat image, RotatedRect rect) {
        try {
            Mat orig_cropped = new Mat();
            image.copyTo(orig_cropped);

            Imgproc.GaussianBlur(image, image, kernelSize, 0);
            Imgproc.cvtColor(image, image, Imgproc.COLOR_RGB2HSV);

            Mat white_rectangle_hsv = crop_rotated_rectangle(image, rect);
        showimage(white_rectangle_hsv);

            int pro_x1 = 0;
            int pro_y1 = (int) (white_rectangle_hsv.height() * 0.2);

            int pro_height = (int) (white_rectangle_hsv.width() * 1.5);
            int pro_width = white_rectangle_hsv.width();


            Rect roi_pro = new Rect(pro_x1, pro_y1, pro_width, pro_height);
            Mat pro_patch = new Mat(white_rectangle_hsv, roi_pro);


            Scalar min_pro1 = new Scalar(34, 50, 100);
            Scalar max_pro1 = new Scalar(100, 179, 200);


            Scalar min_pro2 = new Scalar(20, 90, 87);
            Scalar max_pro2 = new Scalar(40, 200, 147);


            Scalar min_pro3 = new Scalar(13, 147, 180);
            Scalar max_pro3 = new Scalar(40, 236, 191);


            Scalar min_pro4 = new Scalar(150, 50, 40);
            Scalar max_pro4 = new Scalar(220, 90, 100);


            Mat mask_pro1 = new Mat();
            Mat mask_pro2 = new Mat();
            Mat mask_pro3 = new Mat();


            Core.inRange(pro_patch, min_pro1, max_pro1, mask_pro1);
            Core.inRange(pro_patch, min_pro2, max_pro2, mask_pro2);


            try {
                Core.add(mask_pro1, mask_pro2, mask_pro1);


            } catch (Exception e) {
                System.out.println("Eror occur");
                ;
            }
            Imgproc.threshold(mask_pro1, mask_pro1, 120, 255, Imgproc.THRESH_BINARY);

            Point[] pro_points = find_rectangle_contour_second(mask_pro1);


            pro_points[0].y += pro_y1;
            pro_points[3].y += pro_y1;


            Mat white_rectangle = new Mat();

            Imgproc.cvtColor(white_rectangle_hsv, white_rectangle, Imgproc.COLOR_HSV2RGB);

            Mat fortest = new Mat();
            white_rectangle.copyTo(fortest);
            Imgproc.rectangle(white_rectangle, new Point(pro_points[0].x, pro_points[0].y), new Point(pro_points[3].x, pro_points[3].y), new Scalar(255, 0, 0, 255), 6);


            int asc_x1 = (int) (white_rectangle.width() * 0.25);

            int asc_y1 = (int) (white_rectangle.height() * 0.7);

            int asc_height = (int) (white_rectangle.width() * 1.5);
            int asc_width = (int) (white_rectangle.width() * 0.6);


            Rect roi_asc = new Rect(asc_x1, asc_y1, asc_width, asc_height);
            Mat asc_patch = new Mat(white_rectangle, roi_asc);

            Imgproc.cvtColor(asc_patch, asc_patch, Imgproc.COLOR_RGB2GRAY);
            Imgproc.threshold(asc_patch, asc_patch, 120, 255, Imgproc.THRESH_BINARY);
            Core.bitwise_not(asc_patch, asc_patch);

            final Size kernelSize = new Size(7, 7);
            final Point anchor = new Point(-1, -1);
            final int iterations = 5;
            Mat kernal = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, kernelSize);
            Imgproc.erode(asc_patch, asc_patch, kernal, anchor, iterations);


            Point[] asc_points = find_rectangle_contour_second(asc_patch);
            asc_points[0].x += asc_x1;
            asc_points[3].x += asc_x1;

            asc_points[0].y += asc_y1;
            asc_points[3].y += asc_y1;


            Imgproc.rectangle(white_rectangle, new Point(asc_points[0].x, asc_points[0].y), new Point(asc_points[3].x, asc_points[3].y), new Scalar(255, 0, 0, 255), 6);

            int centerx1 = (int) (int) ((pro_points[0].x + pro_points[3].x) * 0.5);
            int centery1 = (int) (int) ((pro_points[0].y + pro_points[3].y) * 0.5);

            int centerx2 = (int) (int) ((asc_points[0].x + asc_points[3].x) * 0.5);
            int centery2 = (int) (int) ((asc_points[0].y + asc_points[3].y) * 0.5);

            int xl = 0;
            int yl = 0;
            int xr = 0;
            int yr = 0;


            Point points_on_centerline = new Point();
            Point point_left = new Point();
            Point point_right = new Point();
            int distance = getDistance((int) centerx1, centery1, centerx2, centery2);
            int step = (int) (distance / 7);
            int margin = 0;
            ArrayList<Rect> patches = new ArrayList<>();

            for (int i = 0; i < 5; i++) {
                Rect toadd = new Rect();
                Scalar color = new Scalar(0, 0, 0);
                toadd.x = 0;
                toadd.y = 0;
                toadd.width = 0;
                toadd.height = 0;
                patches.add(toadd);

            }
            int pos = 3;
            for (int i = 0; i < 8; i++) {

                points_on_centerline = pointOnLine(distance, centerx1, centery1, centerx2, centery2, margin);

                margin += step;

                int xt = (int) points_on_centerline.x - 40;
                int yt = (int) points_on_centerline.y - 40;

                int width = (int) points_on_centerline.x + 40;
                int height = (int) points_on_centerline.y + 40;
                Point newPoint1 = new Point(xt, yt);
                Point newPoint2 = new Point(width, height);

                patches.get(pos).x = xt;
                patches.get(pos).y = yt;
                patches.get(pos).width = width;
                patches.get(pos).height = height;
                Imgproc.rectangle(white_rectangle, newPoint1, newPoint2, new Scalar(0, 255, 0, 255), 6);


                pos++;
            }
            margin = -step;
            pos = 2;
            for (int i = 0; i < 3; i++) {
                points_on_centerline = pointOnLine(distance, centerx1, centery1, centerx2, centery2, margin);
                margin -= step ;

                int xt = (int) points_on_centerline.x - 40;
                int yt = (int) points_on_centerline.y - 40;

                int width = (int) points_on_centerline.x + 40;
                int height = (int) points_on_centerline.y + 40;
                Point newPoint1 = new Point(xt, yt);
                Point newPoint2 = new Point(width, height);

                patches.get(pos).x = xt;
                patches.get(pos).y = yt;
                patches.get(pos).width = width;
                patches.get(pos).height = height;
                Imgproc.rectangle(white_rectangle, newPoint1, newPoint2, new Scalar(0, 255, 0, 255), 6);
                pos--;
            }
            showimage(white_rectangle);
            plotPatch(patches, fortest);
        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(PoolConfirmActivity.this,"Error Processing Image2,Please try with another image",Toast.LENGTH_LONG).show();
        }

    }

    public int getDistance(int x1, int y1, int x2, int y2) {

        return (int) Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }


    public void plotPatch(ArrayList<Rect> colorlist, Mat patchImage) {
        try {
            colour_list = new ArrayList<>();
            for (int i = 0; i < 11; i++) {
                int x = colorlist.get(i).x;
                int y = colorlist.get(i).y;
                int w = colorlist.get(i).width - colorlist.get(i).x;
                int h = colorlist.get(i).height - colorlist.get(i).y;
                System.out.println("Color List ko " + x + " " + y + " " + w + " " + h);
                Rect roi = new Rect(x, y, w, h);
                Mat cropped = new Mat(patchImage, roi);

                // adding the extracted colors to the color list
                colour_list.add(getMeanRGB(cropped));
            }
            done.setText("OK");

        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(PoolConfirmActivity.this,"Error Processing Image3, Please Recapture the image and try aggain",Toast.LENGTH_LONG).show();
        }

    }

    //getting points on the center line
    public Point pointOnLine(int distance, int x1, int y1, int x2, int y2, double step_length) {

        System.out.println("Step length here = " + step_length);
        System.out.println("Distance = " + distance);
        // getting the step to line ratio
        double ratio = step_length / distance;
        System.out.println("New ratio = " + ratio);

        double xt = (x1 * (1 - ratio)) + (x2 * ratio);
        double yt = (y1 * (1 - ratio)) + (y2 * ratio);
        System.out.println("New y coordinate = " + (y1 * (1 - ratio)) + " arko point" + (y2 * ratio));

        Point newPoint = new Point(xt, yt);
        System.out.println("Center point here = " + xt + " and " + yt);
        return newPoint;
    }


    public Scalar getMeanRGB(Mat matImage) {
        return Core.mean(matImage);
    }

    void showimage(Mat matImage) {

        Bitmap bm = Bitmap.createBitmap(matImage.cols(), matImage.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(matImage, bm);

        mImageView.setImageBitmap(bm);
    }

    private static final String pad(String s) {
        return (s.length() == 1) ? "0" + s : s;
    }



    public RotatedRect find_rectangle_contour(Mat matImage) {
        try {
            final Size kernelSize = new Size(6, 6);
            final Point anchor = new Point(-1, -1);
            final int iterations = 1;
            List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
            Mat kernal = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, kernelSize);
            Imgproc.dilate(matImage, matImage, kernal, anchor, iterations);
            Imgproc.threshold(matImage, matImage, 127, 255, Imgproc.THRESH_BINARY);
            Imgproc.findContours(matImage, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

            double area = 0;
            int maxId = 0;
            for (MatOfPoint cont : contours) {
                MatOfPoint2f temp = new MatOfPoint2f(cont.toArray());
                if (Imgproc.contourArea(cont) > area) {
                    area = Imgproc.contourArea(cont);
                    maxId = contours.indexOf(cont);
                }
            }

            MatOfPoint maxMatOfPoint = contours.get(maxId);
            MatOfPoint2f maxMatOfPoint2f = new MatOfPoint2f(maxMatOfPoint.toArray());
            RotatedRect rect = Imgproc.minAreaRect(maxMatOfPoint2f);

            Point points[] = new Point[4];
            rect.points(points);

            Point point1[] = points;

            double[] length = new double[4];


            for (int k = 0; k < 4; k++) {
                length[k] = Math.sqrt(Math.pow(points[k].x, 2) + Math.pow(points[k].y, 2));

            }


            for (int i = 0; i < 4; i++) {
                for (int j = i + 1; j < 4; j++) {
                    if (length[i] >= length[j]) {
                        double temp = length[i];
                        length[i] = length[j];
                        length[j] = temp;

                        double tempx = points[i].x;
                        double tempy = points[i].y;

                        points[i].x = points[j].x;
                        points[i].y = points[j].y;

                        points[j].x = tempx;
                        points[j].y = tempy;


                    }


                }
            }
            System.out.println("here is the sorted points" + points[0].x + "\t" + points[0].y + "\n" + points[1].x + "\t" + points[1].y + "\n" + points[2].x + "\t" + points[2].y + "\n" + points[3].x + "\t" + points[3].y);

            return rect;
        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(PoolConfirmActivity.this,"Error While Processing Image",Toast.LENGTH_LONG).show();
            return null;
        }

    }


    public Mat crop_rotated_rectangle(Mat matImage,RotatedRect rect) {
        try {
            System.out.println("Needed Values mat size " + matImage.width() + " x " + matImage.height());
            System.out.println("Needed Values rect center " + rect.center.x + " " + rect.center.y);

            double theta = rect.angle;
            double orgtheta = theta;
            System.out.println("theta degree " + theta);

            int width = (int) rect.size.width;
            int height = (int) rect.size.height;


            if (theta > 45 && theta <= 90) {

                int a = width;

                width = height;

                height = a;
                System.out.println("Needed Values Swap Vayo");
            }else {
                System.out.println("Needed Values Swap Vayena");
            }

            System.out.println("Needed Values " + theta);
            theta *= Math.PI / 180;
            double v_x_0 = Math.cos((theta));
            double v_x_1 = Math.sin(theta);
            double v_y_0 = -(Math.sin(theta));
            double v_y_1 = Math.cos(theta);
            System.out.println("Needed Values centerx" + rect.center.x);
            System.out.println("Needed Values centery " + rect.center.y);
            double center_x = rect.center.x;
            double center_y = rect.center.y;
            double s_x = center_x - v_x_0 * (rect.size.width / 2) - v_y_0 * (rect.size.height / 2);
            double s_y = center_y - v_x_1 * (rect.size.width / 2) - v_y_1 * (rect.size.height / 2);
            System.out.println("Needed Values sx " + s_x + " " + s_y);


            Mat rotat = new Mat();
            Mat rot_mat = new Mat(2, 3, CvType.CV_64FC1);
            double data[] = {v_x_0, v_y_0, s_x, v_x_1, v_y_1, s_y};
            rot_mat.put(0, 0, data);
            System.out.println("Needed Values vx0" + v_x_0 + " v_y_0 " + v_y_0 + " vx1 " + v_x_1 + " vy1 " + v_y_1 + " theta " + theta + " s x " + s_x + " sy " + s_y);
            Imgproc.warpAffine(matImage, rotat, rot_mat, new Size(width, height), Imgproc.WARP_INVERSE_MAP, Core.BORDER_REPLICATE);
            if(orgtheta<-45 && orgtheta >-90 ){
                Core.rotate(rotat,rotat,Core.ROTATE_90_COUNTERCLOCKWISE);
                System.out.println("Needed Values rotate vayo");
            }else{
                System.out.println("Needed Values theta at last " + theta);
            }
            return rotat;
        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(PoolConfirmActivity.this,"Error Processing Image4",Toast.LENGTH_LONG).show();
            return null;
        }

    }


    public Point[] find_rectangle_contour_second(Mat matImage)
    {
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(matImage, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        double area =0;
        int maxId=0;
        for(MatOfPoint cont : contours){
            MatOfPoint2f temp = new MatOfPoint2f(cont.toArray());
            if(Imgproc.contourArea(cont)>area){
                area = Imgproc.contourArea(cont);
                maxId = contours.indexOf(cont);
            }
        }
        MatOfPoint maxMatOfPoint = contours.get(maxId);
        MatOfPoint2f maxMatOfPoint2f = new MatOfPoint2f(maxMatOfPoint.toArray());
        RotatedRect rect = Imgproc.minAreaRect(maxMatOfPoint2f);
        Point points[] = new Point[4];
        rect.points(points);
        Point point1[]=points;
        double[] length= new double[4];
        for (int k=0;k<4;k++)
        {
            length[k] =  Math.sqrt(Math.pow(points[k].x,2)+Math.pow(points[k].y,2));

        }
        for (int i =0 ;i<4;i++)
        {
            for(int j= i+1;j<4;j++)
            {
                if(length[i]>=length[j])
                {
                    double temp = length[i];
                    length[i] = length[j];
                    length[j] = temp;
                    double tempx = points[i].x;
                    double tempy = points[i].y;

                    points[i].x =  points[j].x;
                    points[i].y = points[j].y;

                    points[j].x = tempx;
                    points[j].y = tempy;


                }


            }
        }
        System.out.println("here is the sorted points"+points[0].x+"\t"+points[0].y+"\n"+points[1].x+"\t"+points[1].y+"\n"+points[2].x+"\t"+points[2].y+"\n"+points[3].x+"\t"+points[3].y);

        return points;
    }


}




