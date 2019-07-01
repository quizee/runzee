package com.example.myrunzeeapp;

public class BoardItem {
    String profile_url;
    String user_name;
    double user_distance;

    public BoardItem(String user_name, String profile_url, double user_distance){
        this.user_name = user_name;
        this.profile_url = profile_url;
        this.user_distance = user_distance;
    }
}
