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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import java.util.ArrayList;

public class AddFriendActivity extends AppCompatActivity {

    private static final String TAG = "AddFriendActivity";
    MaterialSearchView searchView;
    RecyclerView friendRecycler;
    ArrayList<AddFriendItem> friendItems = new ArrayList<>();
    ArrayList<String> myFriendList = new ArrayList<>();
    AddFriendAdapter adapter;
    RecyclerView.LayoutManager layoutManager;
    GenericTypeIndicator<ArrayList<String>> type = new GenericTypeIndicator<ArrayList<String>>() {};

    private FirebaseDatabase database;
    private FirebaseAuth auth;
    Button confirm;

    ArrayList<String> inviteFriendList = new ArrayList<>();
    ArrayList<String> alreadyInvited;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);

        confirm = findViewById(R.id.confirm);

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

        //검색 툴바 세팅
        Toolbar toolbar = findViewById(R.id.searchBar);
        setSupportActionBar(toolbar);//액션바를 없애고 이거로 쓰겠다.
        getSupportActionBar().setTitle("검색으로 친구 초대");
        toolbar.setTitleTextColor(Color.BLACK);

        friendRecycler = findViewById(R.id.friendRecycler);
        layoutManager = new LinearLayoutManager(getApplicationContext(),LinearLayoutManager.VERTICAL,false);
        adapter = new AddFriendAdapter(AddFriendActivity.this, friendItems);
        friendRecycler.setLayoutManager(layoutManager);
        friendRecycler.setAdapter(adapter);

        //이미 만들어진 챌린지에서 친구를 추가하는 경우
        final Intent intent = getIntent();
        alreadyInvited = (ArrayList<String>)intent.getSerializableExtra("alreadyInvited");
        Log.e(TAG, "onCreate: intent에서 받는지 봅시다"+alreadyInvited);

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent resultIntent = new Intent();
                for(AddFriendItem friend : friendItems){
                    if(friend.isInvited && !friend.alreadyInvited){ //체크되었지만 이미 초대된 사람들은 아닌
                        inviteFriendList.add(friend.uid);
                    }
                }//체크되었던 사람들을 리스트에 추가한다.
                    resultIntent.putExtra("inviteList", inviteFriendList);
                    setResult(RESULT_OK, resultIntent);
                    finish();//makeChallenge와 created activity 모두 가능
            }
        });
    }

    public void getMyFriendList(DatabaseReference ref){
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                myFriendList = dataSnapshot.getValue(type);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    public void addInvitedFriend(UserDTO userDTO){
        Log.e(TAG, "onDataChange: 이미 초대된 친구는 "+userDTO.name+"이고, alreadyInvited = true");
        AddFriendItem friendItem = new AddFriendItem(userDTO.uid, userDTO.name, userDTO.profileUrl, true);
        friendItem.alreadyInvited = true; //이미 초대된 처리를 한다.
        friendItems.add(friendItem);
        adapter.notifyDataSetChanged();
    }

    public void addNewFriend(UserDTO userDTO){
        friendItems.add(new AddFriendItem(userDTO.uid, userDTO.name, userDTO.profileUrl, false));//처음엔 다 false
        adapter.notifyDataSetChanged();
    }

    public void showAllFriends(){
        setNewAdapter(friendItems);
    }

    public void setNewAdapter(ArrayList<AddFriendItem> list){
        AddFriendAdapter adapter1 = new AddFriendAdapter(AddFriendActivity.this,list);
        friendRecycler.setAdapter(adapter1);
    }

    @Override
    protected void onStart() {
        super.onStart();
        friendItems.clear();
        myFriendList.clear();

        DatabaseReference userListRef = database.getReference().child("userlist");//유저들의 목록
        String myUid = auth.getCurrentUser().getUid();//나의 uid
        DatabaseReference myFriendListRef = userListRef.child(myUid).child("friendList");//내 친구목록

        //내 친구목록을 구한다.
        getMyFriendList(myFriendListRef);

        //그 친구들의 정보를 리사이클러뷰 리스트에 넣는다.
        //이미 기존에 추가되었던 친구인지 아닌지에 따라 다른 처리를 해준다.
        userListRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    if(myFriendList.contains(snapshot.getKey())){ //내 친구라면
                        UserDTO userDTO = snapshot.getValue(UserDTO.class);
                        if(alreadyInvited.contains(userDTO.uid)){//이미 초대된 친구라면
                            addInvitedFriend(userDTO);
                        }else{//새로운 친구라면
                            addNewFriend(userDTO);
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        searchView = findViewById(R.id.searchView);
        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {
            }

            @Override
            public void onSearchViewClosed() {
                showAllFriends();
            }
        });

        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(newText != null && !newText.isEmpty()){//입력한 것이 있다면
                    ArrayList<AddFriendItem> found = new ArrayList<>();
                    for(AddFriendItem item: friendItems){
                        if(item.username.contains(newText)){//이름이 검색어에 포함되면 추가
                            found.add(item);
                        }
                    }
                    setNewAdapter(found);
                }else{ //아무것도 입력한게 없으면 전체 친구들이 다 보이게 한다.
                    showAllFriends();
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
