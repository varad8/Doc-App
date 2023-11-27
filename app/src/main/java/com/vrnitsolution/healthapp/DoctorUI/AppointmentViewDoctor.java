package com.vrnitsolution.healthapp.DoctorUI;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.vrnitsolution.healthapp.R;
import com.vrnitsolution.healthapp.bookappointment.model.Patient;
import com.vrnitsolution.healthapp.model.Doctors;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class AppointmentViewDoctor extends AppCompatActivity {
    TextView patientName,patientMobileNO,problem,scheduleTime,patientAddress,textView5,textView6;
    String docId="";
    String userid="";
    AppCompatButton addPrescription;

    CircleImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment_view_doctor);

//        Window window = this.getWindow();
//        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        patientName = findViewById(R.id.patientName);
        patientMobileNO=findViewById(R.id.patientMobileNO);
        problem=findViewById(R.id.problem);
        scheduleTime=findViewById(R.id.scheduleTime);
        patientAddress=findViewById(R.id.patientAddress);
        textView5=findViewById(R.id.textView5);
        textView6=findViewById(R.id.textView6);
        addPrescription=findViewById(R.id.addPrescription);
        imageView=findViewById(R.id.backBtnImageview);

        imageView.setOnClickListener(v->{
            finish();
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
            userid=intentData.getStringExtra("userId");

            if (intentData.getStringExtra("visiting_status").equals("Visited"))
            {
                addPrescription.setClickable(false);
                addPrescription.setVisibility(View.GONE);
            }

            getAddressofUser(userid);
        }

        addPrescription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent generatePrescription=new Intent(AppointmentViewDoctor.this,GeneratePrescription.class);
                generatePrescription.putExtra("doctorId",docId);
                generatePrescription.putExtra("userid",userid);
                generatePrescription.putExtra("PatientName",intentData.getStringExtra("PatientName"));
                generatePrescription.putExtra("patientMobileNO",intentData.getStringExtra("PatientMobileNo"));
                generatePrescription.putExtra("AppointmentId",intentData.getStringExtra("AppointmentId"));
                generatePrescription.putExtra("PatientProblem",intentData.getStringExtra("PatientProblem"));
                startActivity(generatePrescription);
            }
        });

    }



    /**
     * This methods getting the patient address
     * */
    private void getAddressofUser(String uid) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .whereEqualTo("uid", uid)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            Map<String, String> coordinates = (Map<String, String>) document.get("currentCoordinates");


                            if (coordinates != null && coordinates.containsKey("latitude") && coordinates.containsKey("longitude")) {
                                String latitude = coordinates.get("latitude");
                                String longitude = coordinates.get("longitude");
                                getAddress(latitude,longitude);

                            }

                        }
                    } else {
                        Exception exception = task.getException();
                        if (exception != null) {
                            Log.e("Firestore", "Error getting documents: " + exception.getMessage());
                        }
                    }
                });
    }
    private void getAddress(String latitude, String longitude) {
        double lat = Double.parseDouble(latitude);
        double lng = Double.parseDouble(longitude);

        Geocoder geocoder = new Geocoder(this);
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            if (addresses != null && addresses.size() > 0) {
                Address address = addresses.get(0);
                String fullAddress = address.getAddressLine(0); // Get the full address including street, city, etc.
                patientAddress.setText("Patient Address : " + fullAddress);
            } else {
                patientAddress.setText("Address not found");
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("ErrorAppointment",e.getMessage());
        }
    }

}