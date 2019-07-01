package com.example.myrunzeeapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RequestMessageActivity extends AppCompatActivity {

    ImageView sender_profile;
    TextView username_tv;
    ImageView sender_belt;
    TextView sender_belt_text;
    TextView msg_content;
    Button accept;
    Button reject;
    String belt;

    //해당하는 uid에게 답장을 보내야하고, 무슨 벨트인지도 알아야함
    FirebaseDatabase database;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_message);

        sender_profile = findViewById(R.id.sender_profile);
        username_tv = findViewById(R.id.username);
        sender_belt = findViewById(R.id.sender_belt);
        sender_belt_text = findViewById(R.id.sender_belt_text);
        msg_content = findViewById(R.id.msg_content);
        accept = findViewById(R.id.accept);
        reject = findViewById(R.id.reject);

        Intent intent = getIntent();
        final MessageItem msgItem = (MessageItem) intent.getSerializableExtra("msg_item") ;
        //어뎁터에서 보낸 클릭된 메시지 아이템을 받는다.

        if(msgItem.msg.message_type.equals("accept")){
            accept.setVisibility(View.GONE);
            reject.setVisibility(View.GONE);
        }

        username_tv.setText(msgItem.sender_name);
        msg_content.setText(msgItem.contents);
        Glide.with(this).load(msgItem.sender_url).apply(RequestOptions.circleCropTransform()).into(sender_profile);

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        //일단 무슨 벨트인지 가져온다.
        database.getReference().child("runninglist").child(msgItem.msg.sender_uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    if(snapshot.getKey().equals("belt")){
                        belt = snapshot.getValue(String.class);
                        switch (belt){
                            case "white":
                                sender_belt.setImageResource(R.drawable.white_belt);
                                sender_belt_text.setText("화이트벨트 회원");
                                break;
                            case "blue":
                                sender_belt.setImageResource(R.drawable.blue_belt);
                                sender_belt_text.setText("블루벨트 회원");
                                break;
                            case "yellow":
                                sender_belt.setImageResource(R.drawable.yellow_belt);
                                sender_belt_text.setText("옐로우벨트 회원");
                                break;
                            case "purple":
                                sender_belt.setImageResource(R.drawable.purple_belt);
                                sender_belt_text.setText("퍼플벨트 회원");
                                break;
                            case "black":
                                sender_belt.setImageResource(R.drawable.black_belt);
                                sender_belt_text.setText("블랙벨트 회원");
                                break;
                            case "red":
                                sender_belt.setImageResource(R.drawable.red_belt);
                                sender_belt_text.setText("레드벨트 회원");
                                break;
                            default:break;
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //친구에게 수락 메시지가 감
                String key = database.getReference().child("messages").child(msgItem.msg.sender_uid).push().getKey();
                MessageDTO msg_accept = new MessageDTO(auth.getCurrentUser().getUid(),key,"accept");
                database.getReference().child("messages").child(msgItem.msg.sender_uid).child(key).setValue(msg_accept);
                //내 메시지함에서 요청메시지가 사라짐
                database.getReference().child("messages").child(auth.getCurrentUser().getUid()).child(msgItem.msg.message_uid).removeValue();
                //서로의 친구목록에 서로의 uid가 추가됨
                database.getReference().child("userlist").addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        if(dataSnapshot.getKey().equals(auth.getCurrentUser().getUid())){//내꺼에 상대 추가
                            UserDTO meDTO = dataSnapshot.getValue(UserDTO.class);
                            meDTO.friendList.add(msgItem.msg.sender_uid);
                            database.getReference().child("userlist").child(auth.getCurrentUser().getUid()).setValue(meDTO);

                        }else if(dataSnapshot.getKey().equals(msgItem.msg.sender_uid)){//상대꺼에 나 추가
                            UserDTO youDTO = dataSnapshot.getValue(UserDTO.class);
                            youDTO.friendList.add(auth.getCurrentUser().getUid());
                            database.getReference().child("userlist").child(msgItem.msg.sender_uid).setValue(youDTO);
                        }
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
                finish();
            }
        });
        reject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //내 메시지함에서 요청메시지가 사라짐
                database.getReference().child("messages").child(auth.getCurrentUser().getUid()).child(msgItem.msg.message_uid).removeValue();
                finish();
            }
        });





    }
}
