package com.example.myrunzeeapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Calendar;
import static com.example.myrunzeeapp.TimerActivity.Timer_Activity;

import java.text.SimpleDateFormat;
import java.util.Date;

public class PauseActivity extends Activity implements View.OnClickListener {

    private Button stop_running;
    TimerActivity TA;
    Intent serviceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_pause);
        TA = (TimerActivity) TimerActivity.Timer_Activity;

        // 정지 시켰을 경우 서비스도 정지되어야 하므로
        serviceIntent = new Intent(getApplicationContext(), MyTimerService.class);
        setContent();

    }
    private void setContent() {
        stop_running = (Button) findViewById(R.id.stop_running);
        stop_running.setOnClickListener(this);
    }

    public String setDefaultTitle(){

        Calendar cal = Calendar.getInstance();

        int hour = cal.get(Calendar.HOUR_OF_DAY);

        String dayOrNight ="";
        String week = "";

        if(hour>12){
            dayOrNight ="오후";
        }else{
            dayOrNight = "오전";
        }

        int day_of_week = cal.get(Calendar.DAY_OF_WEEK);

        switch (day_of_week){
            case 1:
                week = "일";
                break;
            case 2:
                week = "월";
                break;
            case 3:
                week = "화";
                break;
            case 4:
                week = "수";
                break;
            case 5:
                week = "목";
                break;
            case 6:
                week = "금";
                break;
            case 7:
                week = "토";
                break;

        }

        return week+"요일 "+dayOrNight +" 러닝";
    }
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.stop_running:
                Toast.makeText(this, "러닝을 종료합니다.", Toast.LENGTH_SHORT).show();
                Intent intent = getIntent();//timeractivity 에서 휴식 횟수를 받음
                //거리 관련 정보는 intent에서 받고
                int rest_count = intent.getIntExtra("rest_num",0);
                double distance = intent.getDoubleExtra("distance",0.0);
                ArrayList<LatLng> trackpoints = intent.getParcelableArrayListExtra("trackpoints");
                Intent intent1 = new Intent(this,TodayActivity.class);//부가 데이터를 더 넣지는 않고 바로 runningitem을 초기화시킨다.
                //시간 관련 정보는 서비스에서 받는다.
                int count_second = TA.myTimerService.getCount_time();
                int count_left = TA.myTimerService.getInt_Goal();
                int achievement = (int)(((double)count_second/(double)(count_left+count_second))*100);

                long now = System.currentTimeMillis();
                Date date = new Date(now);
                SimpleDateFormat sdf_date = new SimpleDateFormat("yyyy.MM.dd");
                String getDate = sdf_date.format(date);
                String defaultTitle = setDefaultTitle();

                //날짜 제목 거리 뛴 시간(초) 휴식횟수 달성률 담았음
                ReadyActivity.runningItem = new RunningItem(getDate,defaultTitle,distance,count_second,rest_count,achievement);
                ReadyActivity.runningItem.setTrakerPoints(trackpoints);
                ReadyActivity.runningItem.setDirectRunning(true);
                RecordActivity.justWatching = false;
                startActivity(intent1);
                TA.myTimerService.completeTimer();//스레드 죽임
                TA.unBindService();//언바인드
                stopService(serviceIntent);//서비스 죽임
                this.finish();
                TA.finish();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("Pause Activity", "onDestroy: ");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e("Pause Activity", "onStop: ");
    }
}
