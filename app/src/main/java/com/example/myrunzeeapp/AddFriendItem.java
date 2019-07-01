package com.example.myrunzeeapp;

public class AddFriendItem {

    String profile_url;
    String username;
    String uid;
    boolean isInvited;
    boolean already_invited = false;

    public AddFriendItem(){

    }

    public AddFriendItem(String uid, String username, String profile_url, boolean isInvited){
        this.uid = uid;
        this.username = username;
        this.profile_url = profile_url;
        this.isInvited = isInvited;
    }
}
