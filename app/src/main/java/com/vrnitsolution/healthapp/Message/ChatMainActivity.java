package com.vrnitsolution.healthapp.Message;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.vrnitsolution.healthapp.Message.adapter.RecentChatProfileAdapter;
import com.vrnitsolution.healthapp.Message.model.ChatMessageModel;
import com.vrnitsolution.healthapp.Message.model.ChatroomModel;
import com.vrnitsolution.healthapp.Message.model.UserModel;
import com.vrnitsolution.healthapp.R;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatMainActivity extends AppCompatActivity implements RecentChatProfileAdapter.OnResentUserClickListner{
    CircleImageView profileimage, searchimage;
    RecyclerView recyclerView;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    // Create a reference to the "doctors" collection
    private CollectionReference allcollection = db.collection("chatrooms");
    private CollectionReference chatusers=db.collection("chatusers");
    RecentChatProfileAdapter recentChatProfileAdapter;
    ArrayList<ChatroomModel>chatroomModels;
    ImageView backbtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_main);

//        Window window = this.getWindow();
//        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        profileimage = findViewById(R.id.profileimage);
        searchimage = findViewById(R.id.circleImageView3);
        recyclerView = findViewById(R.id.recyclerView);
        backbtn = findViewById(R.id.imageView3);


        backbtn.setOnClickListener(v -> {
            finish();
        });

        //when user click on search then intent to Searchuseractivity
        searchimage.setOnClickListener(v -> {
            startActivity(new Intent(this, SearchUserActivity.class));
        });


        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(ChatMainActivity.this, LinearLayoutManager.VERTICAL, false));

        chatroomModels=new ArrayList<>();
        recentChatProfileAdapter=new RecentChatProfileAdapter(chatroomModels,this,this);
        recyclerView.setAdapter(recentChatProfileAdapter);



        getAllChats(getActiveUser());
        getUserProfile(getActiveUser());

    }

    /**
     * GetUser Profile and set to Circular Imageview
     * @param activeUser
     */
    private void getUserProfile(String activeUser) {
        chatusers.document(activeUser).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                UserModel userModel = task.getResult().toObject(UserModel.class);

                // Do something with the userModel, for example, display the user's name
                if (userModel != null) {
                    String photoUrl = userModel.getPhotoUrl();
                    Glide.with(ChatMainActivity.this).load(photoUrl).into(profileimage);
                }
            } else {
                Log.e("UserProfile", "Error getting user profile", task.getException());
            }
        });
    }


    private void getAllChats(String activeUser) {
        chatroomModels.clear();
        allcollection.whereArrayContains("userIds",activeUser)
                .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {

                            Log.e("Firestore error", error.getMessage());
                            return;
                        }
                        if (chatroomModels!=null)
                        {
                            chatroomModels.clear();
                        }

                        // Check if there are any new documents
                        if (value != null && !value.isEmpty()) {
                            for (DocumentSnapshot document : value.getDocuments()) {
                                ChatroomModel chatroomModel = document.toObject(ChatroomModel.class);
                                if (chatroomModel != null) {
                                    // Add the message to the list
                                    chatroomModels.add(chatroomModel);
                                }
                            }

                            // Notify the adapter that the data has changed
                            recentChatProfileAdapter.notifyDataSetChanged();
                        }
                    }
                });
    }


    /**
     * Return the Active user
     *
     * @return
     */
    public String getActiveUser() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        SharedPreferences preferences = getSharedPreferences("DoctorPrefs", MODE_PRIVATE);
        if (currentUser != null) {
            return currentUser.getUid();
        } else if (preferences != null) {
            // Retrieve doctor data from SharedPreferences
            String userid = preferences.getString("doctorId", "");
            if (!userid.isEmpty()) {
                return userid;
            }

        }

        return null;
    }

    /**
     * Here we get the UserModel when click pass as intent to messageactivity
     * @param position
     * @param model
     */
    @Override
    public void onRecentUserClick(int position,UserModel model) {
        Intent intent=new Intent(ChatMainActivity.this,MessageActivity.class);
        intent.putExtra("userid",model.getUserId());
        intent.putExtra("username",model.getUsername());
        intent.putExtra("fcmtoken",model.getToken());
        intent.putExtra("email",model.getEmail());
        intent.putExtra("profileImage",model.getPhotoUrl());
        intent.putExtra("accountType",model.getAccountType());

        startActivity(intent);

    }
}