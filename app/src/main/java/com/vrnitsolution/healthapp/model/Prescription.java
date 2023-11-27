package com.vrnitsolution.healthapp.model;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class Prescription {
    private String appointmentId;
    private String doctorId;
    private List<Map<String, String>> dosage;
    private String patientMobileNo;
    private String patientName;
    @ServerTimestamp
    private Date prescriptionissueddate;
    private String userId;

    public Prescription() {
    }

    public String getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(String appointmentId) {
        this.appointmentId = appointmentId;
    }

    public String getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(String doctorId) {
        this.doctorId = doctorId;
    }

    public List<Map<String, String>> getDosage() {
        return dosage;
    }

    public void setDosage(List<Map<String, String>> dosage) {
        this.dosage = dosage;
    }

    public String getPatientMobileNo() {
        return patientMobileNo;
    }

    public void setPatientMobileNo(String patientMobileNo) {
        this.patientMobileNo = patientMobileNo;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public Date getPrescriptionissueddate() {
        return prescriptionissueddate;
    }

    public void setPrescriptionissueddate(Date prescriptionissueddate) {
        this.prescriptionissueddate = prescriptionissueddate;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
