package com.vrnitsolution.healthapp.DashboardProfile;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.vrnitsolution.healthapp.AppointmentHistory;
import com.vrnitsolution.healthapp.GetNotification.AllNotification;
import com.vrnitsolution.healthapp.LoginActivity;
import com.vrnitsolution.healthapp.MainActivity;
import com.vrnitsolution.healthapp.Message.ChatMainActivity;
import com.vrnitsolution.healthapp.PrescriptionHistory;
import com.vrnitsolution.healthapp.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class DashboardProfile extends AppCompatActivity {

    FirebaseAuth mauth;
    String userEmail,userName,userId;
    TextView textUserid,textEmail,textName;

    ImageView imageView;
    CircleImageView profileImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard_profile);

//
//        Window window = this.getWindow();
//        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mauth=FirebaseAuth.getInstance();


        textEmail=findViewById(R.id.textEmail);
        textUserid=findViewById(R.id.textUserid);
        textName=findViewById(R.id.textName);
        imageView=findViewById(R.id.imageView3);
        profileImage=findViewById(R.id.profileImage);


        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        getUserProfile();
    }

    private void getUserProfile() {
        userId=mauth.getCurrentUser().getUid();
        userEmail=mauth.getCurrentUser().getEmail();
        userName=mauth.getCurrentUser().getDisplayName();

        textName.setText(userName);
        textEmail.setText(userEmail);
        textUserid.setText(userId);

        Glide.with(getApplicationContext()).load(mauth.getCurrentUser().getPhotoUrl()).fitCenter().into(profileImage);

    }

    public void AppoinmentHistory(View view) {
        startActivity(new Intent(DashboardProfile.this, AppointmentHistory.class));
    }

    public void PrescriptionHistory(View view) {
        startActivity(new Intent(DashboardProfile.this, PrescriptionHistory.class));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void OtherReport(View view) {
    }

    public void chatOpen(View view) {
        startActivity(new Intent(this, ChatMainActivity.class));
    }

    public void logOut(View view) {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    public void Notification(View view) {
        startActivity(new Intent(this, AllNotification.class));
    }
}