package com.vrnitsolution.healthapp.DoctorUI;

import static android.content.Context.MODE_PRIVATE;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.vrnitsolution.healthapp.GetNotification.AllNotification;
import com.vrnitsolution.healthapp.R;
import com.vrnitsolution.healthapp.model.Doctors;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SendNotification extends AppCompatActivity {
    private String username = "";
    private String token = "";
    private String userid = "";
    private TextView textUsername;
    private EditText notificationTitle;
    private EditText notificationBody;
    private EditText notificationImageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_notification);
//        Window window = this.getWindow();
//        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        textUsername = findViewById(R.id.username);
        notificationTitle = findViewById(R.id.notificationTitle);
        notificationImageUrl = findViewById(R.id.imageUrl);
        notificationBody = findViewById(R.id.notificationbody);

        Intent sendMessage = getIntent();

        if (sendMessage != null) {
            username = sendMessage.getStringExtra("username");
            token = sendMessage.getStringExtra("token");
            userid = sendMessage.getStringExtra("userid");

            textUsername.setText("to " + username);
        }
    }

    public void sendNotification(View view) {
        String title = notificationTitle.getText().toString().trim();
        String body = notificationBody.getText().toString();
        String imageLink = notificationImageUrl.getText().toString();

        if (!title.isEmpty() && !body.isEmpty() && !username.isEmpty() && !token.isEmpty()) {
            try {
                // Send Notification Logic here
                JSONObject jsonObject = new JSONObject();

                JSONObject notificationObject = new JSONObject();
                notificationObject.put("title", title);
                notificationObject.put("body", body);
                notificationObject.put("image", imageLink);

                jsonObject.put("notification", notificationObject);
                jsonObject.put("to", token);

                // Pass the context (this) to the ApiClient
                ApiClient apiClient = new ApiClient();
                apiClient.sendNotification(this, jsonObject,userid,body,title,imageLink);

            } catch (JSONException e) {
                Log.e("JSONERROR", "" + e.getMessage());
            }

        } else {
            Toast.makeText(this, "Notification Title and Message should not be blank", Toast.LENGTH_SHORT).show();
        }
    }
}

class ApiClient {
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final String FCM_URL = "https://fcm.googleapis.com/fcm/send";
    private static final String FCM_AUTH_HEADER = "Bearer YOUR_FCM_AUTH_TOKEN"; // Replace with your FCM auth token

    private final OkHttpClient client = new OkHttpClient();

    void sendNotification(Context context, JSONObject jsonObject, String userid, String body, String title, String imageLink) {
        RequestBody requestBody = RequestBody.create(jsonObject.toString(), JSON);
        Request request = new Request.Builder()
                .url(FCM_URL)
                .post(requestBody)
                .header("Authorization", FCM_AUTH_HEADER)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("ApiError", "Failed to send notification: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                Log.d("ApiResponse", "Notification sent successfully");

                // Use the provided context to access SharedPreferences
                saveMessage(context,userid,body,title,imageLink);
            }
        });
    }

    private void saveMessage(Context context, String userid, String body, String title, String imageLink) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        SharedPreferences preferences = context.getSharedPreferences("DoctorPrefs", MODE_PRIVATE);

        if (preferences != null) {
            // Retrieve doctor data from SharedPreferences
            String doctorid = preferences.getString("doctorId", "");

            db.collection("doctors")
                    .whereEqualTo("doctorId", doctorid)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    // Assuming Doctors is a model class representing your Firestore document
                                    Doctors doctor = document.toObject(Doctors.class);

                                    // Now you can use the retrieved doctor object as needed
                                    // For example, you can save the message to Firestore
                                    saveMessageToFirestore(doctor, userid,db,body,title,imageLink,context);
                                }
                            } else {
                                Log.e("FirestoreError", "Error getting doctor document: ", task.getException());
                            }
                        }
                    });
        }
    }

    private void saveMessageToFirestore(Doctors doctor, String userid, FirebaseFirestore db, String body, String title, String imageLink, Context context) {
        HashMap<String,Object>data=new HashMap<>();
        data.put("userid",userid);
        data.put("message",body);
        data.put("sentBy",doctor.getDoctorName());
        data.put("senderDoctorId",doctor.getDoctorId());
        data.put("dcPhoto", doctor.getPhotoUrl());
        data.put("notificationImage",imageLink);
        data.put("createdAt", Timestamp.now());
        data.put("title",title);


        db.collection("messages")
                .add(data)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d("FirestoreSuccess", "Message added with ID: " + documentReference.getId());
                        Toast.makeText(context, "Notification sent successfully", Toast.LENGTH_SHORT).show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("FirestoreError", "Error adding message", e);
                    }
                });
    }

}




