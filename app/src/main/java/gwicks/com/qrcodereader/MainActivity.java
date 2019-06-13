package gwicks.com.qrcodereader;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;
import java.util.List;

import static android.view.View.GONE;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    SurfaceView sv;
    CameraSource mCameraSource;
    TextView mTextView;
    TextView mTextView2;
    BarcodeDetector mBarcodeDetector;
    Button myButton;
    int height;
    int width;
    //ImageView qrSquare;

    public String result= null;

    public void setString(String string){
        mTextView2.setText(string);
        sv.setVisibility(View.GONE);
        //qrSquare.setVisibility(View.GONE);
        mTextView.setVisibility(View.VISIBLE);
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myButton = findViewById(R.id.button);
        mTextView = findViewById(R.id.cameraViewText);
        mTextView2 =findViewById(R.id.textView);



    }

    @Override
    protected void onResume() {
        super.onResume();
        if(result != null){
            Log.d(TAG, "onResume: result +1 null");
        }

    }


    public void scanForCode(View v){




        height = mTextView.getHeight();
        Log.d(TAG, "on: height: " + height);
        width = mTextView.getWidth();
        Log.d(TAG, "on: width: " + width + " next" );


        mTextView.setVisibility(GONE);


        sv = (SurfaceView) findViewById(R.id.cameraView);

        sv.setVisibility(View.VISIBLE);


        mBarcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.QR_CODE).build();
//
        mCameraSource = new CameraSource.Builder(this, mBarcodeDetector)
                .setRequestedPreviewSize(1024, 768).build();

        Camera mCamera = Camera.open();

//
        Camera.Parameters parameters = mCamera.getParameters();
        List<Camera.Size> sizes= parameters.getSupportedPreviewSizes();
        for(int i = 0 ; i< sizes.size(); i++){
            Log.d(TAG, "scanForCode: value is: " + sizes.get(i).toString());
            Log.d(TAG, "scanForCode: " + sizes.get(i).width);
            Log.d(TAG, "scanForCode: =" +  sizes.get(i).height);
        }


        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int widthy = displayMetrics.widthPixels;
        int heighty = displayMetrics.heightPixels;

        Log.d(TAG, "scanForCode: widthy, heighty: " + widthy + " " + heighty);


        LinearLayout imageView = (LinearLayout) findViewById(R.id.image);
        final View bar = findViewById(R.id.bar);
        final Animation animation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.anim);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                bar.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });

        bar.setVisibility(View.VISIBLE);
        bar.startAnimation(animation);


        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale((Activity)
                    this, Manifest.permission.CAMERA)) {


            } else {
                ActivityCompat.requestPermissions((Activity) this,
                        new String[]{Manifest.permission.CAMERA},
                        11);
            }
        }


        sv.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                    Log.d(TAG, "surfaceCreated: in check");
                    return;
                }
                try{
                    mCameraSource.start(holder);
                }catch(IOException e){
                    Log.d(TAG, "surfaceCreated: error");
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                Log.d(TAG, "surfaceChanged: ");

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                Log.d(TAG, "surfaceDestroyed: ");
                mCameraSource.stop();

            }
        });

        mBarcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {

            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {

                //Log.d(TAG, "receiveDetections: detected");
                final SparseArray<Barcode> qrCodes = detections.getDetectedItems();

                if(qrCodes.size() == 1){


                    mTextView.post(new Runnable() {
                        @Override
                        public void run() {
                            Vibrator vibrator = (Vibrator)getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
                            vibrator.vibrate(1000);
                            //mTextView.setText(qrCodes.valueAt(0).displayValue);
                            result = qrCodes.valueAt(0).displayValue;
                            setString(result);
                        }
                    });

                }
            }
        });
    }
}
