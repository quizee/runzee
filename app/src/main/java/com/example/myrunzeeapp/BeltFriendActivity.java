package com.example.myrunzeeapp;

import android.content.Intent;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class BeltFriendActivity extends AppCompatActivity {

    private static final String TAG = "BeltFriendActivity";
    TextView belt_name;
    RecyclerView beltfriend_recycler;
    BeltFriendAdapter belt_adapter;
    RecyclerView.LayoutManager layoutManager;
    ImageView back;

    ConstraintLayout beltfriend_layout;
    FirebaseDatabase database;
    ArrayList<String> friendlist;
    ArrayList<BeltFriendItem> beltFriends = new ArrayList<>();

    String profile;
    String name;
    String uid;
    String state_message;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_belt_friend);

        back = findViewById(R.id.back);
        belt_name = findViewById(R.id.belt_name);
        beltfriend_recycler = findViewById(R.id.beltfriend_recycler);
        beltfriend_layout = findViewById(R.id.beltfriend_layout);
        belt_adapter = new BeltFriendAdapter(BeltFriendActivity.this,beltFriends);
        layoutManager = new LinearLayoutManager(getApplicationContext(),LinearLayoutManager.VERTICAL,false);

        beltfriend_recycler.setAdapter(belt_adapter);
        beltfriend_recycler.setLayoutManager(layoutManager);

        database = FirebaseDatabase.getInstance();

        Intent intent = getIntent();
        friendlist = new ArrayList<>();
        friendlist = (ArrayList<String>) intent.getSerializableExtra("belf_friends");//전달받은 목록은 해당 벨트에 있는 친구들이다.
        String belt_color = intent.getStringExtra("belt_color");

        Log.e(TAG, "onCreate: 넘겨받은 친구 리스트"+friendlist);


        switch (belt_color){
            case "white":
                beltfriend_layout.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                break;
            case "yellow":
                beltfriend_layout.setBackgroundColor(getResources().getColor(R.color.yellow));
                break;
            case "blue":
                beltfriend_layout.setBackgroundColor(getResources().getColor(R.color.blue));
                break;
            case "purple":
                beltfriend_layout.setBackgroundColor(getResources().getColor(R.color.purple));
                break;
            case "black":
                beltfriend_layout.setBackgroundColor(getResources().getColor(R.color.black_opaque));
                break;
            case "red":
                beltfriend_layout.setBackgroundColor(getResources().getColor(R.color.red));
                break;
            default: break;
        }

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        for(String belt_friend: friendlist){
            database.getReference("userlist").child(belt_friend).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    uid = dataSnapshot.getKey();
                    Log.e(TAG, "onDataChange: userlist에서 벨트에 있는 친구 중 한명: "+uid);
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                        if(snapshot.getKey().equals("profile_url")){
                            profile = snapshot.getValue(String.class);
                            Log.e(TAG, "onDataChange: 프로필 "+profile+" 뽑고");
                        }
                        if(snapshot.getKey().equals("name")){
                            name = snapshot.getValue(String.class);
                            Log.e(TAG, "onDataChange: 이름 "+name+" 뽑고");
                        }
                        if(snapshot.getKey().equals("state_message")){
                            state_message = snapshot.getValue(String.class);
                            Log.e(TAG, "onDataChange: 상메 "+state_message+" 뽑고");
                        }
                    }
                    BeltFriendItem beltFriendItem = new BeltFriendItem(uid,name,profile,state_message);
                    Log.e(TAG, "onDataChange: 결정된 벨트 아이템"+beltFriendItem);
                    beltFriends.add(beltFriendItem);
                    belt_adapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

//        database.getReference("userlist").addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                Log.e(TAG, "onDataChange: 왜안찍히지?!?!?!?!?" );
//                if(friendlist.contains(dataSnapshot.getKey())){
//                    //벨트에 있는 친구라면
//                    uid = dataSnapshot.getKey();
//                    Log.e(TAG, "onDataChange: userlist에서 벨트에 있는 친구라면"+uid);
//                    //더 하위로 내려가서
//                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){
//                        if(snapshot.getKey().equals("profile_url")){
//                            profile = snapshot.getValue(String.class);
//                            Log.e(TAG, "onDataChange: 프로필 "+profile+" 뽑고");
//                        }
//                        if(snapshot.getKey().equals("name")){
//                            name = snapshot.getValue(String.class);
//                            Log.e(TAG, "onDataChange: 이름 "+name+" 뽑고");
//                        }
//                        if(snapshot.getKey().equals("state_message")){
//                            state_message = snapshot.getValue(String.class);
//                            Log.e(TAG, "onDataChange: 상메 "+state_message+" 뽑고");
//                        }
//                    }
//                    BeltFriendItem beltFriendItem = new BeltFriendItem(uid,name,profile,state_message);
//                    Log.e(TAG, "onDataChange: 결정된 아이템"+beltFriendItem);
//                    beltFriends.add(beltFriendItem);
//                    belt_adapter.notifyDataSetChanged();
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//            }
//        });
    }
}
