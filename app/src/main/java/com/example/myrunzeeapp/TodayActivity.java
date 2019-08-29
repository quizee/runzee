package com.example.myrunzeeapp;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.Image;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresPermission;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;

import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class TodayActivity extends AppCompatActivity implements OnMapReadyCallback {

    String todayConsult;

    TextView calorieThis;
    TextView timeThis;
    TextView achievementThismuch;
    TextView achievementIs;
    TextView paceThis;
    TextView title;
    TextView consulting;
    TextView todayDistance;
    TextView consultingIs;

    //사진 관련된 부분 다시 imageview 로 바꿈
    ImageView certification;
    String filename;

    //구글맵
    private GoogleMap mMap;
    private PolylineOptions polylineOptions;

    private static String TAG = "TodayActivity";

    //구글 맵 움직이게 하기
    Handler mapHandler = new Handler();
    int pointCount = 0;

    RunningItem runningItem = ReadyActivity.runningItem;
    ArrayList<LatLng> trakerPoints = runningItem.getTrakerPoints(); //움직인 좌표들

    public double returnLatFromIndex(int index){
        return trakerPoints.get(index).latitude;
    }

    public double returnLonFromIndex(int index){
        return trakerPoints.get(index).longitude;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        final MarkerOptions markerOptions = new MarkerOptions();//출발을 위한 마커
        final MarkerOptions markerOptions2 = new MarkerOptions();//도착을 위한 마커

        int startIndex = 0;
        double startLat = returnLatFromIndex(startIndex);//출발지
        double startLon = returnLonFromIndex(startIndex);
        LatLng From = new LatLng(startLat,startLon);
        markerOptions.position(From);//출발 marker 설정
        markerOptions.title("출발");

        int mediumIndex = trakerPoints.size()/2;//중간지점
        LatLng Camera = new LatLng(returnLatFromIndex(mediumIndex),returnLonFromIndex(mediumIndex));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(Camera)); //카메라는 출발과 도착 중간지점에 설정
        mMap.animateCamera(CameraUpdateFactory.zoomTo(17));

        int lastIndex = trakerPoints.size() -1;//도착지
        double LastLat = returnLatFromIndex(lastIndex);
        double LastLon = returnLonFromIndex(lastIndex);
        LatLng To = new LatLng(LastLat,LastLon);
        markerOptions2.position(To);
        markerOptions2.title("도착");

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {//맵 클릭했을 때 출발지 도착지 보이게 만들고 거리 알게
            @Override
            public void onMapClick(LatLng latLng) {
                mMap.addMarker(markerOptions);//출발지
                mMap.addMarker(markerOptions2);//도착지
            }
        });
    }

    public void drawPolyline(LatLng start, LatLng end){//두 점을 잇는 폴리라인을 넣는다.
        polylineOptions = new PolylineOptions();
        polylineOptions.color(Color.CYAN);
        polylineOptions.width(10);
        polylineOptions.endCap(new RoundCap());//그냥 설정
        polylineOptions.add(start);
        polylineOptions.add(end);
        mMap.addPolyline(polylineOptions);
    }


    private class DrawPoly implements Runnable{
        @Override
        public void run() {
            if(runningItem != null && trakerPoints != null) {
                while (pointCount < trakerPoints.size() - 2) {
                    mapHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            LatLng start = trakerPoints.get(pointCount);
                            LatLng end = trakerPoints.get(pointCount + 1);
                            drawPolyline(start, end);
                        }
                    });
                    pointCount++;
                    try {
                        Thread.sleep(40);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK && requestCode==2){
            Log.e(TAG, "onActivityResult: 수정된 아이템을 받는다");
            runningItem.setModified(true);//변경되었음
        }
    }

    //툴바 관련된 사항들
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_directrun_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.delete:
                Intent intent = new Intent(TodayActivity.this,ReallyDeleteActivity.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_today);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_today);
        mapFragment.getMapAsync(this);

        ViewGroup.LayoutParams params = mapFragment.getView().getLayoutParams();
        params.height = 600;
        mapFragment.getView().setLayoutParams(params);

        //bottom navigation bar
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.go_takephoto:
                        Intent intent = new Intent(TodayActivity.this, TakePhotoActivity.class);
                        intent.putExtra("howmuchTIme",ReadyActivity.runningItem.getRuntime_seconds());
                        startActivity(intent);//takePhotoActivity로 넘어가서 사진 촬영
                        break;
                    case R.id.go_record:
                        Intent intent1 = new Intent(TodayActivity.this,RecordActivity.class);

                        if(RecordActivity.justWatching&&!runningItem.getModified()){
                            //삭제를 시킬 때는 이 버튼을 누를 일이 없다.단 수정을 할 때는 이 버튼을 누르기 때문에 예외처리
                            runningItem = null;
                        }
                        intent1.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        startActivity(intent1);
                        finish();
                        break;

                }
                return true;
            }
        });

        paceThis = findViewById(R.id.paceThis);
        calorieThis = findViewById(R.id.calorieThis);
        timeThis = findViewById(R.id.timeThis);
        achievementThismuch = findViewById(R.id.achievementThismuch);
        achievementIs = findViewById(R.id.achievementIs);
        title = findViewById(R.id.title);
        consulting = findViewById(R.id.consulting);
        certification = findViewById(R.id.certification);
        todayDistance = findViewById(R.id.todayDistance);
        consultingIs = findViewById(R.id.consultingIs);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(runningItem.getDate());
        setSupportActionBar(toolbar);
        certification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pictureShow();
            }
        });

        //맵 좌표 움직이기
        Thread mapThread = new Thread(new DrawPoly());
        mapThread.start();

    }

    //사진 확대해서 보기 다이어로그
    public void pictureShow(){
        final Dialog d = new Dialog(TodayActivity.this);
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
        d.setContentView(R.layout.one_picture_dialog);
        final ImageView onePicture = d.findViewById(R.id.onePicture);
        final ImageView cancel = d.findViewById(R.id.goout);
        final Button editPicture = d.findViewById(R.id.editPicture);

        Bitmap bm = BitmapFactory.decodeFile(filename);
        if(filename!=null) {
            Glide.with(onePicture.getContext()).load(bm).into(onePicture);
        }

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.dismiss();
            }
        });

        editPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TodayActivity.this, TakePhotoActivity.class);
                intent.putExtra("howmuchTIme",runningItem.getRuntime_seconds());
                intent.putExtra("editing_picture",true);
                startActivity(intent);//takePhotoActivity로 넘어가서 사진 촬영
                d.dismiss();
            }
        });

        d.show();
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }
    public void writeConsulting(int totalRest, int achievement){
        if(totalRest == 0){
            todayConsult = "가벼운 러닝";
        }else if(totalRest == 1){
            todayConsult = "회복 러닝";
        }else if(totalRest == 2){
            todayConsult = "스피드 러닝";
        }else{
            todayConsult = "하드 러닝";
        }
        if(totalRest == 100){
            consultingIs.setText("");
            consulting.setText("");//저장된 값이 없다고 간주되면 빈칸으로 나둔다
        }else{
            consulting.setText(todayConsult+" (총 휴식 횟수 "+totalRest+"회)");
        }

        if(achievement>0){
            achievementThismuch.setText(String.valueOf(achievement)+"%");
        }else{
            achievementThismuch.setText("");
            achievementIs.setText("");//빈칸으로 나둔다.
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        //화면 초기화를 위한 변수들
        double distance = runningItem.getKm();
        int totalRest = runningItem.getRest_count();
        int timeR = runningItem.getRuntime_seconds();
        int achievement = runningItem.getAchievement();
        int calorie = runningItem.getCalorie();
        String default_title = runningItem.getTitle();
        int TimeperKm= runningItem.getPace_seconds(); //키로미터당 몇초 걸리는지 나옴
        String paceString = TimeperKm/60+"\'"+TimeperKm%60+"\'\'";

        filename = runningItem.getFilename();
        Bitmap bm = BitmapFactory.decodeFile(filename);

        if(filename!=null) {
            Glide.with(certification.getContext()).load(bm).into(certification);
        }

        writeConsulting(totalRest,achievement);

        String timeSet = timeR/60+"분 "+timeR%60+"초";
        timeThis.setText(timeSet);
        calorieThis.setText(String.valueOf(calorie));
        paceThis.setText(paceString);
        title.setText("제목: "+default_title);
        todayDistance.setText(String.format("%.2f",distance)+" km");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e(TAG, "onPause: 아이템이 "+runningItem );
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

}
