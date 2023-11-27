package com.vrnitsolution.healthapp.GetNotification.model;


import com.google.firebase.Timestamp;

public class NotificationModel {
    private  String userid;
    private String message;
    private String sentBy;
    private String senderDoctorId;
    private String dcPhoto;
    private  String title;
    private  String notificationImage;
    private Timestamp createdAt;

    public NotificationModel(String userid, String message, String sentBy, String senderDoctorId, String dcPhoto, String title, String notificationImage, Timestamp createdAt) {
        this.userid = userid;
        this.message = message;
        this.sentBy = sentBy;
        this.senderDoctorId = senderDoctorId;
        this.dcPhoto = dcPhoto;
        this.title = title;
        this.notificationImage = notificationImage;
        this.createdAt = createdAt;
    }

    public NotificationModel() {
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getNotificationImage() {
        return notificationImage;
    }

    public void setNotificationImage(String notificationImage) {
        this.notificationImage = notificationImage;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSentBy() {
        return sentBy;
    }

    public void setSentBy(String sentBy) {
        this.sentBy = sentBy;
    }

    public String getSenderDoctorId() {
        return senderDoctorId;
    }

    public void setSenderDoctorId(String senderDoctorId) {
        this.senderDoctorId = senderDoctorId;
    }

    public String getDcPhoto() {
        return dcPhoto;
    }

    public void setDcPhoto(String dcPhoto) {
        this.dcPhoto = dcPhoto;
    }
}
