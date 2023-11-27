package com.vrnitsolution.healthapp.DoctorUI.model;

public class Dosage {
    private String dosageName;
    private String dosageTime;
    private String dosageRemark;
    private String dosageAfterMill;

    public Dosage(String dosageName, String dosageTime, String dosageRemark, String dosageAfterMill) {
        this.dosageName = dosageName;
        this.dosageTime = dosageTime;
        this.dosageRemark = dosageRemark;
        this.dosageAfterMill = dosageAfterMill;
    }

    public String getDosageName() {
        return dosageName;
    }

    public void setDosageName(String dosageName) {
        this.dosageName = dosageName;
    }

    public String getDosageTime() {
        return dosageTime;
    }

    public void setDosageTime(String dosageTime) {
        this.dosageTime = dosageTime;
    }

    public String getDosageRemark() {
        return dosageRemark;
    }

    public void setDosageRemark(String dosageRemark) {
        this.dosageRemark = dosageRemark;
    }

    public String getDosageAfterMill() {
        return dosageAfterMill;
    }

    public void setDosageAfterMill(String dosageAfterMill) {
        this.dosageAfterMill = dosageAfterMill;
    }
}

