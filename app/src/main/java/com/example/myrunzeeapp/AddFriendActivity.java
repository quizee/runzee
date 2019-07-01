package com.example.myrunzeeapp;

import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import java.util.ArrayList;

public class AddFriendActivity extends AppCompatActivity {

    private static final String TAG = "AddFriendActivity";
    MaterialSearchView searchView;
    RecyclerView friend_recycler;
    ArrayList<AddFriendItem> friendItems = new ArrayList<>();
    ArrayList<String> myFriendList = new ArrayList<>();
    AddFriendAdapter adapter;
    RecyclerView.LayoutManager layoutManager;
    GenericTypeIndicator<ArrayList<String>> type = new GenericTypeIndicator<ArrayList<String>>() {};

    private FirebaseDatabase database;
    private FirebaseAuth auth;
    Button confirm;

    ArrayList<String> inviteFriendList = new ArrayList<>();
    ArrayList<String> already_invited;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);

        confirm = findViewById(R.id.confirm);

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

        //검색 툴바 세팅
        Toolbar toolbar = findViewById(R.id.search_bar);
        setSupportActionBar(toolbar);//액션바를 없애고 이거로 쓰겠다.
        getSupportActionBar().setTitle("검색으로 친구 초대");
        toolbar.setTitleTextColor(Color.BLACK);

        friend_recycler = findViewById(R.id.friend_recycler);
        layoutManager = new LinearLayoutManager(getApplicationContext(),LinearLayoutManager.VERTICAL,false);
        adapter = new AddFriendAdapter(AddFriendActivity.this, friendItems);
        friend_recycler.setLayoutManager(layoutManager);
        friend_recycler.setAdapter(adapter);

        //이미 만들어진 챌린지에서 친구를 추가하는 경우
        final Intent intent = getIntent();
        already_invited = (ArrayList<String>)intent.getSerializableExtra("already_invited");
        Log.e(TAG, "onCreate: intent에서 받는지 봅시다"+already_invited);

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent resultIntent = new Intent();
                for(AddFriendItem friend : friendItems){
                    if(friend.isInvited && !friend.already_invited){ //체크되었지만 이미 초대된 사람들은 아닌
                        inviteFriendList.add(friend.uid);
                    }
                }//체크되었던 사람들을 리스트에 추가한다.
                    resultIntent.putExtra("inviteList", inviteFriendList);
                    setResult(RESULT_OK, resultIntent);
                    finish();//makeChallenge와 created activity 모두 가능
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        friendItems.clear();
        myFriendList.clear();

        //내 친구리스트를 먼저 가져온다.
        database.getReference().child("userlist").child(auth.getCurrentUser().getUid())
                .child("friendList")
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                myFriendList = dataSnapshot.getValue(type);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //그 친구들의 정보를 리사이클러뷰 리스트에 넣는다.
        database.getReference().child("userlist").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    if(myFriendList.contains(snapshot.getKey())){
                        //내 친구라면
                        UserDTO userDTO = snapshot.getValue(UserDTO.class);
                        Log.e(TAG, "already_invited 목록에 대한 기록입니다. null이면 처음 생성하는 겁니다. "+already_invited);
                        //이미 만들어진 챌린지에서 추가한 거라면
                        if(already_invited!=null){
                            if(already_invited.contains(userDTO.uid)) {//이미 초대된 친구라면
                                Log.e(TAG, "onDataChange: 이미 초대된 친구는 "+userDTO.name+"이고, already_invited = true");
                                AddFriendItem friendItem = new AddFriendItem(userDTO.uid, userDTO.name, userDTO.profile_url, true);
                                friendItem.already_invited = true; //이미 초대된 처리를 한다.
                                friendItems.add(friendItem);
                                adapter.notifyDataSetChanged();
                            }else{//나머지는 처음과 동일하게
                                Log.e(TAG, "onDataChange: 아직 초대되지 않은 친구는"+ userDTO.name+"입니다" );
                                friendItems.add(new AddFriendItem(userDTO.uid, userDTO.name, userDTO.profile_url, false));//처음엔 다 false
                                adapter.notifyDataSetChanged();
                            }
                        }else {
                            friendItems.add(new AddFriendItem(userDTO.uid, userDTO.name, userDTO.profile_url, false));//처음엔 다 false
                            adapter.notifyDataSetChanged();
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        searchView = findViewById(R.id.search_view);
        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {
            }

            @Override
            public void onSearchViewClosed() {
                AddFriendAdapter adapter1 = new AddFriendAdapter(AddFriendActivity.this,friendItems);
                friend_recycler.setAdapter(adapter1);
                //아무것도 입력한게 없으면 전체 친구들 다 보이게 한다.
            }
        });

        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(newText != null && !newText.isEmpty()){
                    ArrayList<AddFriendItem> found = new ArrayList<>();
                    for(AddFriendItem item: friendItems){
                        if(item.username.contains(newText)){//이름이 검색어에 포함되면 추가
                            found.add(item);
                        }
                    }
                    AddFriendAdapter adapter1 = new AddFriendAdapter(AddFriendActivity.this,found);
                    Log.e(TAG, "onQueryTextChange: "+found.size());
                    friend_recycler.setAdapter(adapter1);
                }else{
                    AddFriendAdapter adapter1 = new AddFriendAdapter(AddFriendActivity.this,friendItems);
                    friend_recycler.setAdapter(adapter1);
                    //아무것도 입력한게 없으면 전체 친구들 다 보이게 한다.
                }
                return true;
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        searchView.setMenuItem(item);
        return true;
    }
}
