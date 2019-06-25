package com.example.myrunzeeapp;

public class PictureData {

    int picture_id;
    String what_level;
    double what_kilometer;

    public PictureData(String what_level, double what_kilometer, int picture_id){
        this.picture_id = picture_id;
        this.what_kilometer = what_kilometer;
        this.what_level = what_level;
    }

    public int getPicture_id() {
        return picture_id;
    }

    public void setPicture_id(int picture_id) {
        this.picture_id = picture_id;
    }

    public String getWhat_level() {
        return what_level;
    }

    public void setWhat_level(String what_level) {
        this.what_level = what_level;
    }

    public double getWhat_kilometer() {
        return what_kilometer;
    }

    public void setWhat_kilometer(double what_kilometer) {
        this.what_kilometer = what_kilometer;
    }
}
