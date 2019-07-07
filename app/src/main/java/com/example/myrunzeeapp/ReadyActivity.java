package com.example.myrunzeeapp;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


public class ReadyActivity extends MenuActivity implements OnMapReadyCallback {

    //위치 받기 위한 변수들
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Location location;
    LatLng latLng ;
    //구글맵
    private GoogleMap mMap;
    private PolylineOptions polylineOptions;
    private static String TAG = "ReadyActivity";
    static int ready_index;
    //ReadyActivity 만의 멤버 변수
    TextView goal;
    Button start_btn;
    Button edit_time;

    //피드 작동시키기
    int event_count = 0;
    boolean restarted = true;
    static RunningItem runningItem;

    //디폴트 목표시간
    int averageTime;
    float averageDistance;

    private static final String KEY_TIMESET = "timeset_key";

    //핸들러 구현 후 지울 것
    Intent serviceIntent;

    //이메일 키
    String emailKey;

    //firebase
    FirebaseDatabase database;
    FirebaseAuth auth;

    public class ReadyLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            double currentLat = location.getLatitude();
            double currentLon = location.getLongitude();//현재 위치를 받아온다.
            latLng = new LatLng(currentLat, currentLon);
        }
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    }

    public void registerLocationUpdates(){
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new ReadyLocationListener();//리스너를 만들어놔야 requestLocationUpdate을 할 수 있음
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if(location == null){
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
        if(location == null){
            latLng = new LatLng(37.4822314, 127.0368105);//임시로 지정
        }else {
            latLng = new LatLng(location.getLatitude(), location.getLongitude());
            Log.e("GPSACtivity", "registerLocationUpdates: lat-> " + location.getLatitude() + " lon-> " + location.getLongitude());
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 0, locationListener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0, locationListener);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        final MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("현재 위치");
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(18));
        mMap.addMarker(markerOptions);
    }

    //시간 설정 다이어로그
    public void timeShow(){
        final Dialog d = new Dialog(ReadyActivity.this);
        d.setTitle("목표 시간");
        d.setContentView(R.layout.ready_time_dialog);

        final NumberPicker minute = (NumberPicker)d.findViewById(R.id.ready_minute);
        Button decide = (Button) d.findViewById(R.id.decide_time_ready);

        minute.setMinValue(1);
        minute.setMaxValue(500);

        decide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String updateKm="";
                if(minute.getValue()<10){
                    updateKm = "00:0"+minute.getValue();
                }else{
                    if(minute.getValue()>60) {//시간으로 넘어감
                        int minLeft = minute.getValue() % 60;
                        if (minLeft < 10) {
                            updateKm = "0" + minute.getValue() / 60 + ":" + "0" + minLeft;
                        } else {
                            updateKm = "0" + minute.getValue() / 60 + ":" + minLeft;
                        }
                    }else{//시간으로 넘어가진 않음
                        updateKm ="00:"+minute.getValue();
                    }
                }
                goal.setText(updateKm);
                d.dismiss();
            }
        });
        d.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ready);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_ready);
        mapFragment.getMapAsync(this);

        //runningItem = new RunningItem();
        //ReadyActivity만의 멤버변수
        edit_time = (Button) findViewById(R.id.edit_time);
        goal = (TextView) findViewById(R.id.goal);
        start_btn = (Button) findViewById(R.id.start_btn);
        emailKey = getIntent().getStringExtra("email");

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        //권한 설정
        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = {android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //어플리케이션을 처음 실행했을 시에, 권한 확인.
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                //위치 권한 동의 요청.
                ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
            }
        }

        SharedPreferences runListPref;
        //평소 평균 목표 시간을 디폴트로 설정
