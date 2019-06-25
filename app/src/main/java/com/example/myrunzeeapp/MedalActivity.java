package com.example.myrunzeeapp;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;

public class MedalActivity extends MenuActivity {

    ArrayList<PictureData> medal_list = new ArrayList<>();
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    PictureAdapter adapter;
    ImageView my_level;

    PictureData white_belt = new PictureData("화이트 벨트",00.00, R.drawable.white_belt);
    PictureData yellow_belt = new PictureData("옐로우 벨트",50.00, R.drawable.yellow_belt);
    PictureData blue_belt = new PictureData("블루 벨트",250.00, R.drawable.blue_belt);
    PictureData purple_belt = new PictureData("퍼플 벨트",500.00, R.drawable.purple_belt);
    PictureData black_belt = new PictureData("블랙 벨트",2500.00, R.drawable.black_belt);
    PictureData red_belt = new PictureData("레드 벨트",10000.00, R.drawable.red_belt);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medal);
        medal_list.add(white_belt);
        medal_list.add(yellow_belt);
        medal_list.add(blue_belt);
        medal_list.add(purple_belt);
        medal_list.add(black_belt);
        medal_list.add(red_belt);

        recyclerView = findViewById(R.id.record_recycler);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);// set layout manager for recyclerview
        adapter = new PictureAdapter(medal_list,MedalActivity.this);
        recyclerView.setAdapter(adapter);//set adapter for recyclerview
        my_level = findViewById(R.id.my_level);

        SharedPreferences runListPref;
        int pic_id = 0;
        if(LoginActivity.my_info != null) {
            runListPref = getSharedPreferences(LoginActivity.my_info.get("email"), Activity.MODE_PRIVATE);
        }else{
            runListPref = getSharedPreferences(getSharedPreferences("auto",Activity.MODE_PRIVATE).getString("auto_email",""),Activity.MODE_PRIVATE);
        }
        if(runListPref != null) {
            Float total_distance = runListPref.getFloat("total_distance", -1);
            if (total_distance != -1) {
                if (total_distance < 50) {
                    pic_id = R.drawable.white_belt; //화이트 벨트까지
                } else if (total_distance >= 50 && total_distance < 250) {
                    pic_id = R.drawable.yellow_belt;
                } else if (total_distance >= 250 && total_distance < 500) {
                    pic_id = R.drawable.blue_belt;
                } else if (total_distance >= 500 && total_distance < 2500) {
                    pic_id = R.drawable.purple_belt;
                } else if (total_distance >= 2500 && total_distance < 10000) {
                    pic_id = R.drawable.black_belt;
                } else if (total_distance >= 10000) {
                    pic_id = R.drawable.red_belt;
                }
            }
        }
        my_level.setImageResource(pic_id);

        adapter.setOnItemClickListener(new PictureItemClickListener() {
            @Override
            public void OnItemClick(int postion, PictureData pictureData) {
                //해당 메달을 보유한 친구들 목록이 보임
                Toast.makeText(getApplicationContext(),pictureData.what_level+"인 친구들 목록이 보일 예정입니다",Toast.LENGTH_LONG).show();
            }
        });
        setToolbarMenu();
        setTabLayout(1);
        record_lt.setImageResource(R.drawable.ic_storage_black_24dp);
    }
    public void changeActivity(int index) {
        switch(index) {
            case 0:
                Intent intent = new Intent(this, RecordActivity.class);
                intent.putExtra("selected_tab",index);
                startActivity(intent);
                RecordActivity.record_index = 0;
                break;
            default:
                break;
        }
    }
}
