package com.example.myrunzeeapp;

import java.io.Serializable;

public class PhysicInfo implements Serializable {
    String gender;
    int height;
    int weight;
    boolean edit_by_user = false;

    public PhysicInfo(){

    }

    public PhysicInfo(String gender){//기본값 사용
        if(gender.equals("female")){
            this.gender = "female";
            this.height = 161;
            this.weight = 56;
        }else if(gender.equals("male")){
            this.gender = "male";
            this.height = 173;
            this.weight = 68;
        }
        edit_by_user = false;
    }

    public PhysicInfo(String gender, int height, int weight){
        this.gender = gender;
        this.height = height;
        this.weight = weight;
        edit_by_user = true;
    }

}
