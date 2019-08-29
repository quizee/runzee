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

    String todayConsult;

    TextView calorieThis;
    TextView timeThis;
    TextView achievementThismuch;
    TextView achievementIs;
    TextView paceThis;
    TextView title;
    TextView consulting;
    TextView todayDistance;
    TextView consultingIs;
    Toolbar toolbar;

    //사진 관련된 부분 다시 imageview 로 바꿈
    ImageView certification;
    String filename;

    private static String TAG = "MachineTodayActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_machine_today);

        paceThis = findViewById(R.id.paceThis);
        calorieThis = findViewById(R.id.calorieThis);
        timeThis = findViewById(R.id.timeThis);
        achievementThismuch = findViewById(R.id.achievementThismuch);
        achievementIs = findViewById(R.id.achievementIs);
        title = findViewById(R.id.title);
        consulting = findViewById(R.id.consulting);
        certification = findViewById(R.id.certification);
        todayDistance = findViewById(R.id.todayDistance);
        consultingIs = findViewById(R.id.consultingIs);

        toolbar = findViewById(R.id.toolbar);
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
        d.setContentView(R.layout.onePicture_dialog);

        final ImageView onePicture = d.findViewById(R.id.onePicture);
        final ImageView cancel = d.findViewById(R.id.goout);
        //final Button share_picture = d.findViewById(R.id.share_picture);
        final Button editPicture = d.findViewById(R.id.editPicture);

        Bitmap bm = BitmapFactory.decodeFile(filename);
        if(filename!=null) {
            //onePicture.setImageBitmap(bm);
            Glide.with(onePicture.getContext()).load(bm).into(onePicture);
        }

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.dismiss();
            }
        });
//        share_picture.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                //친구와 공유하는 부분은 firebase쓰고 나서
//            }
//        });
        editPicture.setOnClickListener(new View.OnClickListener() {
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
                intent1.putExtra("date_edit",ReadyActivity.runningItem.getDate());
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
        int totalRest = ReadyActivity.runningItem.getRest_count();
        int timeR = ReadyActivity.runningItem.getRuntime_seconds();
        int achievement = ReadyActivity.runningItem.getAchievement();
        int calorie = ReadyActivity.runningItem.getCalorie();
        String default_title = ReadyActivity.runningItem.getTitle();
        int TimeperKm= ReadyActivity.runningItem.getPace_seconds(); //키로미터당 몇초 걸리는지 나옴
        String paceString = TimeperKm/60+"\'"+TimeperKm%60+"\'\'";

        filename = ReadyActivity.runningItem.getFilename();
        Bitmap bm = BitmapFactory.decodeFile(filename);

        if(filename!=null) {
            Glide.with(certification.getContext()).load(bm).into(certification);
        }
        //수정되더라도 자연스럽게 반영되도록 resume에 넣었다

        if(totalRest == 0){
            todayConsult = "가벼운 러닝";
        }else if(totalRest == 1){
            todayConsult = "회복 러닝";
        }else if(totalRest == 2){
            todayConsult = "스피드 러닝";
        }else{
            todayConsult = "하드 러닝";
        }

        if(totalRest == 100){
            consultingIs.setText("");
            consulting.setText("");//저장된 값이 없다고 간주되면 빈칸으로 나둔다
        }else{
            consulting.setText(todayConsult+" (총 휴식 횟수 "+totalRest+"회)");
        }

        String timeSet = timeR/60+"분 "+timeR%60+"초";
        timeThis.setText(timeSet);
        calorieThis.setText(String.valueOf(calorie));

        if(achievement>0){
            achievementThismuch.setText(String.valueOf(achievement)+"%");
        }else{
            achievementThismuch.setText("");
            achievementIs.setText("");//빈칸으로 나둔다.
        }
        toolbar.setTitle(ReadyActivity.runningItem.getDate());

        paceThis.setText(paceString);
        title.setText("제목: "+default_title);
        todayDistance.setText(String.format("%.2f",distance)+" km");

        Log.e(TAG, "onResume: 아이템이 "+ReadyActivity.runningItem );
    }

}
