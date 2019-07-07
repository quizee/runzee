package com.example.myrunzeeapp;

import android.content.Intent;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class MyChallengeActivity extends MenuActivity {

    private static String TAG = "MyChallengeActivity";

    Button make_challenge;
    FirebaseDatabase database;
    FirebaseAuth auth;
    RecyclerView recyclerView;
    RecyclerView recyclerView2;
    MyChallengeAdapter adapter;
    MyChallengeAdapter adapter2;
    RecyclerView.LayoutManager layoutManager;
    RecyclerView.LayoutManager layoutManager2;

    ArrayList<ChallengeDTO> ctos = new ArrayList<>();//참여중인 챌린지 리사이클러뷰에 쓸 리스트
    ArrayList<ChallengeDTO> done_ctos = new ArrayList<>();//이전에 참여한 챌린지 리사이클러뷰에 쓸 리스트

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_challenge);
        make_challenge = findViewById(R.id.make_challenge);

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

        recyclerView = findViewById(R.id.crew_recycler);
        recyclerView2 = findViewById(R.id.done_recycler);

        layoutManager = new LinearLayoutManager(getApplicationContext(),LinearLayoutManager.VERTICAL,false);
        layoutManager2 = new LinearLayoutManager(getApplicationContext(),LinearLayoutManager.VERTICAL,false);
        DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(getApplicationContext(),new LinearLayoutManager(this).getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

        DividerItemDecoration dividerItemDecoration2 =
                new DividerItemDecoration(getApplicationContext(),new LinearLayoutManager(this).getOrientation());
        recyclerView2.addItemDecoration(dividerItemDecoration2);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView2.setLayoutManager(layoutManager2);

        make_challenge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MyChallengeActivity.this, MakeChallengeActivity.class);
                startActivity(intent);
            }
        });

        setToolbarMenu();
        setTabLayout(1);
        group_lt.setImageResource(R.drawable.ic_group_black_24dp);
    }

    @Override
    protected void onStart() {
        super.onStart();
        ctos.clear();
        done_ctos.clear();

        adapter = new MyChallengeAdapter(MyChallengeActivity.this,ctos);
        adapter2 = new MyChallengeAdapter(MyChallengeActivity.this,done_ctos);

        recyclerView.setAdapter(adapter);
        recyclerView2.setAdapter(adapter2);

        database.getReference().child("participate").child(auth.getCurrentUser().getUid()).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                String date = dataSnapshot.child("end_date").getValue(String.class);
                Date today = new Date(System.currentTimeMillis());
                today.setDate(today.getDate()-1);

                //String [] dates = date.split(".");//2019.7.1 이런 식
                Log.e(TAG, "onChildAdded: 날짜 가져옵니다"+date);
//                if(dates[1].length() == 1){
//                    dates[1] = "0"+dates[1];
//                }
//                if(dates[2].length() == 1){
//                    dates[2] = "0"+dates[2];
//                }
                // 한자리 씩이라면 0을 붙여준다.
                //date = dates[0]+"."+dates[1]+"."+dates[2];

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd");
                try {
                    Date challenge_date = dateFormat.parse(date);
                    //챌린지의 끝나는 날짜가 내일 이상이 아니면
                    if(!challenge_date.after(today)){//오늘끝나는것도 포함되버림
                        //participate 에서 done으로 옮긴다.
                        database.getReference("done").child(auth.getCurrentUser().getUid()).child(dataSnapshot.getKey()).setValue(dataSnapshot.getValue(ChallengeDTO.class));
                        database.getReference("participate").child(auth.getCurrentUser().getUid()).child(dataSnapshot.getKey()).removeValue();
                    }else {//아직 끝나는 날이 오지 않았다면
                        ctos.add(dataSnapshot.getValue(ChallengeDTO.class));
                        adapter.notifyDataSetChanged();
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                //date1.after(date2) date1이 date2보다 이후면 true

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

        database.getReference().child("done").child(auth.getCurrentUser().getUid()).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                done_ctos.add(dataSnapshot.getValue(ChallengeDTO.class));
                adapter2.notifyDataSetChanged();
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

    public void changeActivity(int index) {
        switch(index) {
            case 0:
                Intent intent = new Intent(MyChallengeActivity.this, ClubActivity.class);
                // intent.putExtra("selected_tab",index);
                startActivity(intent);
                ClubActivity.club_index = 0;
                break;
            default:
                break;
        }
    }
}
