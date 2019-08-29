package com.example.myrunzeeapp;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class TimerActivity extends AppCompatActivity {

    public static Activity Timer_Activity;
    private static String TAG = "TimerActivity";

    //핸들러 배우고 없애기
   // Button temp_btn;
    Button stop_btn;
    TextView left_time;
    TextView rest_num;
    TextView timer_text;
    int count_rest = 0;
    //핸들러
    Handler mainHandler;

    //바인딩 준비
    MyTimerService myTimerService;
    ServiceConnection serviceConnection;
    boolean isServiceBound;
    Intent serviceIntent;

    TextView kilometer;
    TextView runningspeed;
    //거리측정 서비스를 받기 위한 변수들
    LocationManager locationManager;
    LocationListener locationListener;
    Location location;
    Context context;
    double distance = 0; // 타이머 액티비티가 생길 때마다 새로 생기게 해서 intent로 넘기는 방향으로 가자
    double total_speed = 0;
    int count_location = 0;
    double average_speed = 0;
    ArrayList <LatLng> trackPoints = new ArrayList<>();//위도 경도 리스트도 마찬가지

    FirebaseDatabase database;
    FirebaseAuth auth;
    HashMap<String, Boolean> goMsg = new HashMap<>();

    //timer
    boolean isRunning = true;

    //좌표가 변할 때마다 해야할 일을 하는 메소드 오버라이딩
    public class MyLocationListener implements LocationListener{

        double currentLat;
        double currentLon;

        double lastLat;
        double lastLon;

        Location lastLocation = new Location("lastLocation");
        boolean isFirstTIme = true;

        @Override
        public void onLocationChanged(Location location) {
            count_location ++;
            currentLat = location.getLatitude();
            currentLon = location.getLongitude();//현재 위치를 받아온다.
            LatLng latLng = new LatLng(currentLat, currentLon);
            trackPoints.add(latLng);//위치를 받아올 때마다 리스트에 저장한다.
            Log.e(TAG, "onLocationChanged: "+"새 좌표 받았습니다~~~~!! lat = "+currentLat+", lon"+ currentLon);

            if(isFirstTIme){
                lastLat = currentLat;
                lastLon = currentLon;
                isFirstTIme = false;
            }
            lastLocation.setLatitude(lastLat);
            lastLocation.setLongitude(lastLon);

            double temp_distance = lastLocation.distanceTo(location);
            distance += temp_distance;
            Log.e(TAG, "onLocationChanged: 지금 거리: "+temp_distance+"m 총 거리 " +distance+"m");//거리를 재고나서는 받아왔던 현재위치가 유물이됨

            lastLat = currentLat;
            lastLon = currentLon;
            kilometer.setText(String.format("%.2f",distance/1000.0)+" km");

            if(location.hasSpeed()){
                total_speed += location.getSpeed()*3.6;
                average_speed = total_speed / (double)count_location;
                runningspeed.setText(Math.round(average_speed*100)/100.0+" km/h");
            }

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
    //1초마다 바인딩해와야해서 필요한 쓰레드
    private class GetCountThread implements Runnable {
       // private Handler handler = new Handler();

        @Override
        public void run() {
            while(isRunning){
                Log.e(TAG, "run: " +"Thread num: ");
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        bindService();
                        setTimeText();
                    }
                });
                try{
                    Thread.sleep(1000);
                }catch(InterruptedException e){
                    e.printStackTrace();
                }
            }
        }
    }
    private void sendPostToFCM(final String my_name) {
        //누구에게 가는 어떤 푸시 메시지이고 누르면 어떤 음성이 나올지

        //내가 참여하는 챌린지에 있는 모든 사람들에게 알림이 가도록
        database.getReference("participate").child(auth.getCurrentUser().getUid()).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                //datasnapshot 하나당 내가 참여하는 챌린지 하나
                database.getReference("challenge").child(dataSnapshot.getKey()).addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull final DataSnapshot dataSnapshot2, @Nullable String s) {
                        //datasnapshot2 하나당 그 챌린지에 참여하는 한 사람.
                        if(!goMsg.containsKey(dataSnapshot2.getKey())){
                            goMsg.put(dataSnapshot2.getKey(),false);//처음엔 false
                        }
                        if(!dataSnapshot2.getKey().equals(auth.getCurrentUser().getUid())&& !goMsg.get(dataSnapshot2.getKey())) {//나는 제외하고
                            database.getReference("userlist")
                                    .child(dataSnapshot2.getKey()).child("fcmToken")
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot3) {
                                            final String token = dataSnapshot3.getValue(String.class);
                                            goMsg.put(dataSnapshot2.getKey(),true);// 이미 한번 등장했다
                                            new Thread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    try {
                                                        // FMC 메시지 생성 start
                                                        JSONObject root = new JSONObject();
                                                        JSONObject data = new JSONObject();
                                                        data.put("receiver_name", my_name);
                                                        data.put("file_name", auth.getCurrentUser().getUid());
                                                        root.put("data", data);
                                                        root.put("to", token);

                                                        // FMC 메시지 생성 end
                                                        URL Url = new URL("https://fcm.googleapis.com/fcm/send");
                                                        HttpURLConnection conn = (HttpURLConnection) Url.openConnection();
                                                        conn.setRequestMethod("POST");
                                                        conn.setDoOutput(true);
                                                        conn.setDoInput(true);
                                                        conn.addRequestProperty("Authorization", "key=" + "AAAAbIReuEk:APA91bELlRqy67058sv4DJs_50_OKajyunrJrJYe5ZQAq7ohNxukL5BWg9eVRy5YMEY7fREM6OgCiidw0Eih18_90Fl4aEksi3w1OmbA5vSKHfaqjEGJYeYUm4P4wvAoy-ic0LT7JTX6");
                                                        conn.setRequestProperty("Accept", "application/json");
                                                        conn.setRequestProperty("Content-type", "application/json");
                                                        OutputStream os = conn.getOutputStream();
                                                        os.write(root.toString().getBytes("utf-8"));
                                                        os.flush();
                                                        conn.getResponseCode();
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            }).start();
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                        }

                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);

        //to finish TimerActivity
        Timer_Activity = TimerActivity.this;

        //find view
        left_time = findViewById(R.id.left_time);
        rest_num = findViewById(R.id.rest_num);
        stop_btn = findViewById(R.id.stop_btn);
        timer_text = findViewById(R.id.timer_text);
        kilometer = findViewById(R.id.kilometer);
        runningspeed = findViewById(R.id.runningspeed);

        //핸들러배우고 없애기
        //temp_btn = findViewById(R.id.temp_btn);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        SharedPreferences runListPref = getSharedPreferences(auth.getCurrentUser().getEmail(), Activity.MODE_PRIVATE);
        String my_name = runListPref.getString("my_name", "");
        Log.e(TAG, "onCreate: "+"내이름은!!!!!!!"+my_name);
        sendPostToFCM(my_name);


        //서비스 바인딩 준비
        serviceIntent = new Intent(getApplicationContext(), MyTimerService.class);
        bindService();

        //거리측정 서비스
        this.context = TimerActivity.this;

        stop_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TimerActivity.this, PauseActivity.class);
                //pause를 했다가 아예 멈출 수도 있으므로 todayactivity에서 필요한 정보들을 다 넘겨야함 (timeractivity에서는 static을 안쓰기 때문에)
                //시간이야 서비스니까 pauseActivity에서 바인딩하면되지만 누적거리는 timerAcitivity의 멤버변수를 쓰고 있으므로 intent로 넘겨줘야 한다.
                intent.putExtra("rest_num",count_rest);
                intent.putExtra("distance",distance/1000.0);//km 단위로 담았음
                intent.putParcelableArrayListExtra("trackpoints",trackPoints);
                startActivity(intent);
                count_rest++;
                myTimerService.stopTimer();//타이머 서비스는 잠깐 멈추지만
                isRunning = false;
                locationManager.removeUpdates(locationListener);//거리 서비스는 아예 버리고 다시

            }
        });

        mainHandler = new Handler();
        Thread thread = new Thread((new GetCountThread()));
        thread.setDaemon(true);
        thread.start();
    }

    public void registerLocationUpdates(){
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new MyLocationListener();//리스너를 만들어놔야 requestLocationUpdate을 할 수 있음
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if(location == null){
            location = new Location("dummyLocation");
            location.setLatitude(37.480857);
            location.setLongitude(126.971745);
        }
        Log.e("GPSACtivity", "registerLocationUpdates: lat-> "+location.getLatitude()+" lon-> "+location.getLongitude());
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
    }

    //서비스와 바인드된다는 것은 serviceConnection을 초기화시킨다는 것이다.
    //serviceConntection을 초기화시킨다는 말은 서비스를 얻어냈다는 것이다.
    private void bindService(){
        if(serviceConnection == null){
            serviceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder iBinder) {
                    //ibinder로부터 서비스를 얻어내서 초기화시키자.
                    MyTimerService.MyTimerServiceBinder myTimerServiceBinder = (MyTimerService.MyTimerServiceBinder) iBinder;
                    myTimerService = myTimerServiceBinder.getService();
                    isServiceBound = true;
                }
                @Override
                public void onServiceDisconnected(ComponentName name) {
                    isServiceBound = false;
                }
            };
        }
        // 바인딩하려는 시점에 서비스가 존재하지 않으면 서비스를 만든다.
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    void unBindService(){
        if(isServiceBound){
            unbindService(serviceConnection);
            isServiceBound = false;
        }
    }


    //서비스로 돌아가고 있는 시간을 화면에 찍는다
    private  void setTimeText(){
        //바인딩 상태일 때만 textview에 적용한다.
        if(isServiceBound){
            int count_second = myTimerService.getCount_time();
            int count_left = myTimerService.getInt_Goal();

            if(count_second<60){
                if(count_second<10){
                    timer_text.setText("00:0" + count_second);
                }else {
                    timer_text.setText("00:" + count_second);
                }
            }else{
                int count_minute = count_second/60;
                int left_second = count_second%60;
                if(count_minute<10){
                    if(left_second<10){
                        timer_text.setText("0"+count_minute+":"+"0"+left_second);
                    }else{
                        timer_text.setText("0"+count_minute+":"+left_second);
                    }
                }else {
                    if(left_second<10){
                        timer_text.setText(count_minute + ":0" + left_second);
                    }else{
                        timer_text.setText(count_minute + ":" + left_second);
                    }
                }
            }
            if(count_left<60){
                if(count_left<10){
                    left_time.setText("00:0" + count_second);
                }else {
                    left_time.setText("00:" + count_second);
                }
            }else{
                int count_minute = count_left/60;
                int left_second = count_left%60;
                if(count_minute<10){
                    if(left_second<10){
                        left_time.setText("0"+count_minute+":"+"0"+left_second);
                    }else{
                        left_time.setText("0"+count_minute+":"+left_second);
                    }
                }else {
                    if(left_second<10){
                        left_time.setText(count_minute + ":0" + left_second);
                    }else{
                        left_time.setText(count_minute + ":" + left_second);
                    }
                }
            }
        }else{
            timer_text.setText("00:00");
            left_time.setText("00:00");
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        Log.e("timer Activity", "onStart: ");
    }

    @Override
    protected void onResume() {
        super.onResume();
        rest_num.setText(count_rest+"회");
        if(myTimerService != null) {
            myTimerService.restartTimer();
            isRunning = true;
        }
        Thread thread = new Thread((new GetCountThread()));
        thread.setDaemon(true);
        thread.start();
        registerLocationUpdates();//lcationManager, locationListener를 할당해준다.
        Log.e("timer Activity", "onResume: 다시 시작합니다");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e("timer Activity", "onPause: 휴식 횟수"+count_rest);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e("timer Activity", "onStop: ");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.e("timer Activity", "onRestart: ");

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("timer Activity", "onDestroy: ");
        //서비스를 종료시킨다.
        unBindService();
        stopService(serviceIntent);
    }
}
