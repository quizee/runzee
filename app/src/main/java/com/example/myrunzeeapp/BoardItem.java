package com.example.myrunzeeapp;

public class BoardItem {
    String profileUrl;
    String user_name;
    double user_distance;

    public BoardItem(String user_name, String profileUrl, double user_distance){
        this.user_name = user_name;
        this.profileUrl = profileUrl;
        this.user_distance = user_distance;
    }
}
