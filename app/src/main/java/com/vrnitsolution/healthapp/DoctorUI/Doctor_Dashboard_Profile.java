package com.vrnitsolution.healthapp.DoctorUI;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.vrnitsolution.healthapp.GetNotification.AllNotification;
import com.vrnitsolution.healthapp.LoginActivity;
import com.vrnitsolution.healthapp.MainActivity;
import com.vrnitsolution.healthapp.Message.ChatMainActivity;
import com.vrnitsolution.healthapp.Message.MessageActivity;
import com.vrnitsolution.healthapp.PrescriptionHistory;
import com.vrnitsolution.healthapp.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class Doctor_Dashboard_Profile extends AppCompatActivity {
    CircleImageView backBtnImageview;
    TextView textName,textEmail,textUserid;
    CircleImageView profileImage;
    String doctorId="",email="";

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    // Create a reference to the "doctors" collection
    private CollectionReference doctorsCollection = db.collection("doctors");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_dashboard_profile);


//        Window window = this.getWindow();
//        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);



        backBtnImageview=findViewById(R.id.imageView3);
        profileImage=findViewById(R.id.profileImage);
        textName=findViewById(R.id.textName);
        textEmail=findViewById(R.id.textEmail);
        textUserid=findViewById(R.id.textUserid);

        backBtnImageview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    /**
     * In this method calls its open appointment History Activity
     * @param view
     */
    public void AppoinmentHistory(View view) {
        startActivity(new Intent(Doctor_Dashboard_Profile.this,AppointmentHistoryForDoctor.class));
    }


    /**
     * Passing Intent doctorId
     * @param view
     */
    public void PrescriptionHistory(View view) {
        if (doctorId!=null)
        {
            Intent pres=new Intent(Doctor_Dashboard_Profile.this, PrescriptionHistoryForDoctor.class);
            pres.putExtra("doctorId",doctorId);
            startActivity(pres);

        }
    }


    /**
     * From this activity doctor can send Notification
     * */
    public void showPatinetProfile(View view) {
        startActivity(new Intent(Doctor_Dashboard_Profile.this,PatinetProfile.class));
    }

    @Override
    protected void onStart() {
        super.onStart();

        SharedPreferences preferences = getSharedPreferences("DoctorPrefs", MODE_PRIVATE);
        if (preferences != null) {
            // Retrieve doctor data from SharedPreferences
            doctorId = preferences.getString("doctorId", "");
            email = preferences.getString("email", "");

            if (!doctorId.isEmpty() || !email.isEmpty()) {
                getDoctorProfile(doctorId, email);
            } else {
                redirectToLogin();
            }

        } else {
            redirectToLogin();
        }
    }


    /**
     * Get Doctor Profile using DoctorId and Email
     */

    private void getDoctorProfile(String doctorId, String email) {
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

                    textName.setText("Dr. " + dcName);
                    textEmail.setText(dcemail);
                    textUserid.setText(dcId);

                    // Check if the activity is not destroyed before loading the image
                    if (!isDestroyed() && !isFinishing()) {
                        Glide.with(Doctor_Dashboard_Profile.this).load(photoUrl).into(profileImage);
                    }
                }
            }
        });
    }

    private void redirectToLogin() {
        finish();
        finishAffinity();
        startActivity(new Intent(Doctor_Dashboard_Profile.this, DoctorLogin.class));
    }


    public void logOut(View view) {
        SharedPreferences preferences = getSharedPreferences("DoctorPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear(); // Clear all data in the SharedPreferences
        editor.apply();

        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    public void  chatOpen(View view)
    {
        startActivity(new Intent(this, ChatMainActivity.class));
    }

    public void Notification(View view) {
        startActivity(new Intent(this, AllNotification.class));
    }
}