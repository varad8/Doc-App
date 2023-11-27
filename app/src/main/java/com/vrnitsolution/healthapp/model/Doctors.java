package com.vrnitsolution.healthapp.model;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Doctors {
    private String distance;
    private String doctorId;
    private String doctorName;
    private String email;
    private String occupation;
    private String photoUrl;
    private ArrayList<String> availableservices;
    private Map<String, String> coordinates;
    private Map<String, String> servicehours;
    private String specialistIn;
    private String messageToken;
    private String account_status;

    public Doctors() {
    }

    public Doctors(String distance, String doctorId, String doctorName, String email, String occupation, String photoUrl, ArrayList<String> availableservices, Map<String, String> coordinates, Map<String, String> servicehours, String specialistIn, String messageToken, String account_status) {
        this.distance = distance;
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.email = email;
        this.occupation = occupation;
        this.photoUrl = photoUrl;
        this.availableservices = availableservices;
        this.coordinates = coordinates;
        this.servicehours = servicehours;
        this.specialistIn = specialistIn;
        this.messageToken = messageToken;
        this.account_status = account_status;
    }

    public String getAccount_status() {
        return account_status;
    }

    public void setAccount_status(String account_status) {
        this.account_status = account_status;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(String doctorId) {
        this.doctorId = doctorId;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOccupation() {
        return occupation;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public ArrayList<String> getAvailableservices() {
        return availableservices;
    }

    public void setAvailableservices(ArrayList<String> availableservices) {
        this.availableservices = availableservices;
    }

    public Map<String, String> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(Map<String, String> coordinates) {
        this.coordinates = coordinates;
    }

    public Map<String, String> getServicehours() {
        return servicehours;
    }

    public void setServicehours(Map<String, String> servicehours) {
        this.servicehours = servicehours;
    }

    public String getSpecialistIn() {
        return specialistIn;
    }

    public void setSpecialistIn(String specialistIn) {
        this.specialistIn = specialistIn;
    }

    public String getMessageToken() {
        return messageToken;
    }

    public void setMessageToken(String messageToken) {
        this.messageToken = messageToken;
    }
}
