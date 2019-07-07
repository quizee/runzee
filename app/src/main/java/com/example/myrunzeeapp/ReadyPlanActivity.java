package com.example.myrunzeeapp;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

public class ReadyPlanActivity extends MenuActivity {

    FirebaseAuth auth;
    TextView perweek;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ready_plan);
        auth = FirebaseAuth.getInstance();

        setToolbarMenu();
        setTabLayout(1);
        startrun_lt.setImageResource(R.drawable.ic_directions_run_black_24dp);
    }

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences runListPref = getSharedPreferences(auth.getCurrentUser().getUid(), Activity.MODE_PRIVATE);
        double totalDistance = runListPref.getFloat("total_distance",0.0f);
        if(totalDistance<50){

            //화이트 벨트
        }else if(totalDistance>=50 && totalDistance<250){

            //옐로우 벨트
        }else if(totalDistance>=250 && totalDistance<500){

            //블루 벨트
        }else if(totalDistance>=500 && totalDistance<2500){

            //퍼플 벨트
        }else if(totalDistance>=2500 && totalDistance<10000){

            //블랙 벨트
        }else if(totalDistance>=10000){

            //레드 벨트
        }

    }

    public void changeActivity(int index) {
        switch(index) {
            case 0:
                Intent intent = new Intent(ReadyPlanActivity.this, ReadyActivity.class);
               // intent.putExtra("selected_tab",index);
                startActivity(intent);
                ReadyActivity.ready_index = 0;
                break;
            default:
                break;
        }
    }
}
