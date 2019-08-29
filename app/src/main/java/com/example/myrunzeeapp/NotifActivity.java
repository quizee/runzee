package com.example.myrunzeeapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class NotifActivity extends MenuActivity implements ItemTouchHelperListener {
    ArrayList<MessageItem> messageItems = new ArrayList<>();
    private static String TAG ="NotifActivity";
    RecyclerView msg_recycler;
    MessageAdapter msg_adapter;
    RecyclerView.LayoutManager msg_layout;
    private FirebaseDatabase database;
    private FirebaseAuth auth;
    String username;
    String userprofile;
    String contents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notif);
        //firebase
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

        msg_recycler = findViewById(R.id.msg_recycler);
        msg_adapter = new MessageAdapter(NotifActivity.this, messageItems);
        msg_layout = new LinearLayoutManager(getApplicationContext(),LinearLayoutManager.VERTICAL,false);
        msg_recycler.setAdapter(msg_adapter);
        msg_recycler.setLayoutManager(msg_layout);
        msg_recycler.setItemAnimator(new DefaultItemAnimator());
        DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(getApplicationContext(),new LinearLayoutManager(this).getOrientation());
        msg_recycler.addItemDecoration(dividerItemDecoration);

        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new SwipeDeleteHelper(0,ItemTouchHelper.LEFT,this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(msg_recycler);


        setToolbarMenu();
        notification_lt.setImageResource(R.drawable.ic_notifications_black_24dp);
    }

    @Override
    protected void onStart() {
        super.onStart();
        messageItems.clear();

        //정보 받아오기
        database.getReference().child("messages").child(auth.getCurrentUser().getUid()).addChildEventListener(new ChildEventListener() {
            //로그인한 사람의 메세지함에 들어가 하나씩 받아온다.
            //messageDTO 받아오기 --> 이를 바탕으로 messageItem 구성하기
            //sender uid 를 기반으로 username contents url 3개 확보해야함
            int count = 1;
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.e(TAG, "onChildAdded: "+dataSnapshot);
                final MessageDTO msg = dataSnapshot.getValue(MessageDTO.class);
                //이렇게 하는 이유는 사용자 이름, url 은 자주 바뀔 수 있는 정보이기 때문에 그것들이 바뀔 때마다 메세지에도 반영해야하면 골치아프기 때문이다.
                //contents는 username에 영향을 받기 때문에 자연스럽게 이것도 새로 구해야하는 정보로 구분
                database.getReference().child("userlist").child(msg.sender_uid).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                            Log.e(TAG, "onDataChange: "+snapshot);
                            if(snapshot.getKey().equals("name")){
                                username = snapshot.getValue(String.class);//이름 확보
                                switch(msg.message_type){//contents 확보
                                    case "request":
                                        contents = username+"님이 친구 요청을 보냈습니다.";
                                        break;
                                    case "accept":
                                        contents = username+"님이 친구 요청을 수락했습니다.";
                                        break;
                                    case "cheer":
                                        contents = username+"님이 응원메세지를 보냈습니다.";
                                        break;
                                    default: break;
                                }
                                Log.e(TAG, "onDataChange: 컨텐츠정보 넘기기!!!!"+contents);
                            }else if(snapshot.getKey().equals("profileUrl")){
                                userprofile = snapshot.getValue(String.class);//프사 확보
                                Log.e(TAG, "onDataChange: 컨텐츠정보 넘기기!!!!"+userprofile);
                            }
                        }
                        MessageItem msgItem = new MessageItem(username, userprofile,contents, msg);
                        if(!messageItems.contains(msgItem)) {
                            messageItems.add(msgItem);
                            msg_adapter.notifyDataSetChanged();
                        }
                        Log.e(TAG, "onDataChange: "+count+"번째 for문 돈 후 ");
                        count ++;
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
                //String sender_name, String sender_url, String contents, String message_type, long time
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }
            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                Log.e(TAG, "onChildRemoved: "+dataSnapshot);
                MessageDTO msg = dataSnapshot.getValue(MessageDTO.class);
                //이에 해당하는 메세지 아이템을 리스트에서 제거한다
                for(int i = 0 ; i<messageItems.size(); i++){
                    if(msg.when_made == messageItems.get(i).msg.when_made){
                        messageItems.remove(i);
                        msg_adapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        msg_adapter.notifyDataSetChanged();
        Log.e(TAG, "onChildAdded: "+"notifiy 합니다!! " );

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK) {
            if (requestCode == MessageAdapter.request_code) {//친구 요청인 경우

            }else if(requestCode == MessageAdapter.cheer_code) {//응원메세지인 경우

            }
        }
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if(viewHolder instanceof MessageAdapter.MyViewHolder){
            MessageItem deletedItem = messageItems.get(viewHolder.getAdapterPosition());
            int deleteIndex = viewHolder.getAdapterPosition();
            //messageItems.remove(deleteIndex);
            //msg_adapter.removeItem(deleteIndex);
            //msg_adapter.notifyItemRemoved(deleteIndex);
            database.getReference().child("messages").child(auth.getCurrentUser().getUid()).child(deletedItem.msg.message_uid).removeValue();
            Log.e(TAG, "onSwiped: "+ auth.getCurrentUser().getUid()+ "에서"+deletedItem.msg.sender_uid+"삭제");
        }
    }
}
