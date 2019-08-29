package com.example.myrunzeeapp;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;

public class RecordActivity extends MenuActivity {

    //탭 전환 변수
    static int record_index;

    //RecordActivity만의 멤버변수
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private static final String TAG = "RecordActivity";
    RecordAdapter adapter;
    ImageView add_btn;

    TextView total_distance;
    TextView total_count_num;
    TextView average_distance_num;
    TextView average_pace_num;
    static  boolean justWatching;
    int averageTime;
    double averageDistance;
    double totalDistance;

    //러너 데이터
    ArrayList<RunningItem> runningList = new ArrayList<RunningItem>();

    //firebase
    FirebaseAuth auth;
    FirebaseDatabase database;

    //추가버튼 눌러서 러닝 추가할 때 사용
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1){
            if(resultCode==RESULT_OK){
                String title = data.getStringExtra("name_edit");
                String date = data.getStringExtra("date_edit");
                final String distance = data.getStringExtra("distance_edit");
                int time = data.getIntExtra("time_edit",0);

                final double run_dist = Double.parseDouble(distance.substring(0,4).trim());
                int run_time = time;
                RunningItem newitem = new RunningItem(date,title,run_dist,run_time);//러닝 객체를 만듬
                database.getReference("participate").child(auth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(final DataSnapshot snapshot : dataSnapshot.getChildren()){
                            //스냅샷 하나당 내가 참여하는 챌린지 하나
                            database.getReference("challenge").child(snapshot.getKey()).child(auth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                //내가 참여하는 챌린지들을 쏙쏙 골라서 내꺼를 불러오고 다시 넣는다.
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    double my_distance = dataSnapshot.getValue(Double.class);
                                    my_distance-= run_dist;
                                    database.getReference("challenge").child(snapshot.getKey()).child(auth.getCurrentUser().getUid()).setValue(my_distance);
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
                runningList.add(0,newitem);
                adapter.notifyDataSetChanged();
                saveRunListPref(auth.getCurrentUser().getEmail());
            }
        }
    }
    //shared preference에 반영하는 메소드
    public void saveRunListPref(String emailKey){
        //각 회원마다 다른 이메일로 쉐어드 파일이 열린다.
        SharedPreferences runListPref = getSharedPreferences(emailKey,Activity.MODE_PRIVATE);
        SharedPreferences.Editor edit = runListPref.edit();
        Gson gson = new Gson();
        String json_runningList = gson.toJson(runningList);//arraylist을 json으로 만든다.
        Log.e(TAG, "saveRunListPref: "+json_runningList);
        edit.putString("runningList",json_runningList);
        edit.apply();
        //쉐어드에 저장 후 파이어베이스에도 올린다.
        database.getReference().child("runninglist").child(auth.getCurrentUser().getUid()).child("runningInfo").setValue(runningList);
    }

    public void saveAverage(String emailKey){
        SharedPreferences runListPref = getSharedPreferences(emailKey,Activity.MODE_PRIVATE);
        SharedPreferences.Editor edit = runListPref.edit();
        edit.putInt("average_time",averageTime);
        edit.putFloat("average_distance",(float)averageDistance);
        edit.putFloat("total_distance", (float)totalDistance);
        edit.apply();
        //누적 거리 업데이트
        database.getReference().child("runninglist").child(auth.getCurrentUser().getUid()).child("accumulate").setValue(totalDistance);
        //벨트 업데이트
        if(totalDistance<50){
            database.getReference().child("runninglist").child(auth.getCurrentUser().getUid()).child("belt").setValue("white");
            //화이트 벨트
        }else if(totalDistance>=50 && totalDistance<250){
            database.getReference().child("runninglist").child(auth.getCurrentUser().getUid()).child("belt").setValue("yellow");
            //옐로우 벨트
        }else if(totalDistance>=250 && totalDistance<500){
            database.getReference().child("runninglist").child(auth.getCurrentUser().getUid()).child("belt").setValue("blue");
            //블루 벨트
        }else if(totalDistance>=500 && totalDistance<2500){
            database.getReference().child("runninglist").child(auth.getCurrentUser().getUid()).child("belt").setValue("purple");
            //퍼플 벨트
        }else if(totalDistance>=2500 && totalDistance<10000){
            database.getReference().child("runninglist").child(auth.getCurrentUser().getUid()).child("belt").setValue("black");
            //블랙 벨트
        }else if(totalDistance>=10000){
            database.getReference().child("runninglist").child(auth.getCurrentUser().getUid()).child("belt").setValue("red");
            //레드 벨트
        }
    }
    public void restoreRunListPref(String emailKey) {
        SharedPreferences runListPref = getSharedPreferences(emailKey, Activity.MODE_PRIVATE);
        Log.e(TAG, "restoreRunListPref: "+runListPref );
        if (runListPref != null) {
            Gson gson = new Gson();
            String json_runningList = runListPref.getString("runningList", "");
            Type type = new TypeToken<ArrayList<RunningItem>>() {
            }.getType();
            Log.e(TAG, "restoreRunListPref: if문 들어왔다." );
            if(gson.fromJson(json_runningList, type)!= null){
                runningList = gson.fromJson(json_runningList, type);
            }
            Log.e(TAG, "restoreRunListPref: "+gson.fromJson(json_runningList, type));
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //초기화
        setContentView(R.layout.activity_record);
        recyclerView = findViewById(R.id.record_recycler);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        add_btn = findViewById(R.id.add_btn);

        //파이어베이스 초기화
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        restoreRunListPref(auth.getCurrentUser().getEmail());
        Log.e(TAG, "onCreate: 가져온 후 "+ runningList);
        //굳이 서버에서 가져오지는 않는다. 저장만 서버에 해놓는다.

        total_distance = findViewById(R.id.total_distance);
        total_count_num = findViewById(R.id.total_count_num);
        average_distance_num = findViewById(R.id.average_distance_num);
        average_pace_num = findViewById(R.id.average_pace_num);

        adapter = new RecordAdapter(runningList);
        recyclerView.setAdapter(adapter);

        adapter.notifyDataSetChanged();

        add_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RecordActivity.this,RunAddActivity.class);
                startActivityForResult(intent, 1);//다른화면에서 처리하면 그것을 onActivityResult에서 받아서 list에 add하고 notify한다
            }
        });

        adapter.setOnItemClickListener(new OnItemClickListener(){
            @Override
            public void onItemClick(int position, RunningItem runningItem) {//그날의 기록으로 넘어갈 수 있다. 넘어간 후에 수정 삭제가 가능하다.
                ReadyActivity.runningItem = runningList.get(position);//그 위치에 있는 러닝 아이템 받아옴
                runningItem.position = position;
                RecordActivity.justWatching = true;
                boolean isDirectRunning = ReadyActivity.runningItem.isDirectRunning();
                Intent intent;
                if(isDirectRunning){
                    intent= new Intent(RecordActivity.this, TodayActivity.class);
                }else{
                    intent= new Intent(RecordActivity.this, MachineTodayActivity.class);
                }
                startActivity(intent);
            }
        });

        setToolbarMenu();
        setTabLayout(0);
        record_lt.setImageResource(R.drawable.ic_storage_black_24dp);
    }
    public void changeActivity(int index) {
        switch(index) {
            case 1:
                Intent intent = new Intent(this, MedalActivity.class);
                intent.putExtra("selected_tab",index);
                record_index = 1;
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.e("record Activity", "onRestart:!!!!!!!!! -레코드 ");
    }

    @Override
    protected  void onResume(){
        super.onResume();
        Log.e(TAG, "onResume: 아이템이 "+ReadyActivity.runningItem);
        if(ReadyActivity.runningItem !=null){//삭제시키거나 추가하거나
            Log.e(TAG, "onResume: "+ReadyActivity.runningItem.getDeleted());
            if(ReadyActivity.runningItem.getDeleted()&& RecordActivity.justWatching){//조회하다가 삭제함
                runningList.remove(ReadyActivity.runningItem.position);
            }else if(ReadyActivity.runningItem.getModified()&&RecordActivity.justWatching){//조회하다가 수정함
                int position = ReadyActivity.runningItem.position;
                ReadyActivity.runningItem.setModified(false);//원래대로 되돌려놓고
                runningList.remove(position);
                runningList.add(position, ReadyActivity.runningItem);
            } else if(ReadyActivity.runningItem.getDeleted() && !RecordActivity.justWatching){//바로 삭제 시킨다면
                //아무것도 안한다. 그냥 그대로 있다.
            }else{
                final double run_dist = ReadyActivity.runningItem.getKm();
                database.getReference("participate").child(auth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(final DataSnapshot snapshot : dataSnapshot.getChildren()){
                            //스냅샷 하나당 내가 참여하는 챌린지 하나
                            database.getReference("challenge").child(snapshot.getKey()).child(auth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                //내가 참여하는 챌린지들을 쏙쏙 골라서 내꺼를 불러오고 다시 넣는다.
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    double my_distance = dataSnapshot.getValue(Double.class);
                                    my_distance-= run_dist;
                                    database.getReference("challenge").child(snapshot.getKey()).child(auth.getCurrentUser().getUid()).setValue(my_distance);
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
                runningList.add(0,ReadyActivity.runningItem);
            }
            //리스트의 변화를 반영한다.
            adapter.notifyDataSetChanged();
            saveRunListPref(auth.getCurrentUser().getEmail());
            ReadyActivity.runningItem = null;
        }

        int totalCountNum = runningList.size();
        totalDistance = 0.0;
        averageDistance = 0.0;
        int totalTime = 0;
        averageTime = 0;
        int totalPace = 0;
        int averagePace = 0;
        for(int i=0; i<runningList.size();i++){
            totalDistance += runningList.get(i).getKm();
            totalPace += runningList.get(i).getPace_seconds();
            totalTime += runningList.get(i).getRuntime_seconds();
        }
        if(totalCountNum!=0) {
            averageDistance = totalDistance / (double) totalCountNum;
            averageDistance = Math.round(averageDistance * 100) / 100.0;//소숫점 둘째자리 까지만
            totalDistance = Math.round(totalDistance * 100) / 100.0;//전체거리도 마찬가지
            averagePace = totalPace / totalCountNum;
            averageTime = totalTime/ totalCountNum;
        }
        String paceString = averagePace/60+"\'"+averagePace%60+"\'\'";

      total_distance.setText(String.valueOf(totalDistance));
      total_count_num.setText(String.valueOf(totalCountNum));
      average_distance_num.setText(String.valueOf(averageDistance));
      average_pace_num.setText(paceString);
      saveAverage(auth.getCurrentUser().getEmail());
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e("RecordActivity", "onStop-  레코드" );
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e("RecordActivity", "onpause -  레코드 ");

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("RecordActivity", "destroy - 레코드 " );
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.e("RecordActivity", "start - 레코드" );
    }


}
