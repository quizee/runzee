package com.example.myrunzeeapp;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class RunningItem {

    String date;//날짜
    String title;//제목
    double km;//운동거리
    int pace_seconds;//페이스
    int runtime_seconds;//운동시간
    int calorie; //칼로리

    String filename; //사진이 저장된 위치

    ////////여기부터는 직접 운동해야 알 수 있는 부분///////
    int rest_count;//휴식 횟수
    int achievement; //달성률

    //삭제할 때 필요한 부분
    int position;
    boolean deleted = false;

    //편집할 때 필요한 부분
    boolean modified = false;
    boolean directRunning = false;

    //지도에 표시하기 위해 필요한 부분
    ArrayList<LatLng> trakerPoints = new ArrayList<>();


    public ArrayList<LatLng> getTrakerPoints() {
        return trakerPoints;
    }

    public void setTrakerPoints(ArrayList<LatLng> trakerPoints) {
        this.trakerPoints = trakerPoints;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public boolean isDirectRunning() {
        return directRunning;
    }

    public void setDirectRunning(boolean directRunning) {
        this.directRunning = directRunning;
    }

    public void setDeleted() {
        deleted = true;
    }
    public boolean getDeleted(){
        return deleted;
    }

    public  void setModified(boolean modified){ this.modified = modified;}
    public boolean getModified(){return modified;}

    public RunningItem(){

    }

    public RunningItem(String date, String title, double km, int runtime_seconds, int rest_count, int achievement){
        this.date = date;
        this.title = title;
        this.km = Math.round(km*100)/100.0; //소숫점 둘째자리까지만 저장
        this.runtime_seconds = runtime_seconds;
        this.pace_seconds = (int)((double)(runtime_seconds)/km); //키로미터당 몇초
        this.calorie = (int)(((double)runtime_seconds/60.0)*7);//1분에 7칼로리
        this.rest_count = rest_count;
        this.achievement = achievement;
    }

    //그냥 입력했을 때
    public RunningItem(String date, String title, double km, int runtime_seconds){
        this.date = date;
        this.title = title;
        this.km = km;
        this.runtime_seconds = runtime_seconds;
        this.pace_seconds = (int)((double)(runtime_seconds)/km); //키로미터당 몇초
        this.calorie = (int)(((double)runtime_seconds/60.0)*7);//1분에 7칼로리
        this.rest_count = 100;
        this.achievement = 0;// 나올 수 없는 수들로 초기화시켜서 today화면 뿌려줄 때 검사한다.
    }


    public int getRest_count() {
        return rest_count;
    }

    public void setRest_count(int rest_count) {
        this.rest_count = rest_count;
    }

    public int getCalorie() {
        return calorie;
    }

    public void setCalorie(int calorie) {
        this.calorie = calorie;
    }

    public int getAchievement() {
        return achievement;
    }

    public void setAchievement(int achievement) {
        this.achievement = achievement;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getKm() {
        return km;
    }

    public void setKm(double km) {
        this.km = km;
    }

    public int getPace_seconds() {
        return pace_seconds;
    }

    public void setPace_seconds(int pace_seconds) {
        this.pace_seconds = pace_seconds;
    }

    public int getRuntime_seconds() {
        return runtime_seconds;
    }

    public void setRuntime_seconds(int runtime_seconds) {
        this.runtime_seconds = runtime_seconds;
    }
}
