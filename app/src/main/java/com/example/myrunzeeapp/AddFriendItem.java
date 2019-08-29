package com.example.myrunzeeapp;

public class AddFriendItem {

    String profileUrl;
    String username;
    String uid;
    boolean isInvited;
    boolean alreadyInvited = false;

    public AddFriendItem(){

    }

    public AddFriendItem(String uid, String username, String profileUrl, boolean isInvited){
        this.uid = uid;
        this.username = username;
        this.profileUrl = profileUrl;
        this.isInvited = isInvited;
    }
}
