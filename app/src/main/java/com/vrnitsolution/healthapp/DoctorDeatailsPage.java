package com.vrnitsolution.healthapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.vrnitsolution.healthapp.Location.MapsActivity;
import com.vrnitsolution.healthapp.Message.MessageActivity;
import com.vrnitsolution.healthapp.Message.SearchUserActivity;
import com.vrnitsolution.healthapp.Message.model.UserModel;
import com.vrnitsolution.healthapp.adapter.ServiceHoursAdapter;
import com.vrnitsolution.healthapp.bookappointment.BookAppointment;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DoctorDeatailsPage extends AppCompatActivity {
    ImageView dcImage;
    TextView doctorname,doctorOccupation,doctorID,doctorEmail,doctorSpecailistIn,services,address;
    RecyclerView consultaionSchedule;
    ImageView imageView;
    FirebaseAuth mauth;
    String userid,useremail,photourl;
    private static final String LOCATION_PERMISSION = android.Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String LOCATION_COARSE = android.Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int PERMISSION_REQ_CODE = 1005;
    String dcName;
    String dcSpecialistIn;
    Map<String, String> coordinates;
    private Geocoder geocoder;
    UserModel model;
    FirebaseFirestore db=FirebaseFirestore.getInstance();
    CollectionReference chatCollectionReference=db.collection("chatusers");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_deatails_page);

//
//        Window window = this.getWindow();
//        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mauth=FirebaseAuth.getInstance();
        userid=mauth.getCurrentUser().getUid();
        useremail=mauth.getCurrentUser().getEmail();
        photourl= String.valueOf(mauth.getCurrentUser().getPhotoUrl());
        address=findViewById(R.id.address);

        imageView=findViewById(R.id.imageView3);
        geocoder=new Geocoder(this, Locale.getDefault());

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });


        dcImage=findViewById(R.id.doctorImages);
        doctorname=findViewById(R.id.doctorname);
        doctorOccupation=findViewById(R.id.doctorOccupation);
        consultaionSchedule=findViewById(R.id.consultaionSchedule);
        doctorSpecailistIn=findViewById(R.id.doctorSpecailistIn);
        doctorID=findViewById(R.id.doctorID);
        doctorEmail=findViewById(R.id.doctorEmail);
        services=findViewById(R.id.services);

        // Retrieve data from the Intent
        Intent intent = getIntent();
        if (intent != null) {
            dcName = intent.getStringExtra("dcname");
            String dcId = intent.getStringExtra("dcId");
            String dcEmail = intent.getStringExtra("dcemail");
            String dcPhoto = intent.getStringExtra("dcPhoto");
            String dcOccupation = intent.getStringExtra("dcoccupation");
            dcSpecialistIn = intent.getStringExtra("dcSpecialistIn");





            coordinates = (Map<String, String>) intent.getSerializableExtra("coordinates");
            Map<String, String> serviceHours = (Map<String, String>) intent.getSerializableExtra("serviceHours");
            ArrayList<String> availableservices = intent.getStringArrayListExtra("availableServices");


            if (coordinates != null && coordinates.containsKey("latitude") && coordinates.containsKey("longitude")) {
                String latitude = coordinates.get("latitude");
                String longitude = coordinates.get("longitude");

                // Convert latitude and longitude to an address
                try {
                    double lat = Double.parseDouble(latitude);
                    double lng = Double.parseDouble(longitude);
                    List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
                    if (addresses != null && addresses.size() > 0) {
                        String fulladdress = addresses.get(0).getAddressLine(0);
                        address.setText("Address :" + fulladdress);
                    } else {
                        address.setText("Address not found");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }



            //set the image in image view using glide
            Glide.with(getApplicationContext())
                            .load(dcPhoto)
                                    .into(dcImage);
            doctorname.setText("Dr."+dcName);
            doctorOccupation.setText("("+dcOccupation+")");
            doctorID.setText("Doctor Id :- "+dcId);
            doctorEmail.setText("Doctor Email :- "+dcEmail);
            doctorSpecailistIn.setText("Specialist In :- "+dcSpecialistIn);
            services.setText(availableservices.toString());


            LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
            consultaionSchedule.setLayoutManager(layoutManager);


            //set the adapter for service hours in recyclervie
            ServiceHoursAdapter serviceHoursAdapter = new ServiceHoursAdapter(serviceHours);
            consultaionSchedule.setAdapter(serviceHoursAdapter);


            //get that doctor data from the collection
            chatCollectionReference.document(dcId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful())
                    {
                        model=task.getResult().toObject(UserModel.class);
                    }
                }
            });

        }
        }



        //When click on Chatwith Doctor then Open Doctor chat screen
       //Saved Doctor id to get that perevious doctor cha
    public void chatWihDoctor(View view) {
        Intent intent=new Intent(DoctorDeatailsPage.this, MessageActivity.class);
        intent.putExtra("userid",model.getUserId());
        intent.putExtra("username",model.getUsername());
        intent.putExtra("fcmtoken",model.getToken());
        intent.putExtra("email",model.getEmail());
        intent.putExtra("profileImage",model.getPhotoUrl());
        intent.putExtra("accountType",model.getAccountType());
        startActivity(intent);
    }

    //Book new appointment when click on this mehtod open new activity that books appointment data
    public void bookAppointment(View view) {
        // Retrieve data from the Intent
        Intent intent = getIntent();
        String dcId = intent.getStringExtra("dcId");

        if (intent!=null)
        {
            Intent i=new Intent(DoctorDeatailsPage.this, BookAppointment.class);
            i.putExtra("doctorId",dcId);
            startActivity(i);
        }


    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
    public void showDirection(View view) {
        requestRuntimePermission();
    }

    /**
     * Checking the Location permission and requesting location permission.
     */
    private void requestRuntimePermission() {
        if ((ActivityCompat.checkSelfPermission(DoctorDeatailsPage.this, LOCATION_PERMISSION) == PackageManager.PERMISSION_GRANTED) && (ActivityCompat.checkSelfPermission(DoctorDeatailsPage.this, LOCATION_COARSE) == PackageManager.PERMISSION_GRANTED)) {
            String lat=coordinates.get("latitude");
            String log=coordinates.get("longitude");
            Intent intent=new Intent(DoctorDeatailsPage.this, MapsActivity.class);
            intent.putExtra("latitude",lat);
            intent.putExtra("longitude",log);
            intent.putExtra("dcName",dcName);
            intent.putExtra("spin",dcSpecialistIn);
            startActivity(intent);

        } else if (ActivityCompat.shouldShowRequestPermissionRationale(DoctorDeatailsPage.this, LOCATION_PERMISSION) && ActivityCompat.shouldShowRequestPermissionRationale(DoctorDeatailsPage.this, LOCATION_COARSE)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("This app requires LOCATION PERMISSION  for feature to work as expected.")
                    .setTitle("Permission Required")
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(DoctorDeatailsPage.this, new String[]{LOCATION_PERMISSION, LOCATION_COARSE}, PERMISSION_REQ_CODE);
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
            } else if ((!ActivityCompat.shouldShowRequestPermissionRationale(DoctorDeatailsPage.this, LOCATION_PERMISSION) && !(ActivityCompat.shouldShowRequestPermissionRationale(DoctorDeatailsPage.this, LOCATION_COARSE)))) {
                AlertDialog.Builder builder = new AlertDialog.Builder(DoctorDeatailsPage.this);
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