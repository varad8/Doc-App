package com.vrnitsolution.healthapp.DoctorUI;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.vrnitsolution.healthapp.LoginActivity;
import com.vrnitsolution.healthapp.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class IntroActivity extends AppCompatActivity {
    CircleImageView backBtnImageview;
    private static final String LOCATION_PERMISSION = android.Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String LOCATION_COARSE = android.Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int PERMISSION_REQ_CODE = 1005;
    AppCompatButton registerbtn, loginBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        Window window = this.getWindow();
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        backBtnImageview = findViewById(R.id.backBtnImageview);
        registerbtn = findViewById(R.id.registerbtn);
        loginBtn = findViewById(R.id.loginBtn);

        backBtnImageview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }


    /**
     * When user allow permission then only jump to that activity other wise not
     */
    public void registerDoctor(View view) {
        startActivity(new Intent(IntroActivity.this, RegisterActivity.class));
    }

    public void loginDoctor(View view) {
        startActivity(new Intent(IntroActivity.this, DoctorLogin.class));
    }

    @Override
    protected void onStart() {
        super.onStart();

        SharedPreferences preferences = getSharedPreferences("DoctorPrefs", MODE_PRIVATE);
        if (preferences != null) {
            // Retrieve doctor data from SharedPreferences
            String doctorId = preferences.getString("doctorId", "");
            String email = preferences.getString("email", "");

            if (!doctorId.isEmpty() && !email.isEmpty()) {
                startActivity(new Intent(IntroActivity.this, DoctorDashboard.class));
                finish();
            }

        }
    }


    /**
     * Checking the Location permission and requesting location permission.
     */
    private void requestRuntimePermission() {
        if ((ActivityCompat.checkSelfPermission(IntroActivity.this, LOCATION_PERMISSION) == PackageManager.PERMISSION_GRANTED) && (ActivityCompat.checkSelfPermission(IntroActivity.this, LOCATION_COARSE) == PackageManager.PERMISSION_GRANTED)) {
            //When permission allow then perform this action


        } else if (ActivityCompat.shouldShowRequestPermissionRationale(IntroActivity.this, LOCATION_PERMISSION) && ActivityCompat.shouldShowRequestPermissionRationale(IntroActivity.this, LOCATION_COARSE)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("This app requires LOCATION PERMISSION  for feature to work as expected.")
                    .setTitle("Permission Required")
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(IntroActivity.this, new String[]{LOCATION_PERMISSION, LOCATION_COARSE}, PERMISSION_REQ_CODE);
                            dialogInterface.dismiss();
                        }
                    })
                    .setNegativeButton("Cancel", (((dialogInterface, i) -> dialogInterface.dismiss())));

            builder.show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{LOCATION_PERMISSION, LOCATION_COARSE}, PERMISSION_REQ_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQ_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted.", Toast.LENGTH_SHORT).show();
            } else if ((!ActivityCompat.shouldShowRequestPermissionRationale(IntroActivity.this, LOCATION_PERMISSION) && !(ActivityCompat.shouldShowRequestPermissionRationale(IntroActivity.this, LOCATION_COARSE)))) {
                AlertDialog.Builder builder = new AlertDialog.Builder(IntroActivity.this);
                builder.setMessage("This feature is unavailabe because this feature requires permission that you have denied."
                                + "Please allow Location permission from settings to proceed further")
                        .setTitle("Permission Required")
                        .setCancelable(false)
                        .setNegativeButton("Cancel", (((dialogInterface, i) -> dialogInterface.dismiss())))
                        .setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", getPackageName(), null);
                                intent.setData(uri);
                                startActivity(intent);
                                dialogInterface.dismiss();
                            }
                        });

                builder.show();
            }
        } else {
            requestRuntimePermission();
        }
    }
}