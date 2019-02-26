package com.zhihui.imeeting.cloudmeeting_hd.controller;

public class MyURL {
    private final static String URL="http://39.106.56.132:8080/IMeeting";
    public String mangerLogin(){
        return URL+"/mangerLogin";
    }
    public String compare(){
        return URL+"/face/compare";
    }
    public String selectAll(){
        return URL+"/meetRoom/selectAll";
    }
    public String oneRoomReserver(){
        return URL+"/meeting/oneRoomReserver";
    }
}
