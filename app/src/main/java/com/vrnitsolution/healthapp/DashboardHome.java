package com.vrnitsolution.healthapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.vrnitsolution.healthapp.DashboardProfile.DashboardProfile;
import com.vrnitsolution.healthapp.GetNotification.AllNotification;
import com.vrnitsolution.healthapp.Location.MapsActivity;
import com.vrnitsolution.healthapp.adapter.DoctorsAdapter;
import com.vrnitsolution.healthapp.bookappointment.adapter.HistoryAppointment;
import com.vrnitsolution.healthapp.bookappointment.model.Patient;
import com.vrnitsolution.healthapp.model.Doctors;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class DashboardHome extends AppCompatActivity implements HistoryAppointment.OnAppointmentItemClickListner{
    FirebaseAuth firebaseAuth;
    TextView username, useremail;
    CircleImageView userProfile;
    private DoctorsAdapter doctorAdapter;
    FirebaseFirestore db;
    RecyclerView nearbyDoctorRecyclerView, recycler_activeapt;
    TextView notdata1;

    private BottomAppBar bottomAppBar;
    private BottomNavigationView bottomNavigationView;
    private HistoryAppointment appointmentAdapternew;
    ArrayList<Patient> patients;
    ArrayList<Doctors> doctors;
    TextView notdata;
    ProgressBar progressBar;

    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard_home);

        bottomAppBar = findViewById(R.id.bottom_app_bar);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        progressBar = findViewById(R.id.progressBar);
        notdata = findViewById(R.id.notdata);
        notdata1 = findViewById(R.id.notdata1);


        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_home:
                        // Handle home action
                        Toast.makeText(DashboardHome.this, "" + item.getTitle(), Toast.LENGTH_SHORT).show();
                        return true;
                    case R.id.navigation_dashboard:
                        // Handle dashboard action
                        startActivity(new Intent(DashboardHome.this, DashboardProfile.class));
                        return true;
                    case R.id.navigation_notifications:
                        // Handle notifications action
                        startActivity(new Intent(DashboardHome.this, AllNotification.class));
                        return true;
                    case R.id.search_bar:
                        startActivity(new Intent(DashboardHome.this,SearchDoctor.class));
                }
                return false;
            }
        });


