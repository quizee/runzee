package com.example.myrunzeeapp;

import android.content.Intent;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

import java.util.ArrayList;
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
                ctos.add(dataSnapshot.getValue(ChallengeDTO.class));
                adapter.notifyDataSetChanged();
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
