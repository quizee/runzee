package com.example.myrunzeeapp;

import java.io.Serializable;

public class ChallengeItem implements Serializable {
    UserDTO userDTO;
    double userDistance;

    public ChallengeItem(){

    }

    public ChallengeItem(UserDTO userDTO, double userDistance){
        this.userDTO = userDTO;
        this.userDistance = userDistance;
    }

    @Override
    public boolean equals(Object obj) {
        ChallengeItem challengeItem = (ChallengeItem) obj;
        //두 challenge item이 같다는 것은 userDTO의 uid끼리 같다는 것이다.
        if (obj instanceof ChallengeItem) {
            if(challengeItem.userDTO.uid.equals(this.userDTO.uid)){
                return true;
            }
        }
        return false;
    }
}
