package com.example.myrunzeeapp;

public class FriendItem {
    String name;
    String img_url;
    String email;
    String uid;

    public FriendItem(String name, String img_url, String email, String uid){
        this.name = name;
        this.img_url =img_url;
        this.email = email;
        this.uid = uid;
    }

    @Override
    public boolean equals(Object obj) {
        FriendItem friendItem = (FriendItem) obj;
        if (obj instanceof FriendItem) {
            if(friendItem.uid.equals(uid)&& friendItem.email.equals(email) && friendItem.img_url.equals(img_url) && friendItem.name.equals(name)){
                return true;
            }
        }
        return false;
    }
}
