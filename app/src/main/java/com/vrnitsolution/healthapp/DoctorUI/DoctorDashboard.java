package com.vrnitsolution.healthapp.DoctorUI;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.vrnitsolution.healthapp.DashboardHome;
import com.vrnitsolution.healthapp.DashboardProfile.DashboardProfile;
import com.vrnitsolution.healthapp.DoctorUI.model.ProfileForMessage;
import com.vrnitsolution.healthapp.GetNotification.AllNotification;
import com.vrnitsolution.healthapp.LoginActivity;
import com.vrnitsolution.healthapp.MainActivity;
import com.vrnitsolution.healthapp.R;
import com.vrnitsolution.healthapp.bookappointment.adapter.AppointmentAdapternew;
import com.vrnitsolution.healthapp.bookappointment.adapter.HistoryAppointment;
import com.vrnitsolution.healthapp.bookappointment.model.Patient;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class DoctorDashboard extends AppCompatActivity implements HistoryAppointment.OnAppointmentItemClickListner ,AppointmentAdapternew.onClickFutureAppointment {

    String email, doctorId;
    CircleImageView profileImage, backBtnImageview;
    TextView doctorName, doctorEmial, approvedornot;
    RecyclerView appoinmentRecyclerView,futureAppointments;
    HistoryAppointment historyAdapter;
    AppointmentAdapternew futurehistoryAdapter;
    ArrayList<Patient> patients;
    ArrayList<Patient>futurepatients;

    TextView noactivedata,noactivefureappointments;
    private BottomAppBar bottomAppBar;
    private BottomNavigationView bottomNavigationView;


    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    // Create a reference to the "doctors" collection
    private CollectionReference doctorsCollection = db.collection("doctors");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_dashboard);

