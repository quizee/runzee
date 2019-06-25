package com.example.myrunzeeapp;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

public class CameraPreviewActivity extends AppCompatActivity {

    private static String TAG = "CameraPreviewActivity";
    private int PERMISSIONS_REQUEST_CODE = 100;
    private String REQUIRED_PERMISSION_CAMERA = Manifest.permission.CAMERA;
    private String REQUIRED_PERMISSION_WES = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    private String REQUIRED_PERMISSIONS[] = {REQUIRED_PERMISSION_CAMERA,REQUIRED_PERMISSION_WES};
    private int CAMERA_FACING = Camera.CameraInfo.CAMERA_FACING_BACK;

    FrameLayout cameraPreview;
    TextView howmuchTIme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_preview);

        cameraPreview = findViewById(R.id.cameraPreview);
        howmuchTIme = findViewById(R.id.howmuchTime);
        Intent intent = getIntent();
        int run_seconds = intent.getIntExtra("howmuchTime",0);
        howmuchTIme.setText(run_seconds/60+"분 "+run_seconds%60+"초");

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // OS가 Marshmallow 이상일 경우 권한체크

            int permissionCheckCamera
                    = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
            int permissionCheckStorage
                    = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

            if (permissionCheckCamera == PackageManager.PERMISSION_GRANTED
                    && permissionCheckStorage == PackageManager.PERMISSION_GRANTED) {

                Log.d(TAG, "권한 이미 있음");
                startCamera();


            } else {
                // 권한 없음
                Log.d(TAG, "권한 없음");
                ActivityCompat.requestPermissions(this,
                        REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }
        } else {
            // OS가 Marshmallow 이전일 경우 권한체크를 하지 않는다.
            Log.d("MyTag", "마시멜로 버전 이하로 권한 이미 있음");
            startCamera();
        }
    }
    public void startCamera(){
        Log.e(TAG, "startCamera");
       MyCameraPreview myCameraPreview = new MyCameraPreview(this, CAMERA_FACING);
        cameraPreview.addView(myCameraPreview);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // grantResults[0] 거부 -> -1
        // grantResults[0] 허용 -> 0 (PackageManager.PERMISSION_GRANTED)
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == PERMISSIONS_REQUEST_CODE) {
            boolean check_result = true;

            for(int i = 0; i<grantResults.length;i++){
                if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                    check_result = false;
                    break;
                }
            }

            if(check_result) {
                startCamera();
            } else {
                Log.e(TAG, "권한 거부");
            }
        }
    }


}
