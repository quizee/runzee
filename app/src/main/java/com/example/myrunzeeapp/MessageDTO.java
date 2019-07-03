package com.example.myrunzeeapp;

import java.io.Serializable;

public class MessageDTO implements Serializable {
    String sender_uid;
    String message_uid;
    long when_made;
    String message_type;
    String download_url;
    /*
    *  친구 요청 == request
    *  친구 수락 == accept
    *  응원메세지 == cheer
    * */
    public MessageDTO(){

    }

    public MessageDTO(String sender_uid, String message_uid, String message_type){
        this.sender_uid = sender_uid;
        this.message_uid = message_uid;
        this.message_type = message_type;
        this.when_made = System.currentTimeMillis();
    }
}
