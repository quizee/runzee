package com.example.myrunzeeapp;

public class FeedItem {
    String info_url;
    String img_url;
    public FeedItem(String info_url, String img_url){
        this.info_url = info_url;
        this.img_url = img_url;
    }

    public String getInfo_url() {
        return info_url;
    }

    public void setInfo_url(String info_url) {
        this.info_url = info_url;
    }

    public String getImg_url() {
        return img_url;
    }

    public void setImg_url(String img_url) {
        this.img_url = img_url;
    }
}
