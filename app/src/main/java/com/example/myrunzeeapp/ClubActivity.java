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
    ArrayList<FriendItem> recommend_friends = new ArrayList<>();
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



    //UserDTO recomDTO;
    //String yourFriendUid_static;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_club);
        setToolbarMenu();

        //검색 툴바 세팅
        Toolbar toolbar = findViewById(R.id.search_bar);
        setSupportActionBar(toolbar);//액션바를 없애고 이거로 쓰겠다.
        getSupportActionBar().setTitle("친구 이메일 또는 이름 검색");
        toolbar.setTitleTextColor(Color.BLACK);

        //firebase 세팅
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

        //추천 친구 세팅
        crew_recycler = findViewById(R.id.crew_recycler);
        recommend_adapter = new FriendListAdapter(ClubActivity.this, recommendMap, recommend_friends);
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

        //전체 유저 목록과 내 친구목록을 가져온다.
//        database.getReference().child("userlist").addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                Log.e(TAG, "onDataChange: "+"호출!!!!!");
//                myFriendList.clear();
//                totalUserList.clear();
//                //변화가 있으면 새로 넣어야하므로 비우고 시작한다
//                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
//                    //snapshot 하나당 한사람
//                    Log.e(TAG, "onDataChange: " +snapshot);
//                    if(snapshot.getKey().equals(auth.getCurrentUser().getUid())){
//                        Log.e(TAG, "onDataChange: if문 1"+snapshot);
//                        myFriendList = dataSnapshot.child("friendList").getValue(type);//내 친구 리스트
//                    }else{
//                        Log.e(TAG, "onDataChange: if문 1"+snapshot);
//                        totalUserList.add(snapshot.getValue(UserDTO.class));//전체 리스트
//                    }
//                }
//                Log.e(TAG, "onDataChange: 친구리스트"+myFriendList.size()+" 전체리스트"+totalUserList.size());
//            }
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        recommendMap.clear();
        recommend_friends.clear();
        friendItems.clear();
        myFriendList.clear();
        totalUserList.clear();


//        database.getReference().child("userlist").child(auth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                //snapshot 하나당 한명이다
//                myFriendList = dataSnapshot.child("friendList").getValue(type);
//                Log.e(TAG, "내 친구들 목록:"+myFriendList.size()+"구비 완료!");
//            }
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//            }
//        });

        //내 친구리스트를 먼저 가져온다.
        database.getReference().child("userlist").child(auth.getCurrentUser().getUid())
                .child("friendList")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        myFriendList = dataSnapshot.getValue(type);
                        if(myFriendList!=null) {
                            Log.e(TAG, "onDataChange: " + "내친구 구햇따." + myFriendList.size());
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        //전체 리스트를 가져온다.
        database.getReference().child("userlist").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    totalUserList.add(snapshot.getValue(UserDTO.class));
                }
                Log.e(TAG, "onDataChange: 전체리스트 구햇따"+totalUserList.size());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
//위에꺼 안되면 아래꺼로 걍 하자
//        database.getReference().child("userlist").addChildEventListener(new ChildEventListener() {
//            @Override
//            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//                Log.e(TAG, "차일드 들어왔다 add " +dataSnapshot);
//                if(dataSnapshot.getKey().equals(auth.getCurrentUser().getUid())){
//                    Log.e(TAG, ": if문 1"+dataSnapshot);
//                    myFriendList = dataSnapshot.child("friendList").getValue(type);//내 친구 리스트
//                }else{
//                    Log.e(TAG, " if문 1"+dataSnapshot);
//                    totalUserList.add(dataSnapshot.getValue(UserDTO.class));//전체 리스트
//                }
//                Log.e(TAG, "onDataChange: 친구리스트"+myFriendList.size()+" 전체리스트"+totalUserList.size());
//            }
//
//            @Override
//            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//
//            }
//
//            @Override
//            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
//
//            }
//
//            @Override
//            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });

        //사람들 목록과 total friendlist를 만든다.

        database.getReference().child("userlist").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull final DataSnapshot dataSnapshot, @Nullable String s) {
                //검색했을 때 나오는 사람들 목록
                if(!dataSnapshot.getKey().equals(auth.getCurrentUser().getUid()) && !myFriendList.contains(dataSnapshot.getKey())) {//나 자신과 이미 있는 친구 제외
                    Log.e(TAG, "onChildAdded: 내친구가 아닌 사람들의 모임입니다" );
                    UserDTO searchDTO = dataSnapshot.getValue(UserDTO.class);
                    friendItems.add(new FriendItem(searchDTO.name, searchDTO.profile_url, searchDTO.email, searchDTO.uid));
                    search_adapter.notifyDataSetChanged();
                    Log.e(TAG, "후보 "+friendItems.size() + searchDTO.name);

                    //함께아는 친구 구하기 + 1명 이상이면 추천목록에 넣기
                }else if(myFriendList.contains(dataSnapshot.getKey())){ //내 친구 목록에 있는 사람이라면
                    //final ArrayList<String> yourFriendList = dataSnapshot.child("friendList").getValue(type);
                    Log.e(TAG, "onChildAdded: 내 친구인 사람들의 모임입니다.");
                    UserDTO recomDTO = dataSnapshot.getValue(UserDTO.class);
                    Log.e(TAG, recomDTO.name+"씨의 친구 목록을 꺼내볼까요" );

                    for(String yourFriendUid : recomDTO.friendList){
                        if(!yourFriendUid.equals(auth.getCurrentUser().getUid()) && !myFriendList.contains(yourFriendUid)){//그 친구가 나 이거나 이미 내 친구라면 제외함
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
                    for (final String key : recommendMap.keySet()) {
                        //Log.e(TAG, "onChildChanged: 추천 리스트 만드는 중...... " + key + " 키값 확인합니다. ");
                        //Log.e(TAG, "userlist 사이즈가 설마..! " + userlist.size());
                        //UserDTO userDTO = dataSnapshot.getValue(UserDTO.class);
                        //Log.e(TAG, "onChildChanged: 추가될 userdto는?? " + userDTO.name);
                        FriendItem item;
                        for(UserDTO user : totalUserList){
                            if(user.uid.equals(key)){
                                item = new FriendItem(user.name, user.profile_url, user.email, user.uid);
                                if (!recommend_friends.contains(item)) {
                                    recommend_friends.add(item);
                                    recommend_adapter.notifyDataSetChanged();
                                    Log.e(TAG, " 추천 친구 리스트를 만들고 notify했습니다" + recommend_friends);
                                }
                            }
                        }
                    }
                }
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//                Log.e(TAG, "onChildChanged: is it called??!?!?!?!?!??!?!?");
//                for (final String key : recommendMap.keySet()) {
//                    //Log.e(TAG, "onChildChanged: 추천 리스트 만드는 중...... " + key + " 키값 확인합니다. ");
//                    //Log.e(TAG, "userlist 사이즈가 설마..! " + userlist.size());
//                    UserDTO userDTO = dataSnapshot.getValue(UserDTO.class);
//                    Log.e(TAG, "onChildChanged: 추가될 userdto는?? " + userDTO.name);
//                    FriendItem item = new FriendItem(userDTO.name, userDTO.profile_url, userDTO.email, userDTO.uid);
//                    if (!recommend_friends.contains(item)) {
//                        recommend_friends.add(item);
//                        recommend_adapter.notifyDataSetChanged();
//                        Log.e(TAG, " 추천 친구 리스트를 만들고 notify했습니다" + recommend_friends);
//                    }
//                }
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

        //추천목록 해시맵이 완성되었으므로 어댑터를 만들 수 있다.

        searchView = findViewById(R.id.search_view);
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
