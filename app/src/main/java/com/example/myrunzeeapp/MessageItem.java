package com.example.myrunzeeapp;

import java.io.Serializable;

public class MessageItem implements Serializable {

    MessageDTO msg;
    String sender_name;
    String sender_url;
    String contents;

    public MessageItem(String sender_name,String sender_url, String contents,MessageDTO msg){
        this.sender_name = sender_name;
        this.sender_url = sender_url;
        this.contents = contents;
        this.msg = msg;
    }
}
