package com.example.myrunzeeapp;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;

public class ChallengeDTO implements Serializable {
    String challenge_id;
    String cover_url;
    String title;
    double distance;
    String start_date;
    String end_date;
    //ArrayList<String> invitedMembers = new ArrayList<>();

    public ChallengeDTO(){

    }
    public ChallengeDTO(String challenge_id, String cover_url, String title, double distance, String start_date, String end_date){
        this.challenge_id = challenge_id;
        this.cover_url = cover_url;
        this.title =title;
        this.distance = distance;
        this.start_date = start_date;
        this.end_date = end_date;
        //this.invitedMembers = invitedMembers;
    }
}
