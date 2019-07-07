package com.example.myrunzeeapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Person;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity{

    TextInputLayout textInputEmail;
    TextInputLayout textInputPassword;
    TextInputLayout textInputUserName;
    TextInputLayout textInputBirth;

    TextInputEditText email_register;
    TextInputEditText password_register;
    TextInputEditText name_register;
    CheckBox checkBox;

    private FirebaseAuth mAuth;
    private FirebaseDatabase database;

    final Pattern PASSWORD_PATTERN =
            Pattern.compile(
                    "^"+
                            "(?=.*[0-9])" + //at least 1 digit
                            "(?=.*[a-z])"+ //at least 1 lower case
                            "(?=.*[A-Z])"+ //at least 1 upper case
                            "(?=\\S+$)" + //no whitespace
                            ".{8,}"+//at least 8 characters
                            "$");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        textInputEmail = findViewById(R.id.text_input_email);
        textInputPassword = findViewById(R.id.text_input_password);
        textInputBirth = findViewById(R.id.text_input_birth);
        textInputUserName = findViewById(R.id.text_input_username);

        email_register = findViewById(R.id.email_register);
        password_register = findViewById(R.id.password_register);
        name_register = findViewById(R.id.name_register);
        checkBox = findViewById(R.id.checkBox);

        Button register_btn = findViewById(R.id.register_btn);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        database= FirebaseDatabase.getInstance();


        //이메일 리스너
        email_register.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateEmail();
            }

            @Override
            public void afterTextChanged(Editable s) {
                validateEmail();
            }
        });

        //비밀번호 리스너
        password_register.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validatePassword();
            }
            @Override
            public void afterTextChanged(Editable s) {
                validatePassword();
            }
        });

        //사용자이름 리스너
        name_register.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateUserName();
            }

            @Override
            public void afterTextChanged(Editable s) {
                validateUserName();
            }
        });

        //가입하기 창으로 가기 리스너
        TextView gotoLogin = (TextView) findViewById(R.id.gotoLogin);
        gotoLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(
                        getApplicationContext(),
                        LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        //계정 만들기 리스너 여기서는 화면 전환만 함.
        // 정보를 등록하는 부분은 pause에 넣어야하나 싶음
        register_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email_reg = email_register.getText().toString();
                String password_reg = password_register.getText().toString();
                String name_reg = name_register.getText().toString();
                createUser(email_reg,password_reg, name_reg);
            }
        });
    }

    //유효한 폼인지 검사하고 계정을 만든다
    private void createUser(final String email, final String password, final String name){
        if(!validateForm()){
            Toast.makeText(RegisterActivity.this,"최소요구사항을 확인해주십시오",Toast.LENGTH_LONG).show();
            return;
        }
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(RegisterActivity.this, name_register.getText().toString()+"님 가입을 축하합니다",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(user);
                            UserDTO userDTO = new UserDTO(user.getUid(), email, name_register.getText().toString().trim());
                            //MessageDTO msgDTO = new MessageDTO(null,"welcome");
                            database.getReference().child("userlist").child(user.getUid()).setValue(userDTO);
                            database.getReference().child("runninglist").child(user.getUid()).child("recordlist").setValue(new ArrayList<RunningItem>());
                            double accum_distance = 0.0;
                            database.getReference().child("runninglist").child(user.getUid()).child("accumulate").setValue(accum_distance);
                            database.getReference().child("runninglist").child(user.getUid()).child("belt").setValue("white");

                            SharedPreferences runListPref = getSharedPreferences(email,Activity.MODE_PRIVATE);
                            SharedPreferences.Editor edit = runListPref.edit();
                            edit.putString("my_name",name);
                            edit.apply();
                            //database.getReference().child("messages").child(user.getUid()).setValue(msgDTO);
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            try {
                                throw task.getException();
                            }catch (FirebaseAuthUserCollisionException e){
                                Toast.makeText(RegisterActivity.this, "이미 존재하는 계정입니다",
                                        Toast.LENGTH_SHORT).show();
                                Log.e("register", "onComplete: " + "실패!!!!");
                                updateUI(null);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
    }
    //로그인 페이지로 넘어간다.
    private void updateUI(FirebaseUser user){
        if(user != null){
            String email_reg = user.getEmail();
            String password_reg = password_register.getText().toString();
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            intent.putExtra("email_login",email_reg);
            intent.putExtra("password_login",password_reg);
            startActivity(intent);
        }
    }
    private boolean validateForm(){
        boolean valid = true;
        if(validateEmail()&& validatePassword()&& validateUserName()&&checkBox.isChecked()){
            valid = true;
        }else{
            valid = false;
        }
        return valid;
    }
    //validation method
    public boolean validateEmail() {
        textInputEmail.setErrorEnabled(true);

        if (email_register.getText().toString().isEmpty()) {
            textInputEmail.setError("이메일을 입력해주세요.");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email_register.getText().toString()).matches()) {
            textInputEmail.setError("이메일 형식이 아닙니다.");
            return false;
        } else {
            textInputEmail.setError(null);
            //textInputEmail.setErrorEnabled(false);
            return true;
        }

    }

    public boolean validatePassword() {
        textInputPassword.setErrorEnabled(true);

        if (password_register.getText().toString().isEmpty()) {
            textInputPassword.setError("비밀번호를 입력해주세요.");
            return false;
        } else if (!PASSWORD_PATTERN.matcher(password_register.getText().toString()).matches()) {
            textInputPassword.setError("대소문자 1개 이상 / 숫자 1개 이상 / 최소 8자 이상");
            return false;
        } else {
            textInputPassword.setError(null);
            //textInputPassword.setErrorEnabled(false);
            return true;
        }

    }

    public boolean validateUserName() {
        textInputUserName.setErrorEnabled(true);

        if ( name_register.getText().toString().isEmpty()) {
            textInputUserName.setError("사용하실 이름을 입력해주세요.");
            return false;
        } else if ( name_register.getText().toString().length() > 15) {
            textInputUserName.setError("15자 이내로 입력해주세요.");
            return false;
        } else {
            textInputUserName.setError(null);
            //textInputUserName.setErrorEnabled(false);
            return true;
        }
    }

    protected void onPause(){
        super.onPause();
        //saveState();
    }

//    protected boolean validateDuplication(String email){//중복되면 false를 반환
//        SharedPreferences pref = getSharedPreferences("pref",Activity.MODE_PRIVATE);
//        boolean isDuplicate = true;
//        if(pref != null){
//            if(pref.contains(email)){
//                isDuplicate = false;
//            }else{
//                isDuplicate = true;
//            }
//        }
//        return isDuplicate;
//    }

//    protected  void saveState(){
//        //모든 회원정보가 모이는 곳
//        SharedPreferences pref = getSharedPreferences("pref",Activity.MODE_PRIVATE);
//        SharedPreferences.Editor editor = pref.edit();//edit 메소드 제공함
//
//        String email_info = email_register.getText().toString();//정보들을 스트링으로 받음
//        String name_info = name_register.getText().toString();
//        String password_info = password_register.getText().toString();
//        String birth_info = birth_register.getText().toString();
//
//        HashMap<String,String> UserInfoMap = new HashMap<String,String>(); //이메일을 제외한 나머지 정보들을 리스트에 넣는다.
//        UserInfoMap.put("name", name_info);
//        UserInfoMap.put("password",password_info);
//        UserInfoMap.put("birth",birth_info);
//
//        Gson gson = new Gson();
//        String UserInfoToString = gson.toJson(UserInfoMap);//에디터에 스트링으로 넣기 위해 gson으로 변환
//        editor.putString(email_info,UserInfoToString);//이메일을 키로 하여 정보를 에디터에 저장
///*
//        editor.putString("email_info",email_info);
//        editor.putString("name_info",name_info);
//        editor.putString("password_info",password_info);
//        editor.putString("birth_info",birth_info);*/
//
//        editor.commit();//commit 을 호출해야 실제로 저장
//    }

}
