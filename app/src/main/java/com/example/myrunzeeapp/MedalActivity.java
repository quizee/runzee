package com.example.myrunzeeapp;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MedalActivity extends MenuActivity {

    private static final String TAG = "MedalActivity";
    ArrayList<PictureData> medal_list = new ArrayList<>();
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    PictureAdapter adapter;
    ImageView my_level;
    ProgressBar progressBar;
    TextView mylevel_text;
    TextView mynextlevel_text;

    PictureData white_belt = new PictureData("white",00.00, R.drawable.white_belt);
    PictureData yellow_belt = new PictureData("yellow",50.00, R.drawable.yellow_belt);
    PictureData blue_belt = new PictureData("blue",250.00, R.drawable.blue_belt);
    PictureData purple_belt = new PictureData("purple",500.00, R.drawable.purple_belt);
    PictureData black_belt = new PictureData("black",2500.00, R.drawable.black_belt);
    PictureData red_belt = new PictureData("red",10000.00, R.drawable.red_belt);

    FirebaseAuth auth;
    FirebaseDatabase database;
    int count = 0 ;
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
        progressBar = findViewById(R.id.progressBar);
        progressBar.setProgress(50);
        progressBar.setMax(100);
        progressBar.setIndeterminate(true);
        mylevel_text = findViewById(R.id.mylevel_text);
        mynextlevel_text = findViewById(R.id.mylevel_text);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        SharedPreferences runListPref;
        int pic_id = 0;
        Float total_distance;
        double remain_distance = -1;
//        if(LoginActivity.my_info != null) {
//            runListPref = getSharedPreferences(LoginActivity.my_info.get("email"), Activity.MODE_PRIVATE);
//        }else{
//            runListPref = getSharedPreferences(getSharedPreferences("auto",Activity.MODE_PRIVATE).getString("auto_email",""),Activity.MODE_PRIVATE);
//        }

        runListPref = getSharedPreferences(auth.getCurrentUser().getEmail(),Activity.MODE_PRIVATE);

        if(runListPref != null) {
            total_distance = runListPref.getFloat("total_distance", -1);
            if (total_distance != -1) {
                if (total_distance < 50) {
                    pic_id = R.drawable.white_belt; //화이트 벨트까지
                    remain_distance = 50 - total_distance;
                    mylevel_text.setText("현재 내 레벨 -  화이트벨트");
                } else if (total_distance >= 50 && total_distance < 250) {
                    pic_id = R.drawable.yellow_belt;
                    remain_distance = 250 - total_distance;
                    mylevel_text.setText("현재 내 레벨 -  옐로우벨트");
                } else if (total_distance >= 250 && total_distance < 500) {
                    pic_id = R.drawable.blue_belt;
                    remain_distance = 500 - total_distance;
                    mylevel_text.setText("현재 내 레벨 -  블루벨트");
                } else if (total_distance >= 500 && total_distance < 2500) {
                    pic_id = R.drawable.purple_belt;
                    remain_distance = 2500 - total_distance;
                    mylevel_text.setText("현재 내 레벨 -  퍼플벨트");
                } else if (total_distance >= 2500 && total_distance < 10000) {
                    pic_id = R.drawable.black_belt;
                    remain_distance = 10000 - total_distance;
                    mylevel_text.setText("현재 내 레벨 -  블랙벨트");
                } else if (total_distance >= 10000) {
                    pic_id = R.drawable.red_belt;
                    mylevel_text.setText("현재 내 레벨 -  레드벨트");
                }
            }
        }
        my_level.setImageResource(pic_id);
        if(remain_distance > -1) {
            mynextlevel_text.setText("다음 레벨까지 남은 거리" + " - " + String.format("%.2f",remain_distance )+ " km");
        }else{
            mynextlevel_text.setText("최고 레벨");
        }

        adapter.setOnItemClickListener(new PictureItemClickListener() {
            @Override
            public void OnItemClick(int postion, final PictureData pictureData) {
                //해당 메달을 보유한 친구들 목록이 보임
                //Toast.makeText(getApplicationContext(),pictureData.what_level+"인 친구들 목록이 보일 예정입니다",Toast.LENGTH_LONG).show();
                final ArrayList<String> belt_friends = new ArrayList<>();
                Log.e(TAG, "OnItemClick: "+pictureData.what_level+"클릭!!" );

                database.getReference("userlist").child(auth.getCurrentUser().getUid()).child("friendList").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                        for(final DataSnapshot snapshot : dataSnapshot.getChildren()){
                            Log.e(TAG, "onDataChange: 친구 한명"+snapshot);
                            //snapshot 하나당 내 친구 한명
                            //그 친구의 벨트를 뽑고 해당하는 벨트가 맞으면 추가한다.
                            database.getReference("runninglist").child(snapshot.getValue(String.class)).child("belt").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot2) {
                                    //datasnapshot2 당 벨트 하나
                                    count ++;
                                    Log.e(TAG, "onDataChange: "+snapshot.getValue(String.class)+"에 대한 벨트 뽑는 중/ 카운트"+count);
                                    Log.e(TAG, "onDataChange: 차일드 갯수"+dataSnapshot.getChildrenCount());
                                    String belt = dataSnapshot2.getValue(String.class);
                                    if(pictureData.what_level.equals(belt)){//내가 찾는 벨트가 맞다면
                                        belt_friends.add(snapshot.getValue(String.class));
                                        Log.e(TAG, "onDataChange: 그중 내가 찾는 벨트인"+pictureData.what_level+"인 친구들이 쌓이고 있음"+belt_friends);
                                    }
                                    if(count == dataSnapshot.getChildrenCount()){
                                        count = 0;
                                        Intent intent = new Intent(MedalActivity.this, BeltFriendActivity.class);
                                        Log.e(TAG, "onDataChange: "+belt_friends);
                                        intent.putExtra("belf_friends",belt_friends);
                                        intent.putExtra("belt_color",pictureData.what_level);
                                        startActivityForResult(intent, 25);
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


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
