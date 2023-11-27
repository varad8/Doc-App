package com.vrnitsolution.healthapp.DoctorUI;
import static com.vrnitsolution.healthapp.DoctorUI.AESCrypt.encrypt;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.vrnitsolution.healthapp.R;

public class DoctorLogin extends AppCompatActivity implements LocationListener {
    EditText doctorEmail, doctorPassword;
    String dcEmail, dcPassword;
    ImageView togglebutton;
    private boolean toggle = false;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    // Create a reference to the "doctors" collection
    private CollectionReference doctorsCollection = db.collection("doctors");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login2);



//        Window window = this.getWindow();
//        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        doctorEmail = findViewById(R.id.doctorEmail);
        doctorPassword = findViewById(R.id.doctorPassword);
        togglebutton = findViewById(R.id.togglepassword);

        //password toggle
        togglebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (toggle == false) {
                    toggle = true;
                    togglebutton.setImageResource(R.drawable.baseline_visibility_on);
                    doctorPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                } else {
                    toggle = false;
                    togglebutton.setImageResource(R.drawable.baseline_visibility_off);
                    doctorPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });


    }

    /**
     * In this method check the data is not empty
     */
    public void loginDoctor(View view) {
        dcEmail = doctorEmail.getText().toString().trim();
        dcPassword = doctorPassword.getText().toString().trim();
        String encryptPass=encrypt(dcPassword);

        if (dcEmail.isEmpty() && dcPassword.isEmpty()) {
            Toast.makeText(this, "please enter credentials", Toast.LENGTH_SHORT).show();
        } else {
            doctorAuthCheck(dcEmail, encryptPass);
        }
    }

    /**
     * Using this method check that Email and password is present in Firestore collection Doctors
     */
    private void doctorAuthCheck(String dcEmail, String encryptPass) {
        // Check if the provided email and password match the stored credentials in Firestore
        doctorsCollection.whereEqualTo("email", dcEmail)
                .whereEqualTo("password", encryptPass)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Matching doctor found in Firestore
                        DocumentSnapshot doctorDocument = queryDocumentSnapshots.getDocuments().get(0);

                        // Retrieve doctor's data
                        String doctorId = doctorDocument.getString("doctorId");
                        String email = doctorDocument.getString("email");
                        String photoUrl = doctorDocument.getString("photoUrl");
                        String mobileNo = doctorDocument.getString("mobileNo");

                        // Save the data in SharedPreferences
                        saveDoctorDataToSharedPreferences(doctorId, email, photoUrl, mobileNo);

                    } else {
                        // No matching doctor found
                        Toast.makeText(this, "Invalid email or password.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle any errors during the query
                    Toast.makeText(this, "Error checking authentication: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    /**
     * Saved that data in SharedPreference for checking if logged in
     * */

    private void saveDoctorDataToSharedPreferences(String doctorId, String email, String photoUrl, String mobileNo) {
        SharedPreferences preferences = getSharedPreferences("DoctorPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        // Save doctor data in SharedPreferences
        editor.putString("doctorId", doctorId);
        editor.putString("email", email);
        editor.putString("photoUrl", photoUrl);
        editor.putString("mobileNo", mobileNo);

        // Commit the changes
        editor.apply();


        finish();
        finishAffinity();
        startActivity(new Intent(DoctorLogin.this,DoctorDashboard.class));
    }


    @Override
    protected void onStart() {
        super.onStart();
    }

    public void registerDoctor(View view) {
        finish();
        startActivity(new Intent(DoctorLogin.this,RegisterActivity.class));
    }



    @Override
    public void onLocationChanged(@NonNull Location location) {

    }
}