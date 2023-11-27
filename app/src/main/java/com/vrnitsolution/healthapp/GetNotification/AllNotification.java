package com.vrnitsolution.healthapp.GetNotification;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.vrnitsolution.healthapp.GetNotification.adapter.NotificationAdapter;
import com.vrnitsolution.healthapp.GetNotification.model.NotificationModel;
import com.vrnitsolution.healthapp.R;

import java.util.ArrayList;

public class AllNotification extends AppCompatActivity implements NotificationAdapter.OnNotificationClickListner{
    private NotificationAdapter notificationAdapter;
    private ArrayList<NotificationModel>notificationModels;
    private RecyclerView recyclerView;
    private FirebaseFirestore db=FirebaseFirestore.getInstance();
    ImageView imageView3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_notification);

//        Window window = this.getWindow();
//        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        imageView3=findViewById(R.id.imageView3);
        recyclerView = findViewById(R.id.notificationdata);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        notificationModels = new ArrayList<>();
        notificationAdapter = new NotificationAdapter(notificationModels, this, this);
        recyclerView.setAdapter(notificationAdapter);

        // Call the method to retrieve notifications
        getAllNotification();

        imageView3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }


    /**
     * Get Active User nad
     * @return
     */
    public void getAllNotification() {
        notificationModels.clear(); // Assuming notificationModels is a list you want to populate
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        SharedPreferences preferences = getSharedPreferences("DoctorPrefs", MODE_PRIVATE);

        if (currentUser != null) {
            db.collection("messages")
                    .whereEqualTo("userid", currentUser.getUid())
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (notificationModels != null) {
                                notificationModels.clear();
                            }

                            if (task.isSuccessful()) {
                                // Iterate through the QuerySnapshot and add each document to notificationModels
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    NotificationModel notificationModel = document.toObject(NotificationModel.class);
                                    notificationModels.add(notificationModel);
                                }
                                notificationAdapter.notifyDataSetChanged();
                                // Now notificationModels contains the retrieved NotificationModel objects
                            } else {
                                Log.e("FirestormError", "Error getting notifications: ", task.getException());
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("FirestoreError", "Error getting notifications: " + e.getMessage());
                        }
                    });
        } else if (preferences != null) {
            // Retrieve doctor data from SharedPreferences
            String userid = preferences.getString("doctorId", "");
            if (!userid.isEmpty()) {
                db.collection("messages")
                        .whereEqualTo("senderDoctorId", userid)
                        .orderBy("createdAt", Query.Direction.DESCENDING)
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (notificationModels != null) {
                                    notificationModels.clear();
                                }

                                if (task.isSuccessful()) {
                                    // Iterate through the QuerySnapshot and add each document to notificationModels
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        NotificationModel notificationModel = document.toObject(NotificationModel.class);
                                        notificationModels.add(notificationModel);
                                    }
                                    // Now notificationModels contains the retrieved NotificationModel objects
                                    notificationAdapter.notifyDataSetChanged();
                                } else {
                                    Log.e("FirestormError", "Error getting notifications: ", task.getException());
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e("FirestoreError", "Error getting notifications: " + e.getMessage());
                            }
                        });
            }
        }
    }

    @Override
    public void onNotificationClick(int position,NotificationModel notificationModel) {
        //perform click here open another activity to read that notification in other activity
    }
}