//        Window window = this.getWindow();
//        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);



        profileImage = findViewById(R.id.profileImage);
        backBtnImageview = findViewById(R.id.backBtnImageview);
        doctorName = findViewById(R.id.doctorName);
        doctorEmial = findViewById(R.id.doctorEmial);
        approvedornot = findViewById(R.id.textView7);
        noactivedata = findViewById(R.id.noactivedata);
        noactivefureappointments=findViewById(R.id.noactivefureappointments);

        bottomAppBar = findViewById(R.id.bottom_app_bar);
        bottomNavigationView = findViewById(R.id.bottom_navigation);


        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_home:
                        // Handle home action
                        Toast.makeText(DoctorDashboard.this, "" + item.getTitle(), Toast.LENGTH_SHORT).show();
                        return true;
                    case R.id.navigation_dashboard:
                        if (doctorId != null) {
                            Intent dcboard = new Intent(DoctorDashboard.this, Doctor_Dashboard_Profile.class);
                            dcboard.putExtra("email", email);
                            dcboard.putExtra("doctorId", doctorId);
                            startActivity(dcboard);
                        }
                        return true;
                    case R.id.navigation_notifications:
                        // Handle notifications action
                        startActivity(new Intent(DoctorDashboard.this, AllNotification.class));
                        return true;
                }
                return false;
            }
        });


        backBtnImageview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //Set RecyclerView
        appoinmentRecyclerView = findViewById(R.id.appoinmentRecyclerView);
        appoinmentRecyclerView.setHasFixedSize(true);
        appoinmentRecyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        patients = new ArrayList<>();
        historyAdapter = new HistoryAppointment(DoctorDashboard.this, patients,this);
        appoinmentRecyclerView.setAdapter(historyAdapter);


        futureAppointments=findViewById(R.id.futureAppointments);
        futureAppointments.setHasFixedSize(true);
        futureAppointments.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false));

        futurepatients=new ArrayList<>();
        futurehistoryAdapter=new AppointmentAdapternew(DoctorDashboard.this,futurepatients,this);
        futureAppointments.setAdapter(futurehistoryAdapter);


    }

    /**
     * Get Active patient according to the doctorId
     */

    private void getPatinetActiveAppointments(String doctorId) {
        patients.clear();
        db.collection("appointmentdata")
                .whereEqualTo("docId", doctorId)
                .whereEqualTo("visiting_status", "Not Visited")
                .whereGreaterThan("scheduleTime", new Timestamp(new Date()))
                .whereLessThan("scheduleTime", new Timestamp(getEndOfDay()))
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
                            noactivedata.setVisibility(View.VISIBLE);
                            noactivedata.setText("No Active Appointments");
                        } else {
                            noactivedata.setVisibility(View.GONE);
                        }
                        // Notify the adapter that the data has changed
                        historyAdapter.notifyDataSetChanged();
                    }
                });
    }



    private Date getEndOfDay() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTime();
    }

    /**
     * Getting User data when email and doctorId macth found
     */
    private void getUserData(String email, String doctorId) {
        doctorsCollection.whereEqualTo("email", email).whereEqualTo("doctorId", doctorId).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (!value.isEmpty()) {
                    DocumentSnapshot doctorDocument = value.getDocuments().get(0);

                    // Retrieve doctor's data
                    String dcId = doctorDocument.getString("doctorId");
                    String dcemail = doctorDocument.getString("email");
                    String photoUrl = doctorDocument.getString("photoUrl");
                    String mobileNo = doctorDocument.getString("mobileNo");
                    String dcName = doctorDocument.getString("doctorName");
                    String dcStatus = doctorDocument.getString("account_status");

                    approvedornot.setText(dcStatus);
                    doctorName.setText(dcName);
                    doctorEmial.setText(dcemail);
                    Glide.with(DoctorDashboard.this).load(photoUrl).into(profileImage);
                }
            }
        });
    }


    private boolean isFutureDate(Timestamp appointmentTimestamp) {
        // Get current date without time (only year, month, and day)
        Calendar currentCalendar = Calendar.getInstance();
        currentCalendar.set(Calendar.HOUR_OF_DAY, 0);
        currentCalendar.set(Calendar.MINUTE, 0);
        currentCalendar.set(Calendar.SECOND, 0);
        currentCalendar.set(Calendar.MILLISECOND, 0);
        Date currentDate = currentCalendar.getTime();

        // Get appointment date without time (only year, month, and day)
        Calendar appointmentCalendar = Calendar.getInstance();
        appointmentCalendar.setTimeInMillis(appointmentTimestamp.getSeconds() * 1000);
        appointmentCalendar.set(Calendar.HOUR_OF_DAY, 0);
        appointmentCalendar.set(Calendar.MINUTE, 0);
        appointmentCalendar.set(Calendar.SECOND, 0);
        appointmentCalendar.set(Calendar.MILLISECOND, 0);
        Date appointmentDate = appointmentCalendar.getTime();

        // Compare appointmentDate with currentDate
        return appointmentDate != null && appointmentDate.after(currentDate);
    }

    private void getFutureAppointments(String doctorId) {
        futurepatients.clear();
        db.collection("appointmentdata")
                .whereEqualTo("docId", doctorId)
                .whereEqualTo("visiting_status", "Not Visited")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Log.e("Firestore error", error.getMessage());
                            return;
                        }

                        if (futurepatients!=null)
                        {
                            futurepatients.clear();
                        }

                        for (DocumentSnapshot document : value.getDocuments()) {
                            Patient patient = document.toObject(Patient.class);
                            if (patient != null && patient.getScheduleTime() != null) {
                                // Compare scheduleTime with current date
                                if (isFutureDate(patient.getScheduleTime())) {
                                    // If scheduleTime is in the future, consider it a future appointment
                                    futurepatients.add(patient);
                                }
                            }
                        }

                        if (futurepatients.isEmpty()) {
                            noactivefureappointments.setVisibility(View.VISIBLE);
                            noactivefureappointments.setText("No Future Appointments");
                        } else {
                            noactivefureappointments.setVisibility(View.GONE);
                        }
                        // Notify the adapter that the data has changed
                        futurehistoryAdapter.notifyDataSetChanged();
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        //check the that data is not null
        SharedPreferences preferences = getSharedPreferences("DoctorPrefs", MODE_PRIVATE);
        if (preferences != null) {
            // Retrieve doctor data from SharedPreferences
            doctorId = preferences.getString("doctorId", "");
            email = preferences.getString("email", "");

            if (doctorId != null && email != null) {
                getUserData(email, doctorId);
                getPatinetActiveAppointments(doctorId);
                getFutureAppointments(doctorId);

            } else {
                requestLogin();
            }
        } else {
            requestLogin();
        }
    }


    public void requestLogin() {
        finish();
        finishAffinity();
        startActivity(new Intent(this, DoctorLogin.class));
    }

    @Override
    public void onAppintmentItemClick(int position) {
        //open the appointment history activity
        Patient patient=patients.get(position);

        // Format scheduleTime to the desired format
        SimpleDateFormat dateFormat = new SimpleDateFormat("d MMMM yyyy 'at' hh:mm a", Locale.getDefault());
        String formattedDate = dateFormat.format(patient.getScheduleTime().toDate());

        Intent appointmentDetailsPage=new Intent(DoctorDashboard.this, AppointmentViewDoctor.class);
        appointmentDetailsPage.putExtra("PatientName",patient.getPatientName());
        appointmentDetailsPage.putExtra("PatientMobileNo",patient.getPatientMobileNo());
        appointmentDetailsPage.putExtra("PatientProblem",patient.getPatientProblem());
        appointmentDetailsPage.putExtra("scheduleTime",formattedDate);
        appointmentDetailsPage.putExtra("AppointmentId",patient.getAppintmentId());
        appointmentDetailsPage.putExtra("userId",patient.getUserId());
        appointmentDetailsPage.putExtra("docId",patient.getDocId());
        appointmentDetailsPage.putExtra("visiting_status",patient.getVisiting_status());

        startActivity(appointmentDetailsPage);
    }

    @Override
    public void onFutureItemClick(int position) {
        //open the appointment history activity

        Patient patient=futurepatients.get(position);

        // Format scheduleTime to the desired format
        SimpleDateFormat dateFormat = new SimpleDateFormat("d MMMM yyyy 'at' hh:mm a", Locale.getDefault());
        String formattedDate = dateFormat.format(patient.getScheduleTime().toDate());

        Intent appointmentDetailsPage=new Intent(DoctorDashboard.this, AppointmentViewDoctor.class);
        appointmentDetailsPage.putExtra("PatientName",patient.getPatientName());
        appointmentDetailsPage.putExtra("PatientMobileNo",patient.getPatientMobileNo());
        appointmentDetailsPage.putExtra("PatientProblem",patient.getPatientProblem());
        appointmentDetailsPage.putExtra("scheduleTime",formattedDate);
        appointmentDetailsPage.putExtra("AppointmentId",patient.getAppintmentId());
        appointmentDetailsPage.putExtra("userId",patient.getUserId());
        appointmentDetailsPage.putExtra("docId",patient.getDocId());
        appointmentDetailsPage.putExtra("visiting_status",patient.getVisiting_status());

        startActivity(appointmentDetailsPage);
    }


}