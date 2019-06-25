package com.example.myrunzeeapp;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;

public class LoginActivity extends AppCompatActivity{

    EditText email_login;
    EditText password_login;
    String name_info;
    CheckBox auto_login;
    static HashMap<String, String> my_info;
    private FirebaseAuth mAuth;

    @Override
    protected void onPause() {
        super.onPause();
    }

    public void autoLogin(String email_input, String password_input){
        //다음번 자동로그인을 위해 sharedPreference에 로그인 저장값을 넣어놓는다.
        SharedPreferences auto = getSharedPreferences("auto", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = auto.edit();
        editor.putString("auto_email",email_input);
        editor.putString("auto_password",password_input);
        editor.putBoolean("is_auto",true);
        editor.apply();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button login_btn = findViewById(R.id.login_btn);
        email_login = findViewById(R.id.email_login);
        password_login = findViewById(R.id.passward_login);
        auto_login = findViewById(R.id.auto_login);
        email_login.setText(getIntent().getStringExtra("email_login"));
        password_login.setText(getIntent().getStringExtra("password_login"));
        mAuth = FirebaseAuth.getInstance();

        login_btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                String email_input = email_login.getText().toString();
                String password_input = password_login.getText().toString();
                login(email_input, password_input);

//                SharedPreferences pref = getSharedPreferences("pref", Activity.MODE_PRIVATE);
//                if ((pref != null) && (pref.contains(email_input))) {
//                    json_info = pref.getString(email_input,"");// 이 이메일에 해당하는 정보를 가져온다
//                    Type type = new TypeToken<HashMap<String,String>>() {
//                    }.getType();
//                    my_info =  gson.fromJson(json_info,type);
//                    Log.e("여기야 여기!!!!!!!!", "이메일: "+ email_input);
//                    Log.e("여기야 여기!!!!!!!!!!!!!!", "그 이메일로 부터 얻은 값: "+ json_info);
//                    password_info = my_info.get("password");
//                    name_info = my_info.get("name");
//                    LoginActivity.my_info = my_info;
//                    LoginActivity.my_info.put("email",email_input);
//                    SharedPreferences runList = getSharedPreferences(email_input,Activity.MODE_PRIVATE);
//                    //Log.e("여기야 여기!!!", "onClick: ", );
//                }
            }
        });

        TextView gotoRegister =  (TextView) findViewById(R.id.gotoRegister);
        gotoRegister.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(
                        getApplicationContext(),
                        RegisterActivity.class);
                startActivity(intent);
                finish();//액티비티를 없애버린다
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void login(String email, String password){
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            updateUI(null);
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthInvalidUserException e) {
                                Toast.makeText(LoginActivity.this,"존재하지 않는 아이디입니다." ,Toast.LENGTH_SHORT).show();
                            } catch (FirebaseAuthInvalidCredentialsException e) {
                                Toast.makeText(LoginActivity.this,"아이디와 비밀번호를 확인해주세요." ,Toast.LENGTH_SHORT).show();
                            } catch (FirebaseNetworkException e) {
                                Toast.makeText(LoginActivity.this,"네트워크 상태가 좋지 않습니다." ,Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                Toast.makeText(LoginActivity.this,"Exception" ,Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }
    private void updateUI(FirebaseUser user){
        if(user != null){
            String email_input = email_login.getText().toString();
            String password_input = password_login.getText().toString();
            if(auto_login.isChecked()) {
                autoLogin(email_input, password_input);
            }
            Intent intent = new Intent(LoginActivity.this, ReadyActivity.class);
            startActivity(intent);
            finish();
        }
    }


/*
    public JsonObject restoredata(String emailKey) {
        SharedPreferences pref = getSharedPreferences("pref", Activity.MODE_PRIVATE);
        if ((pref != null) && (pref.contains("email_info"))) {
            String info = pref.getString(emailKey, "");//그 이메일이 있으면 스트링 불러옴
            Log.e("로그인할 때 잘 넘어오나?", info);
            JsonParser jsonParser = new JsonParser();//스트링을 다시 객체로 만든다.
            JsonObject jsonObject = (JsonObject) jsonParser.parse(info);
            return jsonObject;
        }
        return null;
    }
*/
}
