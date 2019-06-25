package com.example.myrunzeeapp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_club);
        setToolbarMenu();

        //추천 친구 세팅
        crew_recycler = findViewById(R.id.crew_recycler);
        recommend_adapter = new FriendListAdapter(ClubActivity.this,recommend_friends);
        layoutManager_recommend = new LinearLayoutManager(getApplicationContext(),LinearLayoutManager.VERTICAL,false);
        layoutManager_search = new LinearLayoutManager(getApplicationContext(),LinearLayoutManager.VERTICAL,false);
        crew_recycler.setLayoutManager(layoutManager_recommend);
        crew_recycler.setAdapter(recommend_adapter);

        //검색 친구 세팅
        friend_recycler = findViewById(R.id.friend_recycler);
        search_adapter = new FriendListAdapter(ClubActivity.this, defaultItems);//처음에는 아무것도 안보이게 !
        friend_recycler.setAdapter(search_adapter);
        friend_recycler.setLayoutManager(layoutManager_search);

        //검색 툴바 세팅
        Toolbar toolbar = findViewById(R.id.search_bar);
        setSupportActionBar(toolbar);//액션바를 없애고 이거로 쓰겠다.
        getSupportActionBar().setTitle("친구 이메일 또는 이름 검색");
        toolbar.setTitleTextColor(Color.BLACK);

        //firebase 세팅
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        //처음에 우선 사람들 목록을 다 받아온다
        database.getReference().child("userlist").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(!dataSnapshot.getKey().equals(auth.getCurrentUser().getUid())) {//나 자신은 빼고 친구에 추가
                    Log.e(TAG, "onChildAdded: " + dataSnapshot);
                    String name = dataSnapshot.child("name").getValue(String.class);
                    String url = dataSnapshot.child("profile_url").getValue(String.class);
                    String email = dataSnapshot.child("email").getValue(String.class);
                    String uid = dataSnapshot.child("uid").getValue(String.class);
                    friendItems.add(new FriendItem(name, url, email, uid));
                    //search_adapter.notifyDataSetChanged();
                    Log.e(TAG, "onChildAdded: " + friendItems.size());
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
        searchView = findViewById(R.id.search_view);
        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {

            }

            @Override
            public void onSearchViewClosed() {
                FriendListAdapter adapter1 = new FriendListAdapter( ClubActivity.this, defaultItems);
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
                    FriendListAdapter adapter1 = new FriendListAdapter(ClubActivity.this, found);
                    Log.e(TAG, "onQueryTextChange: "+found.size());
                    friend_recycler.setAdapter(adapter1);
                }else{
                    //아무것도 안쳤을 때
                    FriendListAdapter adapter1 = new FriendListAdapter(ClubActivity.this,defaultItems);
                    friend_recycler.setAdapter(adapter1);
                }
                return true;
            }
        });

        setToolbarMenu();
        setTabLayout(0);
        group_lt.setImageResource(R.drawable.ic_group_black_24dp);
    }
    //러닝 크루 관련된 피드를 크롤링한다.


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
