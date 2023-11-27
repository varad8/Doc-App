package com.vrnitsolution.healthapp.DoctorUI;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.vrnitsolution.healthapp.DoctorUI.adapter.DosageAdapter;
import com.vrnitsolution.healthapp.DoctorUI.model.Dosage;
import com.vrnitsolution.healthapp.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GeneratePrescription extends AppCompatActivity implements DosageAdapter.OnDosageClickListener {
    TextView appointmentid, PatientName, PatientMobileNo, PatientProblem;
    String doctorId = "", patientName = "", userid = "", patientMobileNo = "", appointmetnId = "";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference prescriptionDataCollection = db.collection("prescriptiondata");
    AppCompatButton dosagebtn, savePrescription;
    private List<Dosage> dosageList;
    private DosageAdapter dosageAdapter;
    private RecyclerView recyclerView;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_prescription);

//        Window window = this.getWindow();
//        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);


        appointmentid = findViewById(R.id.appointmentid);
        PatientName = findViewById(R.id.PatientName);
        PatientMobileNo = findViewById(R.id.PatientMobileNo);
        PatientProblem = findViewById(R.id.PatientProblem);
        dosagebtn = findViewById(R.id.dosagebtn);
        savePrescription = findViewById(R.id.savePrescription);


        dosageList = new ArrayList<>();
        recyclerView = findViewById(R.id.llDosageContainer);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        dosageAdapter = new DosageAdapter(dosageList, this, this);
        recyclerView.setAdapter(dosageAdapter);


        dosagebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDosageDialog();
            }
        });


        Intent intent = getIntent();

        if (intent != null) {
            appointmetnId = intent.getStringExtra("AppointmentId");
            patientName = intent.getStringExtra("PatientName");
            patientMobileNo = intent.getStringExtra("patientMobileNO");


            appointmentid.setText("Appointment Id : " + appointmetnId);
            PatientName.setText("Patient Name : " + patientName);
            PatientMobileNo.setText("Patient Mobile No : " + patientMobileNo);
            PatientProblem.setText("Patient Problem :" + intent.getStringExtra("PatientProblem"));
            userid = intent.getStringExtra("userid");
            doctorId = intent.getStringExtra("doctorId");

        }


        dosagebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDosageDialog();
            }
        });

        progressDialog = new ProgressDialog(GeneratePrescription.this);
        progressDialog.setMessage("Wait While Adding data..");
        progressDialog.setCancelable(false);


        //save the data
        savePrescription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (appointmetnId == null || appointmetnId.isEmpty() ||
                        patientName == null || patientName.isEmpty() ||
                        patientMobileNo == null || patientMobileNo.isEmpty() ||
                        userid == null || userid.isEmpty() ||
                        doctorId == null || doctorId.isEmpty() ||
                        dosageList == null || dosageList.isEmpty()) {
                    Toast.makeText(GeneratePrescription.this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
                } else {
                    progressDialog.show();

                    savePrescriptionData(appointmetnId, patientName, userid, patientMobileNo, doctorId, dosageList);
                }
            }
        });
    }


    /**
     * This Method save the data in firestore collection
     */
    private void savePrescriptionData(String appointmetnId, String patientName, String userid, String patientMobileNo, String doctorId, List<Dosage> dosageList) {
        // Create a Map to hold the data you want to save
        Map<String, Object> prescriptionData = new HashMap<>();
        prescriptionData.put("appointmentId", appointmetnId);
        prescriptionData.put("patientName", patientName);
        prescriptionData.put("userId", userid);
        prescriptionData.put("patientMobileNo", patientMobileNo);
        prescriptionData.put("doctorId", doctorId);
        prescriptionData.put("dosage", dosageList);
        prescriptionData.put("prescriptionissueddate", Timestamp.now());

        // Add data to Firestore
        prescriptionDataCollection.add(prescriptionData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getApplicationContext(), "Prescription data saved successfully!", Toast.LENGTH_SHORT).show();
                    updateVisitingStatus(appointmetnId);

                })
                .addOnFailureListener(e -> {
                    // Error occurred while adding data
                    progressDialog.dismiss();
                    Toast.makeText(getApplicationContext(), "Failed to save prescription data. Please try again.", Toast.LENGTH_SHORT).show();
                    // Handle error if needed
                });

    }


    /**
     * When prescription save then update appointment staus to Visited using that userid and appointment id match found
     *
     * @param appointmentId
     */
    private void updateVisitingStatus(String appointmentId) {
        Toast.makeText(GeneratePrescription.this, "" + appointmentId, Toast.LENGTH_SHORT).show();

        // Reference to the "appointmentdata" collection
        db.collection("appointmentdata").whereEqualTo("AppintmentId", appointmentId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            // Log the current AppointmentId to ensure it's correct
                            Log.d("UpdateScreen", "Current AppointmentId: " + document.getString("AppointmentId"));
                            Toast.makeText(this,document.getId(),Toast.LENGTH_SHORT).show();

                            // Update the visiting_status field to the new status
                            document.getReference().update("visiting_status", "Visited")
                                    .addOnCompleteListener(updateTask -> {
                                        if (updateTask.isSuccessful()) {
                                            // Successfully updated
                                            progressDialog.dismiss();
                                            Log.d("UpdateScreen", "Visiting status updated successfully");
                                            finish();
                                        } else {
                                            progressDialog.dismiss();
                                            // Failed to update
                                            Log.d("Failure", "Error updating visiting_status: " + updateTask.getException().getMessage());
                                        }
                                    });
                        }
                    } else {
                        // Failed to query
                        Log.d("Failure", "Error querying documents: " + task.getException().getMessage());
                    }
                });
    }




    /**
     * It Open Dosage Dialog there you can add dosage information
     */
    private void showDosageDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Dosage Information");

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_dosage, null);
        builder.setView(view);

        final EditText dosageNameEditText = view.findViewById(R.id.editDosageName);
        final EditText dosageTimeEditText = view.findViewById(R.id.editDosageTime);
        final EditText dosageRemarkEditText = view.findViewById(R.id.editDosageRemark);
        final EditText dosageAfterMillEditText = view.findViewById(R.id.editDosageAfterMill);

        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String dosageName = dosageNameEditText.getText().toString();
                String dosageTime = dosageTimeEditText.getText().toString();
                String dosageRemark = dosageRemarkEditText.getText().toString();
                String dosageAfterMill = dosageAfterMillEditText.getText().toString();

                if (!dosageName.isEmpty() && !dosageTime.isEmpty() && !dosageRemark.isEmpty() && !dosageAfterMill.isEmpty()) {
                    Dosage dosage = new Dosage(dosageName, dosageTime, dosageRemark, dosageAfterMill);
                    dosageList.add(dosage);
                    dosageAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(GeneratePrescription.this, "Enter Dosage", Toast.LENGTH_SHORT).show();

                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing, dialog will be dismissed
            }
        });

        builder.show();
    }

    /**
     * When click on Dosage Item then It Open Edit Dilog there you can update or delete data
     */
    @Override
    public void onDosageClick(final int position) {

        ViewDosageDialog.showDialog(this, dosageList.get(position), position, new ViewDosageDialog.OnDosageEditListener() {
            @Override
            public void onDosageEdited(int position, Dosage editedDosage) {
                // Update the dosage at the specified position with the edited dosage
                dosageList.set(position, editedDosage);
                dosageAdapter.notifyDataSetChanged();
                Toast.makeText(GeneratePrescription.this, "Dosage Edited!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDosageDeleted(int position) {
                // Remove the dosage at the specified position from the list
                dosageList.remove(position);
                dosageAdapter.notifyItemRemoved(position);
                Toast.makeText(GeneratePrescription.this, "Dosage Deleted!", Toast.LENGTH_SHORT).show();
            }
        });
    }


}