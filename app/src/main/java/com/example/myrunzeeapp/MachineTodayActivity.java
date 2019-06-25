package com.example.myrunzeeapp;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import javax.crypto.Mac;

public class MachineTodayActivity extends AppCompatActivity {

    String today_consult;

    TextView calorie_this;
    TextView time_this;
    TextView achievement_thismuch;
    TextView achievement_is;
    TextView pace_this;
    TextView title;
    TextView consulting;
    TextView today_distance;
    TextView consulting_is;

    //사진 관련된 부분 다시 imageview 로 바꿈
    ImageView certification;
    String filename;

    private static String TAG = "MachineTodayActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_machine_today);

        pace_this = findViewById(R.id.pace_this);
        calorie_this = findViewById(R.id.calorie_this);
        time_this = findViewById(R.id.time_this);
        achievement_thismuch = findViewById(R.id.achievement_thismuch);
        achievement_is = findViewById(R.id.achievement_is);
        title = findViewById(R.id.title);
        consulting = findViewById(R.id.consulting);
        certification = findViewById(R.id.certification);
        today_distance = findViewById(R.id.today_distance);
        consulting_is = findViewById(R.id.consulting_is);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(ReadyActivity.runningItem.getDate());
        setSupportActionBar(toolbar);

        //bottom navigation bar
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.go_takephoto:
                        Intent intent = new Intent(MachineTodayActivity.this, TakePhotoActivity.class);
                        intent.putExtra("howmuchTIme",ReadyActivity.runningItem.getRuntime_seconds());
                        startActivity(intent);//takePhotoActivity로 넘어가서 사진 촬영
                        break;
                    case R.id.go_record:
                        Intent intent1 = new Intent(MachineTodayActivity.this,RecordActivity.class);
                        if(RecordActivity.justWatching&&!ReadyActivity.runningItem.getModified()){
                            //삭제를 시킬 때는 이 버튼을 누를 일이 없다.단 수정을 할 때는 이 버튼을 누르기 때문에 예외처리
                            ReadyActivity.runningItem = null;
                        }
                        intent1.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        startActivity(intent1);
                        finish();
                        break;

                }
                return true;
            }
        });
        certification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pictureShow();
            }
        });

    }

    //사진 확대해서 보기 다이어로그
    public void pictureShow(){
        final Dialog d = new Dialog(MachineTodayActivity.this);
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
        d.setContentView(R.layout.one_picture_dialog);

        final ImageView one_picture = d.findViewById(R.id.one_picture);
        final ImageView cancel = d.findViewById(R.id.goout);
        final Button share_picture = d.findViewById(R.id.share_picture);
        final Button edit_picture = d.findViewById(R.id.edit_picture);

        Bitmap bm = BitmapFactory.decodeFile(filename);
        if(filename!=null) {
            //one_picture.setImageBitmap(bm);
            Glide.with(one_picture.getContext()).load(bm).into(one_picture);
        }

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.dismiss();
            }
        });
        share_picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //친구와 공유하는 부분은 firebase쓰고 나서
            }
        });
        edit_picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MachineTodayActivity.this, TakePhotoActivity.class);
                intent.putExtra("howmuchTIme",ReadyActivity.runningItem.getRuntime_seconds());
                intent.putExtra("editing_picture",true);
                startActivity(intent);//takePhotoActivity로 넘어가서 사진 촬영
                d.dismiss();
            }
        });

        d.show();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){
            if(requestCode==3){
                Log.e(TAG, "onActivityResult: 수정된 아이템을 받는다");
                ReadyActivity.runningItem.setModified(true);//변경되었음
            }
        }
    }
    //툴바 관련된 사항들
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.delete:
                //delete
                Intent intent = new Intent(MachineTodayActivity.this,ReallyDeleteActivity.class);
                startActivity(intent);
                return true;
            case R.id.modify:
                //modify
                Intent intent1 = new Intent(MachineTodayActivity.this, RunEditActivity.class);
                startActivityForResult(intent1, 3);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //거리 추가함
        double distance = ReadyActivity.runningItem.getKm();
        int total_rest = ReadyActivity.runningItem.getRest_count();
        int time_r = ReadyActivity.runningItem.getRuntime_seconds();
        int achievement = ReadyActivity.runningItem.getAchievement();
        int calorie = ReadyActivity.runningItem.getCalorie();
        String default_title = ReadyActivity.runningItem.getTitle();
        int TimeperKm= ReadyActivity.runningItem.getPace_seconds(); //키로미터당 몇초 걸리는지 나옴
        String pace_string = TimeperKm/60+"\'"+TimeperKm%60+"\'\'";

        filename = ReadyActivity.runningItem.getFilename();
        Bitmap bm = BitmapFactory.decodeFile(filename);

        if(filename!=null) {
            Glide.with(certification.getContext()).load(bm).into(certification);
        }
        //수정되더라도 자연스럽게 반영되도록 resume에 넣었다

        if(total_rest == 0){
            today_consult = "가벼운 러닝";
        }else if(total_rest == 1){
            today_consult = "회복 러닝";
        }else if(total_rest == 2){
            today_consult = "스피드 러닝";
        }else{
            today_consult = "하드 러닝";
        }

        if(total_rest == 100){
            consulting_is.setText("");
            consulting.setText("");//저장된 값이 없다고 간주되면 빈칸으로 나둔다
        }else{
            consulting.setText(today_consult+" (총 휴식 횟수 "+total_rest+"회)");
        }

        String time_set = time_r/60+"분 "+time_r%60+"초";
        time_this.setText(time_set);
        calorie_this.setText(String.valueOf(calorie));

        if(achievement>0){
            achievement_thismuch.setText(String.valueOf(achievement)+"%");
        }else{
            achievement_thismuch.setText("");
            achievement_is.setText("");//빈칸으로 나둔다.
        }

        pace_this.setText(pace_string);
        title.setText("제목: "+default_title);
        today_distance.setText(String.format("%.2f",distance)+" km");

        Log.e(TAG, "onResume: 아이템이 "+ReadyActivity.runningItem );
    }

}
