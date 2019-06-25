package com.example.myrunzeeapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MyChallengeActivity extends MenuActivity {

    Button make_challenge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_challenge);
        make_challenge = findViewById(R.id.make_challenge);

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
