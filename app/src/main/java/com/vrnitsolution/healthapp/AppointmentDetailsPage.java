package com.vrnitsolution.healthapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.vrnitsolution.healthapp.model.Doctors;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AppointmentDetailsPage extends AppCompatActivity {
    ImageView imageView;
    TextView patientName,patientMobileNO,problem,scheduleTime,doctorNameTextView,doctorAddress,textView5,textView6;

    String docId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment_details_page);


//        Window window = this.getWindow();
//        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        imageView=findViewById(R.id.imageView3);
        patientName = findViewById(R.id.patientName);
        patientMobileNO=findViewById(R.id.patientMobileNO);
        problem=findViewById(R.id.problem);
        scheduleTime=findViewById(R.id.scheduleTime);
        doctorNameTextView=findViewById(R.id.doctorName);
        doctorAddress=findViewById(R.id.doctorAddress);
        textView5=findViewById(R.id.textView5);
        textView6=findViewById(R.id.textView6);


        //perform user back pressed
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });


        //get Intent data

        Intent intentData=getIntent();

        if (intentData!=null)
        {
            patientName.setText("Patient Name : "+intentData.getStringExtra("PatientName"));
            patientMobileNO.setText("Patient Mobile No : "+intentData.getStringExtra("PatientMobileNo"));
            problem.setText("Patient Problem : "+intentData.getStringExtra("PatientProblem"));
            scheduleTime.setText("Schedule Time : "+intentData.getStringExtra("scheduleTime"));
            textView5.setText("Appointment Id : "+intentData.getStringExtra("AppointmentId"));
            textView6.setText("Visiting Status : "+intentData.getStringExtra("visiting_status"));

            docId=intentData.getStringExtra("docId");


            getDoctorData(docId);
        }

    }

    private void getDoctorData(String docId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("doctors")
                .whereEqualTo("doctorId", docId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            Doctors doctor = document.toObject(Doctors.class);
                            // Now you have the doctor object, you can use its properties as needed
                            String doctorName = doctor.getDoctorName();
                            // Extract latitude and longitude from the coordinates map
                            Map<String, String> coordinates = doctor.getCoordinates();


                            if (coordinates != null && coordinates.containsKey("latitude") && coordinates.containsKey("longitude")) {
                                String latitude = coordinates.get("latitude");
                                String longitude = coordinates.get("longitude");

                                getAddress(latitude,longitude,doctorName);

                            }

                        }
                    } else {
                        // Handle errors that occurred while fetching data from Firestore
                        // For example, you can log the error message
                        Exception exception = task.getException();
                        if (exception != null) {
                            // Log the error message
                            Log.e("Firestore", "Error getting documents: " + exception.getMessage());
                        }
                    }
                });
    }

    private void getAddress(String latitude, String longitude, String doctorName) {
        double lat = Double.parseDouble(latitude);
        double lng = Double.parseDouble(longitude);

        Geocoder geocoder = new Geocoder(this);
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            if (addresses != null && addresses.size() > 0) {
                Address address = addresses.get(0);
                String fullAddress = address.getAddressLine(0); // Get the full address including street, city, etc.
                doctorAddress.setText("Doctor Address : " + fullAddress);
                doctorNameTextView.setText("Doctor Name : "+doctorName);
            } else {
                doctorAddress.setText("Address not found");
            }
        } catch (IOException e) {
            e.printStackTrace();
            doctorAddress.setText("Error: " + e.getMessage());
        }
    }


}