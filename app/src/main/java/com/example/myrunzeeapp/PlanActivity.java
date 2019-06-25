package com.example.myrunzeeapp;

import android.app.Activity;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class PlanActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan);

        TextView first_planner = (TextView)findViewById(R.id.first_planner);
        SharedPreferences pref = getSharedPreferences("pref", Activity.MODE_PRIVATE);
        String user_name = pref.getString("name_info","");
        first_planner.setText("안녕하세요"+user_name+"님, \n입력하시는 정보로 나만의 맞춤형\n 러닝 플랜을 생성할 수 있습니다.");

    }
    //내 플랜 만들기 누르면 그 레디 액티비티로 넘어가게 됨
    //단 탭은 오른쪽에 와있을 것임
    //이거 애니메이션으로 할지 뭘로할지 몰라서 일단 나두는 액티비티임.
    //동적으로 생성해 줄 것이 많은 액티비티
    //다 입력하고 나면 내 플랜 만들기라는 버튼이 추가로 '생기게'만들 예정


    //만약 처음이 아니라면 이 화면은 지나간다.
}
