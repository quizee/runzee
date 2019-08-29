package com.example.myrunzeeapp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.solver.widgets.Snapshot;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class ClubActivity extends MenuActivity {
    static int club_index = 0;
    private static String TAG = "ClubActivity";

    //두가지 종류의 친구목록이 떠야한다. 추천친구와 친구검색

    //추천 친구
    ArrayList<FriendItem> recommendFriends = new ArrayList<>();
    RecyclerView crew_recycler;
    FriendListAdapter recommend_adapter;

    //검색 친구
    MaterialSearchView searchView;
    RecyclerView friend_recycler;
    ArrayList<FriendItem> friendItems = new ArrayList<>();
    ArrayList<FriendItem> defaultItems = new ArrayList<>();
    FriendListAdapter search_adapter;

    //둘다 linear layout manager
    RecyclerView.LayoutManager layoutManager_search;
    RecyclerView.LayoutManager layoutManager_recommend;
    private FirebaseDatabase database;
    private FirebaseAuth auth;

    GenericTypeIndicator<ArrayList<String>> type = new GenericTypeIndicator<ArrayList<String>>() {};
    //내 친구들 목록을 받아오기 위한 리스트
    ArrayList<String> myFriendList = new ArrayList<>();
    ArrayList<UserDTO> totalUserList = new ArrayList<>();
    //친구 추천 목록을 위한 해시맵
    HashMap<String, Integer> recommendMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_club);
        setToolbarMenu();

        //검색 툴바 세팅
        Toolbar toolbar = findViewById(R.id.searchBar);
        setSupportActionBar(toolbar);//액션바를 없애고 이거로 쓰겠다.
        getSupportActionBar().setTitle("친구 이메일 또는 이름 검색");
        toolbar.setTitleTextColor(Color.BLACK);

        //firebase 세팅
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

        //추천 친구 세팅
        crew_recycler = findViewById(R.id.crew_recycler);
        recommend_adapter = new FriendListAdapter(ClubActivity.this, recommendMap, recommendFriends);
        layoutManager_recommend = new LinearLayoutManager(getApplicationContext(),LinearLayoutManager.VERTICAL,false);
        crew_recycler.setLayoutManager(layoutManager_recommend);
        crew_recycler.setAdapter(recommend_adapter);

        //검색 친구 세팅
        friend_recycler = findViewById(R.id.friend_recycler);
        search_adapter = new FriendListAdapter(ClubActivity.this, recommendMap, defaultItems);//처음에는 아무것도 안보이게 !
        layoutManager_search = new LinearLayoutManager(getApplicationContext(),LinearLayoutManager.VERTICAL,false);
        friend_recycler.setLayoutManager(layoutManager_search);
        friend_recycler.setAdapter(search_adapter);

        setToolbarMenu();
        setTabLayout(0);
        group_lt.setImageResource(R.drawable.ic_group_black_24dp);

    }

    public void addCandidate(DataSnapshot dataSnapshot){
        Log.e(TAG, "onChildAdded: 내친구가 아닌 사람들의 모임입니다" );

        UserDTO searchDTO = dataSnapshot.getValue(UserDTO.class);
        FriendItem newFriend = new FriendItem(searchDTO.name, searchDTO.profileUrl, searchDTO.email, searchDTO.uid);
        friendItems.add(newFriend);
        search_adapter.notifyDataSetChanged();

        Log.e(TAG, "후보 "+friendItems.size() + searchDTO.name);
    }

    public void makeFriendMap(UserDTO recomDTO, String myUid){
        for(String yourFriendUid : recomDTO.friendList){
            if(!yourFriendUid.equals(myUid) && !myFriendList.contains(yourFriendUid)){//그 친구가 나 이거나 이미 내 친구라면 제외함
                if (recommendMap.containsKey(yourFriendUid)) {//이미 있다면
                    int count = recommendMap.get(yourFriendUid);
                    count++; //출현 횟수를 한번 늘린다.
                    Log.e(TAG, recomDTO.name+"씨 친구목록 안에 있습니다. "+ yourFriendUid+" "+count);
                    recommendMap.put(yourFriendUid, count);
                } else {//아직 없다면
                    recommendMap.put(yourFriendUid, 1);
                    Log.e(TAG, recomDTO.name+"씨 친구목록 안에 있습니다. "+ yourFriendUid+" "+1);
                    //최초로 등장했으므로 출현 횟수는 1
                }
                Log.e(TAG, "여기까지 완성된 해시맵: "+recommendMap);
                //이게 변경이 되는지는 모르겠다
            }
        }
    }

    public void sortFriends(){
        FriendItem temp ;
        for(int i = recommendFriends.size()-1; i>0; i--){
            for(int j = 0; j<i; j++){
                if(recommendMap.get(recommendFriends.get(j).uid)<recommendMap.get(recommendFriends.get(j+1).uid)){
                    temp = recommendFriends.get(j);
                    recommendFriends.remove(j);
                    recommendFriends.add(j+1, temp);
                }
            }
        }
    }

    public void putSearchedWord(ArrayList<FriendItem> found, String newText){
        for(FriendItem item: friendItems){
            if(newText.contains("@")) {
                String search_words[];
                search_words = newText.split("@");//검색어에 @ 가 포함되면 앞부분까지만 본다.
                newText = search_words[0];
            }
            String results[] = item.email.split("@");//각 사용자의 이메일도 앞부분까지만 본다
            if(item.name.contains(newText) || results[0].contains(newText)){//이메일에 검색어가 포함되거나 이름에 포함되면 추가
                found.add(item);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        recommendMap.clear();
        recommendFriends.clear();
        friendItems.clear();
        myFriendList.clear();
        totalUserList.clear();

        DatabaseReference userListRef = database.getReference().child("userlist");//유저들의 목록
        final String myUid = auth.getCurrentUser().getUid();//나의 uid
        DatabaseReference myFriendListRef = userListRef.child(myUid).child("friendList");//내 친구목록

        myFriendListRef.addChildEventListener(new ChildEventListener() {
           @Override
           public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
               myFriendList.add(dataSnapshot.getValue(String.class));
               if(myFriendList!=null) {
                   Log.e(TAG, "onChildAdded: " + "내친구 구했다." + myFriendList.size());
               }else{
                   Log.e(TAG, "onChildAdded: 아직 친구 없음");
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

        //사람들 목록과 total friendlist를 만든다.
        userListRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull final DataSnapshot dataSnapshot, @Nullable String s) {
                //검색했을 때 나오는 사람들 목록
                String personKey = dataSnapshot.getKey();//personKey 하나 당 한 사람
                if(!personKey.equals(myUid) && !myFriendList.contains(personKey)) {//나 자신과 이미 있는 친구 제외
                    addCandidate(dataSnapshot);
                //내 친구들의 친구목록으로 함께 아는 친구 해시맵 만들기
                }else if(myFriendList.contains(personKey)){ //내 친구 목록에 있는 사람이라면
                    Log.e(TAG, "onChildAdded: 내 친구인 사람들의 모임입니다.");
                    UserDTO recomDTO = dataSnapshot.getValue(UserDTO.class);
                    makeFriendMap(recomDTO, myUid);
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

        userListRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    String onePerson = snapshot.getKey();
                    if(recommendMap.containsKey(onePerson)){//추천목록에 있는 사람이라면
                        UserDTO userDTO = snapshot.getValue(UserDTO.class);
                        FriendItem item = new FriendItem(userDTO.name,userDTO.profileUrl,userDTO.email,userDTO.uid);
                        if (!recommendFriends.contains(item)) {
                            recommendFriends.add(item);
                            sortFriends();
                            recommend_adapter.notifyDataSetChanged();
                            Log.e(TAG, " 추천 친구 리스트를 만들고 notify했습니다" + recommendFriends.size());
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //추천목록 해시맵이 완성되었으므로 어댑터를 만들 수 있다.

        searchView = findViewById(R.id.searchView);
        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {
            }

            @Override
            public void onSearchViewClosed() {
                FriendListAdapter adapter1 = new FriendListAdapter( ClubActivity.this, recommendMap, defaultItems);
                friend_recycler.setAdapter(adapter1);
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
                    ArrayList<FriendItem> found = new ArrayList<>();
                    putSearchedWord(found,newText);
                    FriendListAdapter adapter1 = new FriendListAdapter(ClubActivity.this, recommendMap, found);
                    Log.e(TAG, "onQueryTextChange: "+found.size());
                    friend_recycler.setAdapter(adapter1);
                }else{
                    //아무것도 안쳤을 때
                    FriendListAdapter adapter1 = new FriendListAdapter(ClubActivity.this, recommendMap, defaultItems);
                    friend_recycler.setAdapter(adapter1);
                }
                return true;
            }
        });
    }

    public void changeActivity(int index) {
        switch(index) {
            case 1:
                Intent intent = new Intent(ClubActivity.this, MyChallengeActivity.class);
                // intent.putExtra("selected_tab",index);
                startActivity(intent);
                club_index = 1;
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        searchView.setMenuItem(item);
        return true;
    }

}