//        if(LoginActivity.my_info != null) {
//            runListPref = getSharedPreferences(LoginActivity.my_info.get("email"), Activity.MODE_PRIVATE);
//        }else{
//            SharedPreferences auto = getSharedPreferences("auto",Activity.MODE_PRIVATE);
//            runListPref = getSharedPreferences(auto.getString("auto_email",""),Activity.MODE_PRIVATE);
//        }
        runListPref = getSharedPreferences(auth.getCurrentUser().getEmail(),Activity.MODE_PRIVATE);
        if (runListPref != null) {
            averageTime = runListPref.getInt("average_time",0);
            averageDistance = runListPref.getFloat("average_distance",0.0f);
        }

        String goal_string = "";//나머지는 중요하지 않아
        if(averageTime/3600>0){//시간단위로 넘어가면
            if((averageTime%3600)/60<10) {//분이 10보다 작으면
                goal_string = "0" + averageTime / 3600 + ":0" + (averageTime % 3600) / 60;
            }else{
                goal_string = "0" + averageTime / 3600 + ":" + (averageTime % 3600) / 60;
            }
        }else{//시간 단위로 넘어가지 않으면
            if(averageTime/60<10){
                goal_string = "00:0"+averageTime/60;
            }else{
                goal_string = "00:"+averageTime/60;
            }
        }
        goal.setText(goal_string);

        edit_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timeShow();
            }
        });

        //ReadyActivity만의 리스너
        start_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //핸들러 배우고 다시 바꾸기
                //Intent intent = new Intent(ReadyActivity.this, CountDownActivity.class);

                //////////////////핸들러하고나서 이부분은 지워도 됩니다(서비스를 시작하는 지점이 여기가 아니므로)//////////////
                serviceIntent = new Intent(getApplicationContext(), MyTimerService.class);
                String words[] = goal.getText().toString().split(":");
                int send_minute = Integer.parseInt(words[0])*60+ Integer.parseInt(words[1]);
                serviceIntent.putExtra("goal_time",send_minute); // 목표시간을 전달한다.
                startService(serviceIntent);//타이머 서비스를 시작시킨다.
                //////////////////////////////////////////////////////////////////////////////////////////////////////////////

                Intent intent = new Intent(ReadyActivity.this, TimerActivity.class);
                //intent.putExtra("goal_time",time_edit.getText().toString().trim());//countdown activity에서 다시 전달해야할 듯

                startActivity(intent);
            }
        });


        //기본 하단 상단 화면 세팅
         setToolbarMenu();
         setTabLayout(0);
         startrun_lt.setImageResource(R.drawable.ic_directions_run_black_24dp);

         //토큰 값 얻기
         FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.e(TAG, "getInstanceId failed", task.getException());
                            return;
                        }
                        // Get new Instance ID token
                        String token = task.getResult().getToken();
                        database.getReference("userlist").child(auth.getCurrentUser().getUid()).child("fcmToken").setValue(token);
                    }
                });
    }

    //tab 눌렀을 때 뷰를 어떻게 바꿀지에 대한 오버라이딩
//    public void changeActivity(int index) {
//        switch(index) {
//            case 1:
//                Intent intent = new Intent(ReadyActivity.this, ReadyPlanActivity.class);
//               // intent.putExtra("selected_tab",index);
//                startActivity(intent);
//                ready_index = 1;
//                break;
//            default:
//                break;
//        }
//    }
    @Override
    protected  void onResume(){
        super.onResume();
        Log.e("ReadyActivity", "Resume - 레디 ");
        registerLocationUpdates();
        if(restarted == false){//피드 보이게 하기
            event_count++;
            if(event_count == 1) {
                Intent intent = new Intent(this, EventActivity3.class);
                startActivity(intent);
            }else if(event_count == 2){
                Intent intent = new Intent(this, EventActivity1.class);
                startActivity(intent);
            }else if(event_count == 3){
                Intent intent = new Intent(this, EventActivity2.class);
                startActivity(intent);
            }
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e("ReadyActivity", "onStop-  레디" );
        restarted = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e("ReadyActivity", "onpause - 레디");
        restarted = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("ReadyActivity", "destroy - 레디" );
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        restarted = true;
        Log.e("ReadyActivity", "restart!!!!! - 레디" );
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

}
