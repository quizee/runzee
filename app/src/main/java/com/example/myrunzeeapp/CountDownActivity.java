package com.example.myrunzeeapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class CountDownActivity extends AppCompatActivity {

    TextView countdown;
    int fromcount = 3;

    //서비스 시작시키기(단, 3초후에)
    Intent serviceIntent;
    Intent intent;

    //타이머 표시를 위한 핸들러
    @SuppressLint("HandlerLeak")
    Handler mHandler = new Handler(){
        public void handleMessage(Message msg){
            Log.e("핸들러", "handleMessage: " );
            fromcount--;
            countdown.setText(String.valueOf(fromcount));
            mHandler.sendEmptyMessageDelayed(0,1000);

            if (fromcount ==1){
                intent = new Intent(CountDownActivity.this,TimerActivity.class);
                startActivity(intent);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_count_down);
        countdown = findViewById(R.id.countdown);
        mHandler.sendEmptyMessage(0); //핸들러에 메세지 전달?

        //다음 액티비티로
        Log.e("countDownActivity", "timerActivity 보내기 직전 ");

        //서비스 시작
        serviceIntent = new Intent(getApplicationContext(),MyTimerService.class);
        startService(serviceIntent);//타이머 서비스를 시작시킨다.
    }
}
