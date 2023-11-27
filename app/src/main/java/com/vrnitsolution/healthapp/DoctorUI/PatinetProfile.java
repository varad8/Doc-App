package com.vrnitsolution.healthapp.DoctorUI;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.vrnitsolution.healthapp.DoctorUI.adapter.PatientProfileAdapter;
import com.vrnitsolution.healthapp.DoctorUI.model.ProfileForMessage;
import com.vrnitsolution.healthapp.R;
import com.vrnitsolution.healthapp.bookappointment.adapter.HistoryAppointment;

import java.util.ArrayList;
import java.util.HashSet;

import de.hdodenhof.circleimageview.CircleImageView;

public class PatinetProfile extends AppCompatActivity implements PatientProfileAdapter.OnProfileClickListner {
    CircleImageView backBtnImageview;
    RecyclerView profileRecyclerView;
    ArrayList<ProfileForMessage> profileForMessages;
    PatientProfileAdapter patientProfileAdapter;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patinet_profile);

//        Window window = this.getWindow();
//        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        backBtnImageview=findViewById(R.id.backBtnImageview);
        profileRecyclerView=findViewById(R.id.profileRecyclerView);
        profileRecyclerView=findViewById(R.id.profileRecyclerView);


        profileRecyclerView.setHasFixedSize(true);
        profileRecyclerView.setLayoutManager(new LinearLayoutManager(PatinetProfile.this,LinearLayoutManager.HORIZONTAL,false));

        profileForMessages=new ArrayList<>();
        patientProfileAdapter=new PatientProfileAdapter(profileForMessages,PatinetProfile.this,this);
        profileRecyclerView.setAdapter(patientProfileAdapter);


        backBtnImageview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }



    @Override
    protected void onStart() {
        super.onStart();

        SharedPreferences preferences = getSharedPreferences("DoctorPrefs", MODE_PRIVATE);
        if (preferences != null) {
            // Retrieve doctor data from SharedPreferences
            String doctorId = preferences.getString("doctorId", "");
            String email = preferences.getString("email", "");

            if (!doctorId.isEmpty() || !email.isEmpty()  ) {
                getPatientProfiles(doctorId);
            }
            else
            {
                redirectToLogin();
            }

        }
        else
        {
            redirectToLogin();
        }
    }


    /**
     * Using this Method we fetch the all userId from the appointmentdata collection where doctorId match found
     * @param doctorId
     */
    private void getPatientProfiles(String doctorId) {
        // Reference to the Firestore collection
        CollectionReference appointmentDataRef = db.collection("appointmentdata");

        // Use a HashSet to store processed userIds
        HashSet<String> processedUserIds = new HashSet<>();

        appointmentDataRef.whereEqualTo("docId", doctorId)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            // Handle errors here
                            return;
                        }

                        // Iterate through the query results
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            String userId = documentSnapshot.getString("userId");

                            // Check if the userId is not in the processed set
                            if (!processedUserIds.contains(userId)) {
                                // Add userId to the processed set
                                processedUserIds.add(userId);

                                // Call the method to fetch user data based on userId
                                fetchUserData(userId);
                            }
                        }

                        Log.d("UserID",""+processedUserIds);
                    }
                });
    }

    /**
     * Using this Method when get all users when userId match in the Users collection {userId:uid}
     * @param userId
     */
    private void fetchUserData(String userId) {
        Toast.makeText(this, ""+userId, Toast.LENGTH_SHORT).show();
        // Reference to the Firestore collection for user data
        CollectionReference userRef = db.collection("users");

        // Query to fetch user data based on userId
        userRef.whereEqualTo("uid", userId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        // Iterate through the query results and add user data to profileForMessages
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            ProfileForMessage profile = documentSnapshot.toObject(ProfileForMessage.class);
                            profileForMessages.add(profile);
                            Log.d("ProfileMessage",""+profile);
                        }

                        // Notify the adapter that the data set has changed
                        patientProfileAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle errors here
                        Log.e("Errror",e.getMessage());
                    }
                });
    }

    private void redirectToLogin() {
        finish();
        finishAffinity();
        startActivity(new Intent(PatinetProfile.this, DoctorLogin.class));
    }


    @Override
    public void onProfileClicks(int position) {
        Intent sendMessage=new Intent(PatinetProfile.this,SendNotification.class);
        sendMessage.putExtra("userid",profileForMessages.get(position).getUid());
        sendMessage.putExtra("username",profileForMessages.get(position).getUsername());
        sendMessage.putExtra("token",profileForMessages.get(position).getMessageToken());
        startActivity(sendMessage);
    }

}