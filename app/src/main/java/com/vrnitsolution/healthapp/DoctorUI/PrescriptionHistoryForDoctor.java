package com.vrnitsolution.healthapp.DoctorUI;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.vrnitsolution.healthapp.PrescriptionHistory;
import com.vrnitsolution.healthapp.PrescriptionView;
import com.vrnitsolution.healthapp.R;
import com.vrnitsolution.healthapp.adapter.PrescriptionHistoryAdapter;
import com.vrnitsolution.healthapp.model.Prescription;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class PrescriptionHistoryForDoctor extends AppCompatActivity implements PrescriptionHistoryAdapter.OnItemClickListener {

    CircleImageView imageView3;
    RecyclerView prescriptionhistory;
    PrescriptionHistoryAdapter prescriptionHistoryAdapter;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseFirestore db=FirebaseFirestore.getInstance();
    ArrayList<Prescription> prescriptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prescription_history_for_doctor);

//
//        Window window = this.getWindow();
//        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);


        imageView3=findViewById(R.id.imageView3);
        prescriptionhistory=findViewById(R.id.prescriptionhistory);

        imageView3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        prescriptionhistory.setHasFixedSize(true);
        prescriptionhistory.setLayoutManager(new LinearLayoutManager(this,RecyclerView.HORIZONTAL,false));

        prescriptions = new ArrayList<>();
        prescriptionHistoryAdapter = new PrescriptionHistoryAdapter(PrescriptionHistoryForDoctor.this,prescriptions);
        prescriptionHistoryAdapter.setOnItemClickListener(this);
        prescriptionhistory.setAdapter(prescriptionHistoryAdapter);

    }


    /**
     * Get Prescription data according to DoctorID
     * */
    private void getPrescriptionHistory(String doctorId) {
        prescriptions.clear();
        db.collection("prescriptiondata")
                .whereEqualTo("doctorId", doctorId)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Log.e("Firestore error", error.getMessage());
                            return;
                        }

                        for (DocumentSnapshot document : value.getDocuments()) {
                            Prescription pres = document.toObject(Prescription.class);
                            if (pres != null) {
                                prescriptions.add(pres);
                            }
                        }
                        // Notify the adapter that the data has changed
                        prescriptionHistoryAdapter.notifyDataSetChanged();
                    }
                });
    }

    @Override
    public void onItemClick(Prescription prescription) {
        Intent prescriptionView=new Intent(PrescriptionHistoryForDoctor.this, PrescriptionView.class);
        prescriptionView.putExtra("PatientName",prescription.getPatientName());
        prescriptionView.putExtra("PatientMobile",prescription.getPatientMobileNo());
        prescriptionView.putExtra("doctorId",prescription.getDoctorId());
        prescriptionView.putExtra("appointmentid",prescription.getAppointmentId());
        prescriptionView.putExtra("prescriptionIssueddate",prescription.getPrescriptionissueddate());
        prescriptionView.putExtra("userId",prescription.getUserId());

        //dosage in this format private List<Map<String, String>> dosage  i want to pass in to intent
        // prescription.getDosage()
        // Assuming prescription.getDosage() returns List<Map<String, String>>
        List<Map<String, String>> dosageList = prescription.getDosage();
        Log.d("dosageList",""+dosageList);
        prescriptionView.putExtra("dosageList", (Serializable) dosageList);

        startActivity(prescriptionView);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent=getIntent();

        if (intent!=null)
        {
            getPrescriptionHistory(intent.getStringExtra("doctorId"));
        }
    }
}