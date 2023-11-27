package com.vrnitsolution.healthapp.Admin;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.vrnitsolution.healthapp.Admin.adapter.DoctorAdapterNew;
import com.vrnitsolution.healthapp.DashboardHome;
import com.vrnitsolution.healthapp.DashboardProfile.DashboardProfile;
import com.vrnitsolution.healthapp.DoctorDeatailsPage;
import com.vrnitsolution.healthapp.LoginActivity;
import com.vrnitsolution.healthapp.MainActivity;
import com.vrnitsolution.healthapp.R;
import com.vrnitsolution.healthapp.model.Doctors;

import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdminDashboard extends AppCompatActivity implements DoctorAdapterNew.OnDoctorProfileClickListner{
    CircleImageView profileimage;
    TextView email, username, accounttype, registerDocCount, registerApprovedDoctors, registernotApproved;

    // Assuming you have a Firebase instance
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Reference to the "doctors" collection
    CollectionReference doctorsCollection = db.collection("doctors");
    private DoctorAdapterNew doctorAdapterNew;
    ArrayList<Doctors>doctors;
    RecyclerView recyclerview;
    private BottomAppBar bottomAppBar;
    private BottomNavigationView bottomNavigationView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

//        Window window = this.getWindow();
//        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);


        profileimage = findViewById(R.id.profileimage);
        email = findViewById(R.id.email);
        username = findViewById(R.id.username);
        accounttype = findViewById(R.id.accountType);
        registerDocCount = findViewById(R.id.registerDocCount);
        registerApprovedDoctors = findViewById(R.id.registerApprovedDoctors);
        registernotApproved = findViewById(R.id.registernotApproved);
        bottomAppBar = findViewById(R.id.bottom_app_bar);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        recyclerview=findViewById(R.id.recyclerview);
        recyclerview.setHasFixedSize(true);
        recyclerview.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));

        doctors=new ArrayList<>();
        doctorAdapterNew=new DoctorAdapterNew(this,doctors,this);
        recyclerview.setAdapter(doctorAdapterNew);


        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_home:
                        return true;
                    case R.id.navigation_doctor:
                        // Handle dashboard action
                        startActivity(new Intent(AdminDashboard.this, AllDoctorListActivity.class));
                        return true;
                }
                return false;
            }
        });


        getCount();
        getNotApprovedDoctors();

    }

    /**
     * When this method calls then it will get all records where that equal to Not Approved and set on that recyclerView
     */
    private void getNotApprovedDoctors() {
        doctorsCollection.whereEqualTo("account_status", "Not Approved")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            // Handle the error
                            Log.w(TAG, "Error getting not approved doctors", error);
                            return;
                        }
                        // Clear the existing list
                        doctors.clear();

                        if (value != null) {
                            for (QueryDocumentSnapshot document : value) {
                                // Assuming you have a Doctor class, adjust accordingly
                                Doctors doctor = document.toObject(Doctors.class);
                                doctors.add(doctor);
                            }

                            // Notify the adapter of the data change
                            doctorAdapterNew.notifyDataSetChanged();
                        }
                }
                });
    }


    @Override
    protected void onStart() {
        super.onStart();

        retrieveDataFromSharedPreferences();
    }

    private void retrieveDataFromSharedPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("adminData", MODE_PRIVATE);

        // Retrieve data from SharedPreferences
        String savedUsername = sharedPreferences.getString("username", "");
        String savedEmail = sharedPreferences.getString("email", "");
        String savedProfileUrl = sharedPreferences.getString("profileUrl", "");
        String savedAccountType = sharedPreferences.getString("accountType", "");


        if (sharedPreferences != null) {
            if (!savedEmail.isEmpty()) {
                //set the fields

                email.setText(savedEmail);
                username.setText(savedUsername);
                accounttype.setText(savedAccountType);

                Glide.with(AdminDashboard.this).load(savedProfileUrl).into(profileimage);
            }
        }
    }


    /**
     * When this method calls it will get counts of document
     */
    public void getCount() {
        // Get the total count of documents
        doctorsCollection.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    // Handle errors
                    Log.w(TAG, "Error getting document count", e);
                    return;
                }

                if (queryDocumentSnapshots != null) {
                    String totalDocumentCount = String.valueOf(queryDocumentSnapshots.size());
                    registerDocCount.setText(totalDocumentCount);
                }
            }
        });

        // Get the count of documents with visiting_status = "Approved"
        doctorsCollection.whereEqualTo("account_status", "Approved").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    // Handle errors
                    Log.w(TAG, "Error getting approved doctors count", e);
                    return;
                }

                if (queryDocumentSnapshots != null) {
                    String approvedCount = String.valueOf(queryDocumentSnapshots.size());
                    registerApprovedDoctors.setText(approvedCount);
                }
            }
        });

        // Get the count of documents with visiting_status = "Not Approved"
        doctorsCollection.whereEqualTo("account_status", "Not Approved").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    // Handle errors
                    Log.w(TAG, "Error getting not approved doctors count", e);
                    return;
                }

                if (queryDocumentSnapshots != null) {
                    String notApprovedCount = String.valueOf(queryDocumentSnapshots.size());
                    registernotApproved.setText(notApprovedCount);
                }
            }
        });
    }


    @Override
    public void onDoctorProfileClick(int position, Doctors doctor) {
                    Intent intent=new Intent(AdminDashboard.this, DoctorDetailForAdmin.class);
                    intent.putExtra("dcname",doctor.getDoctorName());
                    intent.putExtra("dcId",doctor.getDoctorId());
                    intent.putExtra("dcemail",doctor.getEmail());
                    intent.putExtra("dcPhoto",doctor.getPhotoUrl());

                    // Pass availableServices map to the next activity
                    intent.putStringArrayListExtra("availableServices", doctor.getAvailableservices());
                    intent.putExtra("dcoccupation",doctor.getOccupation());
                    intent.putExtra("coordinates",new HashMap<>(doctor.getCoordinates()));
                    intent.putExtra("serviceHours",new HashMap<>(doctor.getServicehours()));
                    intent.putExtra("dcSpecialistIn",doctor.getSpecialistIn());
                    intent.putExtra("status",doctor.getAccount_status());
                    startActivity(intent);

    }


    public void logOut(View view) {
        SharedPreferences preferences = getSharedPreferences("adminData", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear(); // Clear all data in the SharedPreferences
        editor.apply();

        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

}