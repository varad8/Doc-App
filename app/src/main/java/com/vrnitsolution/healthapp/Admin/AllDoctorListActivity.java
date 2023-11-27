package com.vrnitsolution.healthapp.Admin;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.vrnitsolution.healthapp.Admin.adapter.DoctorAdapterNew;
import com.vrnitsolution.healthapp.R;
import com.vrnitsolution.healthapp.model.Doctors;

import java.util.ArrayList;
import java.util.HashMap;

public class AllDoctorListActivity extends AppCompatActivity implements DoctorAdapterNew.OnDoctorProfileClickListner{
    ImageView imageView;
    RecyclerView recyclerView;
    ArrayList<Doctors> doctors;
    DoctorAdapterNew doctorAdapterNew;
    FirebaseFirestore db=FirebaseFirestore.getInstance();
    CollectionReference doctorCollectionref=db.collection("doctors");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_doctor_list);

//        Window window = this.getWindow();
//        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        imageView=findViewById(R.id.imageView3);
        imageView.setOnClickListener(v->{
            finish();
        });

        recyclerView=findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));

        doctors=new ArrayList<>();

        doctorAdapterNew=new DoctorAdapterNew(this,doctors,this);
        recyclerView.setAdapter(doctorAdapterNew);


        getAllDoctor();
    }


    /**
     * This method get All doctor from the doctors collection firestore
     */
    private void getAllDoctor() {
        doctorCollectionref.addSnapshotListener((value, error) -> {
            if (error != null) {
                // Handle the error
                return;
            }

            if (value != null) {
                doctors.clear(); // Clear the existing list before adding new data

                for (Doctors doctor : value.toObjects(Doctors.class)) {
                    doctors.add(doctor);
                }

                // Notify the adapter that the data set has changed
                doctorAdapterNew.notifyDataSetChanged();
            }
        });
    }


    /**
     * Performing onClick event listner when user click on that doctor it opens Doctor Details page that shows doctor details
     * @param position
     * @param doctors
     */
    @Override
    public void onDoctorProfileClick(int position, Doctors doctors) {
        Intent intent=new Intent(AllDoctorListActivity.this, DoctorDetailForAdmin.class);
        intent.putExtra("dcname",doctors.getDoctorName());
        intent.putExtra("dcId",doctors.getDoctorId());
        intent.putExtra("dcemail",doctors.getEmail());
        intent.putExtra("dcPhoto",doctors.getPhotoUrl());

        // Pass availableServices map to the next activity
        intent.putStringArrayListExtra("availableServices", doctors.getAvailableservices());
        intent.putExtra("dcoccupation",doctors.getOccupation());
        intent.putExtra("coordinates",new HashMap<>(doctors.getCoordinates()));
        intent.putExtra("serviceHours",new HashMap<>(doctors.getServicehours()));
        intent.putExtra("dcSpecialistIn",doctors.getSpecialistIn());
        intent.putExtra("status",doctors.getAccount_status());
        startActivity(intent);

    }
}