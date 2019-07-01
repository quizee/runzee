package com.example.myrunzeeapp;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    long pressedTime = 0;

    private void login_auto(String email, String password){
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();

                            updateUI_auto(user);
                        } else {
                            updateUI_auto(null);
                        }
                    }
                });
    }

    private void updateUI_auto(FirebaseUser user){
        if(user!=null){
            //데이터베이스에서 이름꺼내오면 조케따
            Toast.makeText(MainActivity.this, user.getEmail()+" 자동로그인 입니다.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this, ReadyActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
            finish();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences auto = getSharedPreferences("auto", Activity.MODE_PRIVATE);

        String LoginId = auto.getString("auto_email","");
        String LoginPwd = auto.getString("auto_password","");
        Boolean LoginAuto = auto.getBoolean("is_auto",false);
        //자동 로그인을 위한 정보들
        mAuth = FirebaseAuth.getInstance();

        if(LoginAuto){
            login_auto(LoginId,LoginPwd);
        }else{
            //원래대로 로그인
            Button register = findViewById(R.id.register);
            register.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(
                            getApplicationContext(),
                            RegisterActivity.class);
                    startActivity(intent);
                }
            });
            Button login =  findViewById(R.id.login);
            login.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(
                            getApplicationContext(),
                            LoginActivity.class);
                    startActivity(intent);
                }
            });
        }
    }
    public void onBackPressed() {
        if ( pressedTime == 0 ) {
            Toast.makeText(this, " 한 번 더 누르면 종료됩니다." , Toast.LENGTH_LONG).show();
            pressedTime = System.currentTimeMillis();
        }
        else {
            int seconds = (int) (System.currentTimeMillis() - pressedTime);
            if ( seconds > 2000 ) {
                Toast.makeText(this, " 한 번 더 누르면 종료됩니다." , Toast.LENGTH_LONG).show();
                pressedTime = 0 ;
            }
            else {
                //super.onBackPressed();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    finishAffinity();
                }
                System.runFinalizersOnExit(true);
                System.exit(0);
            }
        }
    }

}
