package com.example.myrunzeeapp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class UserDTO implements Serializable {

    String email;
    String name;
    String uid;
    String profileUrl;
    PhysicInfo physicInfo;
    String fcmToken;
    ArrayList<String> friendList = new ArrayList<>();

    public UserDTO(){

    }

    public UserDTO(String uid, String email, String name){//회원가입할 때 이름과 이메일만 생성되고 프로필사진과 신체정보는 디폴트
        this.uid = uid;
        this.email = email;
        this.name = name;
        this.physicInfo = new PhysicInfo("female");//기본 값
        this.profileUrl = null;
    }
    public Map<String, Object> toMap() {
        //5가지 정보를 맵으로 만든다.
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("name", name);
        result.put("email", email);
        result.put("profileUrl",profileUrl);
        HashMap<String,Object> physic_result = new HashMap<>();
        physic_result.put("gender",physicInfo.gender);
        physic_result.put("height",physicInfo.height);
        physic_result.put("weight",physicInfo.weight);
        result.put("physicInfo",physic_result);
        return result;
    }

}
