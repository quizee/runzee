package com.example.myrunzeeapp;

import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresPermission;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

public class MenuActivity extends AppCompatActivity {

    //하단에 툴바처럼 있는 이미지 뷰들
    ImageView account_lt;
    ImageView record_lt;
    ImageView startrun_lt;
    ImageView group_lt;
    ImageView notification_lt;
    long pressedTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
    }

    //상단의 tablayout에 대한 리스너
    public void setTabLayout(int index){
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs) ;

        //몇번을 입력했는지 intent로 받는다
       // Intent intent = getIntent();
        //int index = intent.getIntExtra("selected_tab",0);
        TabLayout.Tab tab = tabLayout.getTabAt(index);
        tab.select();

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // TODO : process tab selection event.
                int pos = tab.getPosition() ;
                changeActivity(pos) ;//몇번을 누르냐에 따라 뷰를 바꿀 수 있게끔
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // do nothing
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // do nothing
            }
        }) ;
    }
    //하단의 toolbar에 대한 리스너
    public void setToolbarMenu(){
        account_lt = (ImageView) findViewById(R.id.account_lt);
        record_lt = (ImageView) findViewById(R.id.record_lt);
        startrun_lt = (ImageView) findViewById(R.id.startrun_lt);
        group_lt = (ImageView) findViewById(R.id.group_lt);
        notification_lt = (ImageView) findViewById(R.id.notification_lt);

        account_lt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),ProfileActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);

            }
        });

        record_lt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                if(RecordActivity.record_index == 0){
                    intent = new Intent(getApplicationContext(),RecordActivity.class);
                }else if(RecordActivity.record_index == 1){
                    intent = new Intent(getApplicationContext(),MedalActivity.class);
                }
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
            }
        });

        startrun_lt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                if(ReadyActivity.ready_index == 0) {
                    intent = new Intent(getApplicationContext(), ReadyActivity.class);
                }else if(ReadyActivity.ready_index == 1){
                    intent = new Intent(getApplicationContext(),ReadyPlanActivity.class);
                }
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
            }
        });

        group_lt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                if(ClubActivity.club_index == 0){
                    intent = new Intent(getApplicationContext(),ClubActivity.class);
                }else if(ClubActivity.club_index == 1){
                    intent = new Intent(getApplicationContext(),MyChallengeActivity.class);
                }
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
            }
        });
        notification_lt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),NotifActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);

            }
        });

    }

    public void changeActivity(int index) {}//상속받는 액티비티마다 구현을 따로 해줄 것.

    @Override
    public void onBackPressed() {
        if ( pressedTime == 0 ) {
            Toast.makeText(this, " 한 번 더 누르면 종료됩니다." , Toast.LENGTH_LONG).show();
            pressedTime = System.currentTimeMillis();
        }
        else {
            int seconds = (int) (System.currentTimeMillis() - pressedTime);
            if ( seconds > 2000 ) {
                Toast.makeText(this, " 한 번 더 누르면 종료됩니다." , Toast.LENGTH_LONG).show();
                pressedTime = 0 ;
            }
            else {
                //super.onBackPressed();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    finishAffinity();
                }
                System.runFinalizersOnExit(true);
                System.exit(0);
            }
        }
    }
}