//        Window window = this.getWindow();
//        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);


        db = FirebaseFirestore.getInstance();


        username = findViewById(R.id.displayname);
        useremail = findViewById(R.id.email);
        userProfile = findViewById(R.id.profileImage);


        //Set RecyclerView Active Appointments
        recycler_activeapt = findViewById(R.id.recycler_activeapt);
        recycler_activeapt.setHasFixedSize(true);
        recycler_activeapt.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));

        patients = new ArrayList<>();
        appointmentAdapternew = new HistoryAppointment(DashboardHome.this, patients,this);
        recycler_activeapt.setAdapter(appointmentAdapternew);


        //Nearby Doctor Adapter
        nearbyDoctorRecyclerView = findViewById(R.id.recycler_main);

        nearbyDoctorRecyclerView.setHasFixedSize(true);
        nearbyDoctorRecyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));

        doctors = new ArrayList<>();
        doctorAdapter = new DoctorsAdapter(DashboardHome.this, doctors);
        nearbyDoctorRecyclerView.setAdapter(doctorAdapter);


    }


    /**
     * Get All Active Appointments according that doctorId present in appointmentdata collection
     */
    private void getActiveAppointments(String userId) {
        patients.clear();
        db.collection("appointmentdata")
                .whereEqualTo("userId", userId)
                .whereEqualTo("visiting_status","Not Visited")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Log.e("Firestore error", error.getMessage());
                            return;
                        }

                        if (patients!=null)
                        {
                            patients.clear();
                        }

                        // Get current timestamp
                        Timestamp currentTime = new Timestamp(new Date());

                        for (DocumentSnapshot document : value.getDocuments()) {
                            Patient patient = document.toObject(Patient.class);
                            if (patient != null && patient.getScheduleTime() != null) {
                                // Compare scheduleTime with current time
                                if (patient.getScheduleTime().compareTo(currentTime) > 0) {
                                    // If scheduleTime is in the future, consider it an active appointment
                                    patients.add(patient);
                                }
                            }
                        }

                        if (patients.isEmpty()) {
                            notdata1.setVisibility(View.VISIBLE);
                            notdata1.setText("No Active Appointments");
                        } else {
                            notdata1.setVisibility(View.GONE);
                        }
                        // Notify the adapter that the data has changed
                        appointmentAdapternew.notifyDataSetChanged();
                    }
                });
    }


    /**
     * From this method calls it gets coordinates latitude and longitude from collection
     */
    private void getUserLocation(String uid) {
        // Query the "users" collection to get the current user's coordinates based on UID
        db.collection("users")
                .whereEqualTo("uid", uid)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Assuming there is only one document for the given UID
                        DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);


                        // Extract latitude and longitude from the coordinates map
                        Map<String, String> coordinates = (Map<String, String>) documentSnapshot.get("currentCoordinates");

                        if (coordinates != null && coordinates.containsKey("latitude") && coordinates.containsKey("longitude")) {
                            String latitude = coordinates.get("latitude");
                            String longitude = coordinates.get("longitude");


                            getNearbyDoctors(latitude, longitude);

                        }

                    } else {
                        // No user found for the given UID
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle errors
                    Toast.makeText(this, "Failed to get data", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * GetNearby Doctor according to the latitude and longitude and account is approved by the admin
     */
    private void getNearbyDoctors(String latitude, String longitude) {
        doctors.clear();
        double userLatitude = Double.parseDouble(latitude);
        double userLongitude = Double.parseDouble(longitude);
        double radius = 10.0; // 10 km radius

        db.collection("doctors")
                .whereEqualTo("account_status", "Approved")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshot, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            progressBar.setVisibility(View.GONE);
                            Log.e("Firestore error", error.getMessage());
                            return;
                        }


                        List<Doctors> nearbyDoctorsList = new ArrayList<>();

                        for (QueryDocumentSnapshot document : snapshot) {
                            Map<String, String> coordinates = (Map<String, String>) document.get("coordinates");
                            if (coordinates != null && coordinates.containsKey("latitude") && coordinates.containsKey("longitude")) {
                                double doctorLatitude = Double.parseDouble(coordinates.get("latitude"));
                                double doctorLongitude = Double.parseDouble(coordinates.get("longitude"));

                                float[] results = new float[1];
                                Location.distanceBetween(userLatitude, userLongitude, doctorLatitude, doctorLongitude, results);
                                double distance = results[0] / 1000; // Convert meters to kilometers

                                if (distance <= radius) {
                                    Doctors doctor = document.toObject(Doctors.class);
                                    doctor.setDistance(String.valueOf(distance).substring(0, 3));
                                    nearbyDoctorsList.add(doctor);
                                }
                            }
                        }

                        // Set the nearby doctors list to your adapter
                        doctors.clear();
                        progressBar.setVisibility(View.GONE);
                        doctors.addAll(nearbyDoctorsList);
                        doctorAdapter.notifyDataSetChanged();

                        if (doctors.isEmpty()) {
                            progressBar.setVisibility(View.GONE);
                            notdata.setVisibility(View.VISIBLE);
                            notdata.setText("No nearby doctors found");
                        } else {
                            notdata.setVisibility(View.GONE);
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (user != null) {

            String userProfileUrl = String.valueOf(user.getPhotoUrl());
            username.setText(user.getDisplayName());
            useremail.setText(user.getEmail().substring(0, 20) + "...");

            Glide.with(this)
                    .load(userProfileUrl)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            Log.e("GlideError", "Image load failed: " + e.getMessage());
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            Log.d("GlideSuccess", "Image loaded successfully");
                            return false;
                        }
                    })
                    .into(userProfile);

            getUserLocation(user.getUid());
            getActiveAppointments(user.getUid());


        } else {
            finish();
            finishAffinity();
            startActivity(new Intent(DashboardHome.this, LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }

    }


    @Override
    public void onAppintmentItemClick(int position) {
        //open the appointment history activity
        Patient patient = patients.get(position);
        // Format scheduleTime to the desired format
        SimpleDateFormat dateFormat = new SimpleDateFormat("d MMMM yyyy 'at' hh:mm a", Locale.getDefault());
        String formattedDate = dateFormat.format(patient.getScheduleTime().toDate());

        Intent appointmentDetailsPage = new Intent(DashboardHome.this, AppointmentDetailsPage.class);
        appointmentDetailsPage.putExtra("PatientName", patient.getPatientName());
        appointmentDetailsPage.putExtra("PatientMobileNo", patient.getPatientMobileNo());
        appointmentDetailsPage.putExtra("PatientProblem", patient.getPatientProblem());
        appointmentDetailsPage.putExtra("scheduleTime", formattedDate);
        appointmentDetailsPage.putExtra("AppointmentId", patient.getAppintmentId());
        appointmentDetailsPage.putExtra("userId", patient.getUserId());
        appointmentDetailsPage.putExtra("docId", patient.getDocId());
        appointmentDetailsPage.putExtra("visiting_status", patient.getVisiting_status());

        startActivity(appointmentDetailsPage);
    }

    public void viewProfile(View view) {
        startActivity(new Intent(DashboardHome.this,UpdateUserProfile.class));
    }
}


