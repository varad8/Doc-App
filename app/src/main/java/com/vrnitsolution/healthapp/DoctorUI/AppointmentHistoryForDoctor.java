package com.vrnitsolution.healthapp.DoctorUI;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.vrnitsolution.healthapp.AppointmentDetailsPage;
import com.vrnitsolution.healthapp.AppointmentHistory;
import com.vrnitsolution.healthapp.R;
import com.vrnitsolution.healthapp.bookappointment.adapter.AppointmentAdapter;
import com.vrnitsolution.healthapp.bookappointment.model.Patient;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class AppointmentHistoryForDoctor extends AppCompatActivity implements AppointmentAdapter.OnApppointmentClickListner {
    RecyclerView appointmetnHistory;
    private AppointmentAdapter appointmentAdapter;
    ArrayList<Patient> patients;
    CircleImageView imageView3;


    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment_history_for_doctor);


//        Window window = this.getWindow();
//        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        imageView3 = findViewById(R.id.imageView3);

        appointmetnHistory = findViewById(R.id.recyclerView);
        //Set RecyclerView Active Appointments

        appointmetnHistory.setHasFixedSize(true);
        appointmetnHistory.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));

        patients = new ArrayList<>();
        appointmentAdapter = new AppointmentAdapter(AppointmentHistoryForDoctor.this, patients, this);
        appointmetnHistory.setAdapter(appointmentAdapter);


        imageView3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

    }


    /**
     * Get All Appointments from the collection according to that doctorId
     */
    private void getAllAppointments(String doctorId) {
        patients.clear();
        db.collection("appointmentdata")
                .whereEqualTo("docId", doctorId)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Log.e("Firestore error", error.getMessage());
                            return;
                        }

                        for (DocumentSnapshot document : value.getDocuments()) {
                            Patient patient = document.toObject(Patient.class);
                            if (patient != null) {
                                patients.add(patient);
                            }
                        }
                        // Notify the adapter that the data has changed
                        appointmentAdapter.notifyDataSetChanged();
                    }
                });
    }


    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences preferences = getSharedPreferences("DoctorPrefs", MODE_PRIVATE);
        if (preferences != null) {
            // Retrieve doctor data from SharedPreferences
            String doctorId = preferences.getString("doctorId", "");
            String email = preferences.getString("email", "");

            if (!doctorId.isEmpty() || !email.isEmpty()) {
                getAllAppointments(doctorId);
            } else {
                redirectToLogin();
            }

        } else {
            redirectToLogin();
        }
    }


    private void redirectToLogin() {
        finish();
        finishAffinity();
        startActivity(new Intent(AppointmentHistoryForDoctor.this, DoctorLogin.class));
    }


    @Override
    protected void onStop() {
        super.onStop();

    }


    @Override
    public void onAppointmentClick(int position) {
        //open the appointment history activity
        Patient patient = patients.get(position);
        // Format scheduleTime to the desired format
        SimpleDateFormat dateFormat = new SimpleDateFormat("d MMMM yyyy 'at' hh:mm a", Locale.getDefault());
        String formattedDate = dateFormat.format(patient.getScheduleTime().toDate());

        Intent appointmentDetailsPage = new Intent(AppointmentHistoryForDoctor.this, AppointmentViewDoctor.class);
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
}