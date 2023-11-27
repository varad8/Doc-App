package com.vrnitsolution.healthapp.bookappointment.model;

import com.google.firebase.Timestamp;

public class Patient {
    private String AppintmentId;
    private String PatientMobileNo;
    private String PatientName;
    private String PatientProblem;
    private String docId;
    private Timestamp scheduleTime;
    private String userId;
    private String visiting_status;

    // Empty constructor
    public Patient() {
    }

    // Parameterized constructor
    public Patient(String AppintmentId, String PatientMobileNo, String PatientName, String PatientProblem,
                   String docId, Timestamp scheduleTime, String userId, String visiting_status) {
        this.AppintmentId = AppintmentId;
        this.PatientMobileNo = PatientMobileNo;
        this.PatientName = PatientName;
        this.PatientProblem = PatientProblem;
        this.docId = docId;
        this.scheduleTime = scheduleTime;
        this.userId = userId;
        this.visiting_status = visiting_status;
    }

    // Getters and setters
    public String getAppintmentId() {
        return AppintmentId;
    }

    public void setAppintmentId(String AppintmentId) {
        this.AppintmentId = AppintmentId;
    }

    public String getPatientMobileNo() {
        return PatientMobileNo;
    }

    public void setPatientMobileNo(String PatientMobileNo) {
        this.PatientMobileNo = PatientMobileNo;
    }

    public String getPatientName() {
        return PatientName;
    }

    public void setPatientName(String PatientName) {
        this.PatientName = PatientName;
    }

    public String getPatientProblem() {
        return PatientProblem;
    }

    public void setPatientProblem(String PatientProblem) {
        this.PatientProblem = PatientProblem;
    }

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public Timestamp getScheduleTime() {
        return scheduleTime;
    }

    public void setScheduleTime(Timestamp scheduleTime) {
        this.scheduleTime = scheduleTime;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getVisiting_status() {
        return visiting_status;
    }

    public void setVisiting_status(String visiting_status) {
        this.visiting_status = visiting_status;
    }
}
