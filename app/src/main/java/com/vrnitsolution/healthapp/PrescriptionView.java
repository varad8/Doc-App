package com.vrnitsolution.healthapp;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfDocument;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfWriter;
import com.vrnitsolution.healthapp.adapter.DosageAdapter;
import com.vrnitsolution.healthapp.model.Doctors;
import com.vrnitsolution.healthapp.model.DosageUser;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PrescriptionView extends AppCompatActivity {
    ImageView imageView3;

    TextView doctorName,doctorAddress,patientName,patinetMobile,appointmentid;
    RecyclerView dosagerecyclerview;
    ConstraintLayout downloadlayout;
    Bitmap bitmap;
    String dirpath;
    private static final int PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE = 100;
    String pname,pmobileno,aptid,doctorid,userid,pissuedate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prescription_view);

        imageView3=findViewById(R.id.imageView3);
        doctorName=findViewById(R.id.doctorName);
        doctorAddress=findViewById(R.id.doctorAddress);
        patientName=findViewById(R.id.patientName);
        patinetMobile=findViewById(R.id.patinetMobile);
        appointmentid=findViewById(R.id.appointmentid);
        downloadlayout=findViewById(R.id.downloadlayout);

        dosagerecyclerview=findViewById(R.id.dosagerecyclerview);
        dosagerecyclerview.setHasFixedSize(true);
        dosagerecyclerview.setLayoutManager(new LinearLayoutManager(this));



     //onback pressed
     imageView3.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View view) {
             finish();
         }
     });

    }

    @Override
    protected void onStart() {
        super.onStart();

        Intent intent = getIntent();

        if (intent!=null)
        {


            pname=intent.getStringExtra("PatientName");
            pmobileno=intent.getStringExtra("PatientMobile");
            aptid=intent.getStringExtra("appointmentid");
            doctorid=intent.getStringExtra("doctorId");
            userid=intent.getStringExtra("userId");
            pissuedate=intent.getStringExtra("prescriptionIssueddate");
            // Retrieve the dosageList from the Intent
            List<Map<String, String>> dosageList = (List<Map<String, String>>) intent.getSerializableExtra("dosageList");


            List<DosageUser> convertedDosageList = new ArrayList<>();


            for (Map<String, String> dosageMap : dosageList) {
                String dosageAfterMill = dosageMap.get("dosageAfterMill");
                String dosageName = dosageMap.get("dosageName");
                String dosageRemark = dosageMap.get("dosageRemark");
                String dosageTime = dosageMap.get("dosageTime");

                DosageUser dosageUser  = new DosageUser(dosageAfterMill, dosageName, dosageRemark, dosageTime);
                convertedDosageList.add(dosageUser);

                Log.d("adapter",""+dosageUser.getDosageName());

            }




            DosageAdapter adapter = new DosageAdapter(convertedDosageList);
            dosagerecyclerview.setAdapter(adapter);



            if (doctorid!=null)
           {
               getDoctorDetailsUsingId(doctorid);
           }

           patientName.setText("Patient Name :"+pname);
           patinetMobile.setText("Patient Mobile No :"+pmobileno);
           appointmentid.setText("APT. ID. :"+aptid);

        }

    }

    private void getDoctorDetailsUsingId(String doctorid) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("doctors")
                .whereEqualTo("doctorId", doctorid)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            Doctors doctor = document.toObject(Doctors.class);
                            // Now you have the doctor object, you can use its properties as needed
                            String dcName = doctor.getDoctorName();
                            String occupation=doctor.getOccupation();
                            // Extract latitude and longitude from the coordinates map
                            Map<String, String> coordinates = doctor.getCoordinates();


                            if (coordinates != null && coordinates.containsKey("latitude") && coordinates.containsKey("longitude")) {
                                String latitude = coordinates.get("latitude");
                                String longitude = coordinates.get("longitude");

                                getAddress(latitude,longitude,dcName,occupation);

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

    private void getAddress(String latitude, String longitude, String dcName, String occupation) {
        double lat = Double.parseDouble(latitude);
        double lng = Double.parseDouble(longitude);

        Geocoder geocoder = new Geocoder(this);
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            if (addresses != null && addresses.size() > 0) {
                Address address = addresses.get(0);
                String fullAddress = address.getAddressLine(0); // Get the full address including street, city, etc.
                doctorAddress.setText("Address: " + fullAddress);
                doctorName.setText("Dr."+dcName+" ("+occupation+")");
            } else {
                doctorAddress.setText("Address not found");
            }
        } catch (IOException e) {
            e.printStackTrace();
            doctorAddress.setText("Error: " + e.getMessage());
        }
    }

    public void DownloadPrescription(View view) {
        if (checkPermission()) {
            downloadPrescription();
        } else {
            requestPermission();
        }
    }

    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int result = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
            return result == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                downloadPrescription();
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void downloadPrescription() {
        downloadlayout.setDrawingCacheEnabled(true);
        downloadlayout.buildDrawingCache();
        Bitmap bm = downloadlayout.getDrawingCache();
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("application/pdf");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        File f = new File(getExternalFilesDir(null), "images.jpg");
        try {
            f.createNewFile();
            FileOutputStream fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());
            imageToPDF();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void imageToPDF() {
        try {
            String fileName = "Prescription_" + aptid + ".pdf";
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File pdfFile = new File(downloadsDir, fileName);

            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
            document.open();

            // Use the correct file path where the image is saved
            File imageFile = new File(getExternalFilesDir(null), "images.jpg");
            Image img = Image.getInstance(imageFile.getAbsolutePath());
            img.scaleToFit(595, 750);
            img.setAlignment(Image.ALIGN_CENTER);
            document.add(img);

            document.close();

            Toast.makeText(this, "PDF Saved Successfully: " + pdfFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error creating PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

}