package com.vrnitsolution.healthapp.model;

import java.io.Serializable;

public class DosageUser implements Serializable {
    private String dosageAfterMill;
    private String dosageName;
    private String dosageRemark;
    private String dosageTime;

    public DosageUser(String dosageAfterMill, String dosageName, String dosageRemark, String dosageTime) {
        this.dosageAfterMill = dosageAfterMill;
        this.dosageName = dosageName;
        this.dosageRemark = dosageRemark;
        this.dosageTime = dosageTime;
    }

    public DosageUser() {
    }

    public String getDosageAfterMill() {
        return dosageAfterMill;
    }

    public String getDosageName() {
        return dosageName;
    }

    public String getDosageRemark() {
        return dosageRemark;
    }

    public String getDosageTime() {
        return dosageTime;
    }
}
