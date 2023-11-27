package com.vrnitsolution.healthapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.vrnitsolution.healthapp.adapter.DoctorSearchAdapter;
import com.vrnitsolution.healthapp.adapter.DoctorsAdapter;
import com.vrnitsolution.healthapp.bookappointment.model.Patient;
import com.vrnitsolution.healthapp.model.Doctors;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class SearchDoctor extends AppCompatActivity implements DoctorSearchAdapter.OnDoctorProfileClickListner {
    EditText search_specialist;
    ImageView imageView3, searchBtn;
    private DoctorSearchAdapter doctorAdapter;
    RecyclerView searchRecyclerview;
    ArrayList<Doctors> doctors;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    // Create a reference to the "doctors" collection
    private CollectionReference doctorCollection = db.collection("doctors");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_doctor);

        search_specialist = findViewById(R.id.search_specialist);
        imageView3 = findViewById(R.id.imageView3);
        searchBtn = findViewById(R.id.searchBtn);


        search_specialist.requestFocus();

        imageView3.setOnClickListener(view -> {
            finish();
        });

        getAllDoctor();


        searchRecyclerview = findViewById(R.id.searchRecyclerview);
        searchRecyclerview.setHasFixedSize(true);
        searchRecyclerview.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        doctors = new ArrayList<>();
        doctorAdapter = new DoctorSearchAdapter(SearchDoctor.this, doctors, this);
        searchRecyclerview.setAdapter(doctorAdapter);


        searchBtn.setOnClickListener(view -> {
            String specialistIn = search_specialist.getText().toString().trim();

            if (specialistIn.isEmpty()) {
                displayToast("Enter specialist doctor that you want");
            } else {
                searchSpecialistDoctor(specialistIn);

            }
        });

    }

    private void searchSpecialistDoctor(String specialistIn) {
        // Clear the existing list of doctors
        doctors.clear();


        // Query the Firestore database to get doctors with the specified specialization
        doctorCollection.whereEqualTo("specialistIn", specialistIn)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Check if there are any matching doctors
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (Doctors doctor : queryDocumentSnapshots.toObjects(Doctors.class)) {
                            doctors.add(doctor);
                        }
                        doctorAdapter.notifyDataSetChanged();
                    } else {
                        displayToast("No doctors found with the specified specialization");
                    }
                })
                .addOnFailureListener(e -> {
                    displayToast("Error: " + e.getMessage());
                });
    }


    private void getAllDoctor() {
        // Clear the existing list of doctors
        if (doctors != null) {
            doctors.clear();

        }        // Query the Firestore database to get doctors with the specified specialization
        doctorCollection.whereEqualTo("account_status", "Approved").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Check if there are any matching doctors
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (Doctors doctor : queryDocumentSnapshots.toObjects(Doctors.class)) {
                            doctors.add(doctor);
                        }
                        doctorAdapter.notifyDataSetChanged();
                    } else {
                        displayToast("No doctors found");
                    }
                })
                .addOnFailureListener(e -> {
                    displayToast("Error: " + e.getMessage());
                });
    }


    public void displayToast(String txt) {
        Toast.makeText(SearchDoctor.this, "" + txt, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDoctorProfileClick(int position, Doctors doctors) {
        // Handle item click event, if necessary
        Intent intent = new Intent(SearchDoctor.this, DoctorDeatailsPage.class);
        intent.putExtra("dcname", doctors.getDoctorName());
        intent.putExtra("dcId", doctors.getDoctorId());
        intent.putExtra("dcemail", doctors.getEmail());
        intent.putExtra("dcPhoto", doctors.getPhotoUrl());

        // Pass availableServices map to the next activity
        intent.putStringArrayListExtra("availableServices", doctors.getAvailableservices());
        intent.putExtra("dcoccupation", doctors.getOccupation());
        intent.putExtra("coordinates", new HashMap<>(doctors.getCoordinates()));
        intent.putExtra("serviceHours", new HashMap<>(doctors.getServicehours()));
        intent.putExtra("dcSpecialistIn", doctors.getSpecialistIn());

        startActivity(intent);
    }
}


