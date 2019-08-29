package com.example.myrunzeeapp;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class CreatedChallengeActivity extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseDatabase database;

    TextView title;
    TextView left_date;
    TextView due_date;
    TextView distance;
    ImageView back;
    ImageView imageView3;
    RecyclerView challenge_recycler;

    ArrayList<ChallengeItem> boardItems = new ArrayList<>();
    ArrayList<ChallengeDTO> ctos = new ArrayList<>();
    RecyclerView.LayoutManager layoutManager;
    BoardAdapter boardAdapter;
    ChallengeDTO cto;
    ArrayList<String> added_friends;

    static boolean again = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_created_challenge);
        if(again){
            finish();
        }

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        title = findViewById(R.id.title);
        left_date = findViewById(R.id.left_date);
        due_date = findViewById(R.id.due_date);
        distance = findViewById(R.id.distance);
        back = findViewById(R.id.back);
        challenge_recycler = findViewById(R.id.challenge_recycler);
        imageView3 = findViewById(R.id.imageView3);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                again = false;
                finish();
            }
        });


        Intent intent = getIntent();
        boardItems = (ArrayList<ChallengeItem>)intent.getSerializableExtra("leader_board");
        //userDTO와 거리
        cto = (ChallengeDTO)intent.getSerializableExtra("challenge_info") ;
        //챌린지 정보

        Log.e("createdActivity", "onCreate: create 액티비티는 왜 여러번 생성되는 걸깡ㅅ" +added_friends);

        Glide.with(imageView3.getContext()).load(cto.cover_url).into(imageView3);

        title.setText(cto.title);

        //남은 날 계산
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd");
        try {
            Date endDate = formatter.parse(cto.end_date);
            Date today = new Date(System.currentTimeMillis());

            long diff = endDate.getTime() - today.getTime();
            long diffDays = diff / (24 * 60 * 60 * 1000); //하루 단위로
            left_date.setText(diffDays+"일 남음");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        due_date.setText(cto.start_date+" - "+cto.end_date);
        distance.setText(cto.distance+" KM");

        layoutManager = new LinearLayoutManager(getApplicationContext(),LinearLayoutManager.VERTICAL,false);
        boardAdapter = new BoardAdapter(CreatedChallengeActivity.this,cto.distance,boardItems);
        challenge_recycler.setAdapter(boardAdapter);
        challenge_recycler.setLayoutManager(layoutManager);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
    }

    //친구를 추가로 초대할 때
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            if(requestCode == 23){
                //추가된 친구의 uid만 받으면 된다.
                added_friends = (ArrayList<String>) data.getSerializableExtra("inviteList");
                again = true;
                Log.e("createdActivity", "onActivityResult: 호출 여러번 되니????????????"+added_friends);
                for(String added_friend: added_friends){
                    Log.e("CreatedActivity", "onActivityResult: "+added_friend+"님이 새로 추가되었습니다. ");
                    database.getReference("challenge").child(cto.challenge_id).child(added_friend).setValue(0.0);
                    database.getReference("participate").child(added_friend).child(cto.challenge_id).setValue(cto);
                }

                database.getReference("userlist").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                            if(added_friends.contains(snapshot.getKey())){
                                //마지막에 추가한다
                                ChallengeItem challengeItem = new ChallengeItem(snapshot.getValue(UserDTO.class),0.0);
                                if(!boardItems.contains(challengeItem)) {
                                    boardItems.add(boardItems.size() - 1, new ChallengeItem(snapshot.getValue(UserDTO.class), 0.0));
                                    boardAdapter.notifyDataSetChanged();
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                //boardAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.challenge_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.getout:
                //챌린지에서 내 이름을 지움
                database.getReference("challenge").child(cto.challenge_id).child(auth.getCurrentUser().getUid()).removeValue();
                //내가 참여중인 목록에서 챌린지를 지움
                database.getReference("participate").child(auth.getCurrentUser().getUid()).child(cto.challenge_id).removeValue();
                Intent intent = new Intent(CreatedChallengeActivity.this,MyChallengeActivity.class);
                startActivity(intent);
                finish();
                return true;
            case R.id.invite:
                //종료되지 않은 경우에만 초대 가능함
                Intent intent1 = new Intent(CreatedChallengeActivity.this,AddFriendActivity.class);
                ArrayList<String> invitedMembers = new ArrayList<>();
                for( ChallengeItem challengeItem : boardItems){
                    invitedMembers.add(challengeItem.userDTO.uid);
                }
                intent1.putExtra("alreadyInvited",invitedMembers);
                Log.e("CreatedActivity", "onOptionsItemSelected: "+ invitedMembers+"보내려고 합니다!!!");
                startActivityForResult(intent1,23);
                //startActivity(intent1);
                //finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
