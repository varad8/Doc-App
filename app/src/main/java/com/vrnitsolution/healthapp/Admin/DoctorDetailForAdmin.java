package com.vrnitsolution.healthapp.Admin;

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
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.vrnitsolution.healthapp.DoctorDeatailsPage;
import com.vrnitsolution.healthapp.Location.MapsActivity;
import com.vrnitsolution.healthapp.R;
import com.vrnitsolution.healthapp.adapter.ServiceHoursAdapter;

import java.io.IOException;
import java.nio.file.FileStore;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DoctorDetailForAdmin extends AppCompatActivity {
    ImageView dcImage;
    TextView doctorname,doctorOccupation,doctorID,doctorEmail,doctorSpecailistIn,services,address;
    RecyclerView consultaionSchedule;
    ImageView imageView,updateaccounstatus;
    private static final String LOCATION_PERMISSION = android.Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String LOCATION_COARSE = android.Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int PERMISSION_REQ_CODE = 1005;
    String dcName,dcEmail;
    String dcSpecialistIn,status,dcId;
    private Geocoder geocoder;

    Map<String, String> coordinates;
    FirebaseFirestore db=FirebaseFirestore.getInstance();
    CollectionReference doctorCollectionref=db.collection("doctors");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_detail_for_admin);


//        Window window = this.getWindow();
//        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);


        imageView=findViewById(R.id.imageView3);
        updateaccounstatus=findViewById(R.id.updateaccounstatus);

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
        address=findViewById(R.id.address);


        geocoder=new Geocoder(this,Locale.getDefault());

        // Retrieve data from the Intent
        Intent intent = getIntent();
        if (intent != null) {
            dcName = intent.getStringExtra("dcname");
            dcId = intent.getStringExtra("dcId");
            dcEmail = intent.getStringExtra("dcemail");
            String dcPhoto = intent.getStringExtra("dcPhoto");
            String dcOccupation = intent.getStringExtra("dcoccupation");
            dcSpecialistIn = intent.getStringExtra("dcSpecialistIn");
            coordinates = (Map<String, String>) intent.getSerializableExtra("coordinates");
            status=intent.getStringExtra("status");


            if (status.equals("Not Approved"))
            {
                updateaccounstatus.setImageDrawable(getResources().getDrawable(R.drawable.baseline_check_24));
            }
            else
            {
                updateaccounstatus.setImageDrawable(getResources().getDrawable(R.drawable.baseline_clear_24));
            }

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


            //check account status and add drawable image on that



            Map<String, String> serviceHours = (Map<String, String>) intent.getSerializableExtra("serviceHours");
            ArrayList<String> availableservices = intent.getStringArrayListExtra("availableServices");


            //set the image in image view using glide
            Glide.with(getApplicationContext())
                    .load(dcPhoto)
                    .into(dcImage);
            doctorname.setText("Dr." + dcName);
            doctorOccupation.setText("(" + dcOccupation + ")");
            doctorID.setText("Doctor Id :- " + dcId);
            doctorEmail.setText("Doctor Email :- " + dcEmail);
            doctorSpecailistIn.setText("Specialist In :- " + dcSpecialistIn);
            services.setText(availableservices.toString());


            LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
            consultaionSchedule.setLayoutManager(layoutManager);


            //set the adapter for service hours in recyclervie
            ServiceHoursAdapter serviceHoursAdapter = new ServiceHoursAdapter(serviceHours);
            consultaionSchedule.setAdapter(serviceHoursAdapter);
        }
    }




    public void sendEmail(View view) {
        Toast.makeText(this, "" + dcEmail, Toast.LENGTH_SHORT).show();

        // Create an Intent with the action ACTION_SEND
        Intent emailIntent = new Intent(Intent.ACTION_SEND);

        // Set the email address in the intent
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{dcEmail});

        // Set the type of the intent to 'message/rfc822'
        emailIntent.setType("message/rfc822");

        // Check if there is an app that can handle this Intent
        if (emailIntent.resolveActivity(getPackageManager()) != null) {
            // Start the activity if an app is available
            startActivity(Intent.createChooser(emailIntent, "Send Email"));
        }
    }


    public void showDirection(View view) {
        requestRuntimePermission();
    }




    /**
     * Checking the Location permission and requesting location permission.
     */
    private void requestRuntimePermission() {
        if ((ActivityCompat.checkSelfPermission(DoctorDetailForAdmin.this, LOCATION_PERMISSION) == PackageManager.PERMISSION_GRANTED) && (ActivityCompat.checkSelfPermission(DoctorDetailForAdmin.this, LOCATION_COARSE) == PackageManager.PERMISSION_GRANTED)) {
            String lat=coordinates.get("latitude");
            String log=coordinates.get("longitude");
            Intent intent=new Intent(DoctorDetailForAdmin.this, MapsActivity.class);
            intent.putExtra("latitude",lat);
            intent.putExtra("longitude",log);
            intent.putExtra("dcName",dcName);
            intent.putExtra("spin",dcSpecialistIn);
            startActivity(intent);

        } else if (ActivityCompat.shouldShowRequestPermissionRationale(DoctorDetailForAdmin.this, LOCATION_PERMISSION) && ActivityCompat.shouldShowRequestPermissionRationale(DoctorDetailForAdmin.this, LOCATION_COARSE)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("This app requires LOCATION PERMISSION  for feature to work as expected.")
                    .setTitle("Permission Required")
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(DoctorDetailForAdmin.this, new String[]{LOCATION_PERMISSION, LOCATION_COARSE}, PERMISSION_REQ_CODE);
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
            } else if ((!ActivityCompat.shouldShowRequestPermissionRationale(DoctorDetailForAdmin.this, LOCATION_PERMISSION) && !(ActivityCompat.shouldShowRequestPermissionRationale(DoctorDetailForAdmin.this, LOCATION_COARSE)))) {
                AlertDialog.Builder builder = new AlertDialog.Builder(DoctorDetailForAdmin.this);
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


    /**
     * This method calls it check account status according that it update status of doctor account (approved/not approved)
     * @param view
     */

    public void updateAccountStatus(View view) {
        if (!status.isEmpty())
        {
            if (status.equals("Not Approved"))
            {
                updateStatus("Approved");
            } else if (status.equals("Approved")) {
                updateStatus("Not Approved");
            }
        }
    }
    private void updateStatus(String accountStatus) {
        // Query to find the doctor with the specified doctorId
        doctorCollectionref.whereEqualTo("doctorId", dcId).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    // Check if the doctor with the specified doctorId exists
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        // Update the account_status field
                        doctorCollectionref.document(document.getId()).update("account_status", accountStatus)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                       displayToast("Account Status Updated");
                                       finish();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        displayToast(e.getMessage().toString());
                                    }
                                });
                    }
                } else {

                }
            }
        });
    }

    public void displayToast(String txt)
    {
        Toast.makeText(this, ""+txt, Toast.LENGTH_SHORT).show();
    }

}