package com.vrnitsolution.healthapp.DoctorUI.model;

import java.util.Map;

public class ProfileForMessage {
    private Map<String, String> currentCoordinates;
    private String email;
    private String messageToken;
    private String phoneNo;
    private String profileUrl;
    private String uid;
    private String username;


    public ProfileForMessage() {
    }

    public ProfileForMessage(Map<String, String> currentCoordinates, String email, String messageToken, String phoneNo, String profileUrl, String uid, String username) {
        this.currentCoordinates = currentCoordinates;
        this.email = email;
        this.messageToken = messageToken;
        this.phoneNo = phoneNo;
        this.profileUrl = profileUrl;
        this.uid = uid;
        this.username = username;
    }

    public Map<String, String> getCurrentCoordinates() {
        return currentCoordinates;
    }

    public String getEmail() {
        return email;
    }

    public String getMessageToken() {
        return messageToken;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public String getUid() {
        return uid;
    }

    public String getUsername() {
        return username;
    }
}
