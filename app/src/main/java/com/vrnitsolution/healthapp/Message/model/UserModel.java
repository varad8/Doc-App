package com.vrnitsolution.healthapp.Message.model;

public class UserModel {
    private String userId;
    private String photoUrl;
    private String username;
    private String accountType;
    private String email;
    private String token;
    public UserModel() {
    }

    public UserModel(String userId, String photoUrl, String username, String accountType, String email, String token) {
        this.userId = userId;
        this.photoUrl = photoUrl;
        this.username = username;
        this.accountType = accountType;
        this.email = email;
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }
}
