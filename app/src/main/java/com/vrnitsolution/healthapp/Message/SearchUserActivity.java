package com.vrnitsolution.healthapp.Message;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.auth.User;
import com.vrnitsolution.healthapp.Message.adapter.SearchUserRecyclerAdapter;
import com.vrnitsolution.healthapp.Message.model.UserModel;
import com.vrnitsolution.healthapp.R;
import com.vrnitsolution.healthapp.bookappointment.model.Patient;

import java.util.ArrayList;

public class SearchUserActivity extends AppCompatActivity implements SearchUserRecyclerAdapter.OnSearchUserClickListner {
    ImageView backbtn;
    EditText searchuser;
    ImageView searchBtn;
    ArrayList<UserModel> userModels;
    RecyclerView searchRecyclerview;
    SearchUserRecyclerAdapter searchUserRecyclerAdapter;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    // Create a reference to the "doctors" collection
    private CollectionReference chatcollection = db.collection("chatusers");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_user);

//        Window window = this.getWindow();
//        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        backbtn = findViewById(R.id.imageView3);
        searchuser = findViewById(R.id.searchuser);
        searchBtn = findViewById(R.id.searchBtn);
        searchRecyclerview = findViewById(R.id.searchRecyclerview);
        searchRecyclerview.setHasFixedSize(true);
        searchRecyclerview.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        searchuser.requestFocus();

        userModels = new ArrayList<>();
        searchUserRecyclerAdapter = new SearchUserRecyclerAdapter(userModels, this, this);
        searchRecyclerview.setAdapter(searchUserRecyclerAdapter);


        backbtn.setOnClickListener(v -> {
            finish();
        });


        searchBtn.setOnClickListener(v -> {
            String username = searchuser.getText().toString().trim();
            if (!username.isEmpty()) {
                searchUser(username);
            } else {
                displayToast("enter username");
            }
        });
    }

    /**
     * When enter username then is checks that username present in that collection
     *
     * @param username
     */
    private void searchUser(String username) {
        userModels.clear();
        chatcollection.whereEqualTo("username", username).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.e("Firestore error", error.getMessage());
                    return;
                }

                if (userModels!=null)
                {
                    userModels.clear();
                }

                for (DocumentSnapshot document : value.getDocuments()) {
                    UserModel userProfile = document.toObject(UserModel.class);
                    if (userProfile != null) {
                        userModels.add(userProfile);
                    }
                }

                // Notify the adapter that the data has changed
                searchUserRecyclerAdapter.notifyDataSetChanged();
            }
        });
    }


    /**
     * When perform click then send that userModel data to thet in intent
     * @param position
     */
    @Override
    public void onUserClickListner(int position) {
        UserModel userModel = userModels.get(position);
        Intent intent=new Intent(SearchUserActivity.this,MessageActivity.class);
        intent.putExtra("userid",userModel.getUserId());
        intent.putExtra("username",userModel.getUsername());
        intent.putExtra("fcmtoken",userModel.getToken());
        intent.putExtra("email",userModel.getEmail());
        intent.putExtra("profileImage",userModel.getPhotoUrl());
        intent.putExtra("accountType",userModel.getAccountType());
        startActivity(intent);
        finish();
    }

    public void displayToast(String txt) {
        Toast.makeText(this, "" + txt, Toast.LENGTH_SHORT).show();
    }
}