package com.example.myrunzeeapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class EventActivity extends AppCompatActivity {


    ImageView account_lt;
    ImageView record_lt;
    ImageView startrun_lt;
    ImageView group_lt;
    ImageView notification_lt;

    Button event1;
    Button event2;
    Button event3;
    Button event4;

    boolean restarted = true;

    public void setEventMenu(){
        event1 = (Button) findViewById(R.id.event1);
        event2 = (Button) findViewById(R.id.event2);
        event3 = (Button) findViewById(R.id.event3);
        event4 = (Button) findViewById(R.id.event4);

        event1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),EventActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                finish();
            }
        });
        event2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), EventActivity1.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                finish();
            }
        });
        event3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), EventActivity2.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                finish();
            }
        });
        event4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), EventActivity3.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                finish();
            }
        });
    }

    public void setToolbarMenu(){
        account_lt = (ImageView) findViewById(R.id.account_lt);
        record_lt = (ImageView) findViewById(R.id.record_lt);
        startrun_lt = (ImageView) findViewById(R.id.startrun_lt);
        group_lt = (ImageView) findViewById(R.id.group_lt);
        notification_lt = (ImageView) findViewById(R.id.notification_lt);

        notification_lt.setImageResource(R.drawable.ic_notifications_black_24dp);

        account_lt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),ProfileActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

                startActivity(intent);
                finish();
            }
        });

        record_lt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),RecordActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

                startActivity(intent);
                finish();
            }
        });

        startrun_lt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),ReadyActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

                startActivity(intent);
                finish();
            }
        });

        group_lt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),ClubActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

                startActivity(intent);
                finish();
            }
        });
        notification_lt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),NotifActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

                startActivity(intent);
                finish();
            }
        });

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        setToolbarMenu();
        setEventMenu();
        Log.e("이벤트", "onCreate: ");

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.e("event Activity", "onStart: 이벤트");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e("event Activity", "onPause: 이벤트");
        restarted = false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e("event Activity", "onStop: 이벤트");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("event Activity", "onDestroy: 이벤트 ");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        restarted = true;
        Log.e("event Activity", "onRestart!!!!!!: 이벤트 ");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("이벤트 액티비티", "onResume 막 시작 "+restarted);
        if(restarted == false) {
            event1.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_thumb, 0, 0, 0);
        }
        Log.e("이벤트 액티비티", "onResume 처리 후 "+restarted);
    }

}
