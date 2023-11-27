package com.vrnitsolution.healthapp.DoctorUI;

import static android.content.ContentValues.TAG;
import static com.vrnitsolution.healthapp.DoctorUI.AESCrypt.encrypt;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessaging;
import com.vrnitsolution.healthapp.R;
import java.util.HashMap;
import java.util.UUID;

public class RegisterActivity extends AppCompatActivity {
    EditText doctorName, doctorEmail, doctorMobileNo, doctorPassword;

    String dcName, dcEmail, dcMobile, dcPassword;

    ImageView togglebutton;
    private boolean toggle = false;
    String token = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Window window = this.getWindow();
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        doctorName = findViewById(R.id.doctorName);
        doctorEmail = findViewById(R.id.doctorEmail);
        doctorMobileNo = findViewById(R.id.doctorMobile);
        doctorPassword = findViewById(R.id.doctorPassword);
        togglebutton = findViewById(R.id.togglepassword);


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
     * Getting data from edittext and check is not empty
     * data is not empty then send that to DoctorFormPage
     * In that the passeord is encrypt and send
     * */

    public void registerDoctor(View view) {
        dcName = doctorName.getText().toString().trim();
        dcEmail = doctorEmail.getText().toString().trim();
        dcMobile = doctorMobileNo.getText().toString().trim();
        dcPassword = doctorPassword.getText().toString().trim();
        String encryptedPassword=encrypt(dcPassword);

        if (dcName.isEmpty() || dcEmail.isEmpty() || dcMobile.isEmpty() || dcPassword.isEmpty()) {
            Toast.makeText(this, "Enter all fields", Toast.LENGTH_SHORT).show();
        } else if (!((doctorPassword.getText().toString().length()) >= 6)) {
            Toast.makeText(this, "Password must be greater 6 charachter", Toast.LENGTH_SHORT).show();
        } else {
            //send data to Doctor Form Details Page
            HashMap<String, Object> doctorData = new HashMap<>();
            doctorData.put("doctorName", dcName);
            doctorData.put("password", encryptedPassword);
            doctorData.put("email", dcEmail);
            doctorData.put("mobileNo", dcMobile);
            doctorData.put("doctorId", generateUniqueId());
            doctorData.put("messageToken", token);



            Intent intent = new Intent(this, DoctorFormPage.class);
            intent.putExtra("doctordata", doctorData);
            finish();
            startActivity(intent);
        }
    }




    /**
     * This method generate firebase message token accordin that token we perform the POP Notification recived and send for that perticular device
     * */
    private void generateMessageToken() {
        // Get FCM token
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                        return;
                    }

                    // Get the FCM token
                    token = task.getResult();
                    Log.d(TAG, "FCM token: " + token);

                    // Now you can save this token to your Firestore database or handle it as needed
                });
    }

    /**
     * This method generate Doctor Id unique ID
     * */
    public static String generateUniqueId() {
        UUID uniqueId = UUID.randomUUID();
        return uniqueId.toString();
    }

    public void loginDoctor(View view) {
        startActivity(new Intent(this, DoctorLogin.class));
    }

    @Override
    protected void onStart() {
        super.onStart();
        generateMessageToken();

    }


}