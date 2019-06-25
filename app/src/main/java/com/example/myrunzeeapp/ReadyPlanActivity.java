package com.example.myrunzeeapp;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class ReadyPlanActivity extends MenuActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ready_plan);

        setToolbarMenu();
        setTabLayout(1);
        startrun_lt.setImageResource(R.drawable.ic_directions_run_black_24dp);
    }

    public void changeActivity(int index) {
        switch(index) {
            case 0:
                Intent intent = new Intent(ReadyPlanActivity.this, ReadyActivity.class);
               // intent.putExtra("selected_tab",index);
                startActivity(intent);
                ReadyActivity.ready_index = 0;
                break;
            default:
                break;
        }
    }
}
