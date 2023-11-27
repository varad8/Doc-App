package com.vrnitsolution.healthapp.bookappointment;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.vrnitsolution.healthapp.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.UUID;

public class BookAppointment extends AppCompatActivity {
    private Calendar calendar;
    EditText PatientName,PatientMobile,Patientproblem,scheduleTime;
    FirebaseAuth mauth;
    String user;
    ProgressDialog progressDialog;
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_appointment);

//
//        Window window = this.getWindow();
//        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        PatientName=findViewById(R.id.patientName);
        PatientMobile=findViewById(R.id.patientMobileNO);
        Patientproblem=findViewById(R.id.problem);
        scheduleTime=findViewById(R.id.scheduleTime);
        imageView=findViewById(R.id.imageView3);


        mauth=FirebaseAuth.getInstance();
        user=mauth.getCurrentUser().getUid();


        calendar = Calendar.getInstance();

        scheduleTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDateTimePicker();
            }
        });


        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });


        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Wait while booking appointment"); // Set your message here
        progressDialog.setCancelable(false); // Set whether the dialog can be canceled by pressing back button

    }

    private void showDateTimePicker() {
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, monthOfYear);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                showTimePicker();
            }
        };

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, dateSetListener,
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

    private void showTimePicker() {
        TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                updateDateTime();
            }
        };

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, timeSetListener,
                calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false);

        timePickerDialog.show();
    }

    private void updateDateTime() {
        String myFormat = "dd/MM/yyyy hh:mm a"; // your desired format
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.getDefault());
        scheduleTime.setText(sdf.format(calendar.getTime()));
    }

    public void BookAppointment(View view) {
        if (user.isEmpty() || PatientName.getText().toString().isEmpty() || PatientMobile.getText().toString().isEmpty()  || Patientproblem.getText().toString().isEmpty() || scheduleTime.getText().toString().isEmpty())
        {
            Toast.makeText(this, "Enter all fields", Toast.LENGTH_SHORT).show();
        }else {
            progressDialog.show();
            // Retrieve data from the Intent
            Intent intent = getIntent();
            String dcId = intent.getStringExtra("doctorId");

            if (intent!=null)
            {
                String Name=PatientName.getText().toString();
                String MobileNo=PatientMobile.getText().toString();
                String Problem=Patientproblem.getText().toString();
                String time=scheduleTime.getText().toString();
                String appointmentId = generateUniqueAppointmentId();
                String userid=user;

                HashMap mp=new HashMap<>();
                mp.put("PatientName",Name);
                mp.put("PatientMobileNo",MobileNo);
                mp.put("PatientProblem",Problem);
                mp.put("scheduleTime",time);
                mp.put("AppintmentId",appointmentId);
                mp.put("userId",userid);
                mp.put("docId",dcId);
                mp.put("visiting_status","Not Visited");


                saveAppointmentData(mp);

            }




        }
    }


    /**
     * This method saved the object that present the appointment data
     * @param mp
     */
    private void saveAppointmentData(HashMap mp) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // Parse the scheduleTime String to Date object
        String scheduleTimeString = mp.get("scheduleTime").toString();
        Date scheduleTimeDate = null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault());
            scheduleTimeDate = sdf.parse(scheduleTimeString);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // Convert Date object to Timestamp
        Timestamp scheduleTimeTimestamp = new Timestamp(scheduleTimeDate);
        // Update the "scheduleTime" value in the HashMap with the Timestamp object
        mp.put("scheduleTime", scheduleTimeTimestamp);

        // Save the updated data in Firestore
        db.collection("appointmentdata")
                .add(mp)
                .addOnSuccessListener(documentReference -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Appointment booked successfully!", Toast.LENGTH_SHORT).show();
                    //Clear the EditText
                    PatientName.setText("");
                    PatientMobile.setText("");
                    Patientproblem.setText("");
                    scheduleTime.setText("");
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Failed to book appointment. Please try again later.", Toast.LENGTH_SHORT).show();
                });
    }


    /**
     * When user book appointmetn this method generate unique appointment ids
     * @return
     */
    public static String generateUniqueAppointmentId() {
        // Get current timestamp in milliseconds
        long timestamp = System.currentTimeMillis();

        // Generate a random UUID
        String randomUUID = UUID.randomUUID().toString();

        // Concatenate timestamp and randomUUID to create a unique ID
        String uniqueId = timestamp + "_" + randomUUID;

        return uniqueId;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}