package com.vrnitsolution.healthapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.vrnitsolution.healthapp.model.Coordinates;

import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;

public class UpdateUserProfile extends AppCompatActivity {
    CircleImageView backBtnImageview;
    EditText username, usermobile;
    CircleImageView profilepic;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ProgressDialog uploadProgressDialog;
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int MAX_IMAGE_SIZE = 20 * 1024; // 20KB in bytes
    private Uri filePath;
    AppCompatButton upldateprofile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_user_profile);

        backBtnImageview = findViewById(R.id.backBtnImageview);

        backBtnImageview.setOnClickListener(view -> {
            finish();
        });


        username = findViewById(R.id.doctorName);
        usermobile = findViewById(R.id.doctorMobile);
        profilepic = findViewById(R.id.circleImageView6);
        upldateprofile = findViewById(R.id.upldateprofile);


        // Initialize firebase auth
        firebaseAuth = FirebaseAuth.getInstance();

        //get current user data
        FirebaseUser user=firebaseAuth.getCurrentUser();

        if (user!=null)
        {
            getCurrentUserData(user);
        }


        uploadProgressDialog = new ProgressDialog(this);
        uploadProgressDialog.setMessage("Uploading Image...");
        uploadProgressDialog.setCancelable(false);


        profilepic.setOnClickListener(v -> openFileChooser());

    }

    private void getCurrentUserData(FirebaseUser user) {
        db.collection("users").whereEqualTo("uid",user.getUid()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful())
                {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        // Set the data in EditText fields
                        username.setText( document.getString("username"));
                        usermobile.setText(document.getString("phoneNo"));

                        Glide.with(UpdateUserProfile.this).load(document.getString("profileUrl")).into(profilepic);
                    }

                }
            }
        });
    }


    /**
     * This method only update the username and mobile no
     *
     * @param view
     */
    public void updateProfile(View view) {
        String fullname = username.getText().toString().trim();
        String mobileno = usermobile.getText().toString().trim();
        FirebaseUser user = firebaseAuth.getCurrentUser();


        if (fullname.isEmpty() || mobileno.isEmpty()) {
            displayToast("Data can't empty");
        } else {
            //update the username and phoneNo
            if (user != null) {

                // Update profile picture in Firebase Authentication
                user.updateProfile(new UserProfileChangeRequest.Builder()
                        .setDisplayName(fullname)
                        .build());


                // Update profile picture in Firestore user data
                CollectionReference userRef = db.collection("users");

                // Query the user documents with the matching UID
                userRef.whereEqualTo("uid", user.getUid())
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            // Assuming there is only one document with the given UID
                            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                userRef.document(document.getId())
                                        .update("username", fullname, "phoneNo", mobileno)
                                        .addOnSuccessListener(aVoid -> {
                                            upldateprofile.setVisibility(View.GONE);
                                            displayToast("Profile Updated Successfully");
                                        })
                                        .addOnFailureListener(e -> {
                                            displayToast("Failed to update profile pic");
                                        });
                            }
                        })
                        .addOnFailureListener(e -> {
                            displayToast("Failed to retrieve user data");
                        });
            }
        }
    }

    /**
     * filechooser intent
     */
    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();

            try {
                // Check image size before processing
                if (getImageSize(filePath) > MAX_IMAGE_SIZE) {
                    displayToast("Image size exceeds the limit of 20KB");
                    upldateprofile.setVisibility(View.GONE);
                    return;
                }


                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                upldateprofile.setVisibility(View.VISIBLE);
                profilepic.setImageBitmap(bitmap);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private long getImageSize(Uri uri) {
        // Get the size of the image in bytes
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
            long size = cursor.getLong(sizeIndex);
            cursor.close();
            return size;
        }
        return 0;
    }


    /**
     * This method only update the profilepic
     *
     * @param view
     */
    public void updateProfilePic(View view) {
        if (filePath != null) {
            uploadProgressDialog.show();

            // Get a reference to the storage location
            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("userProfile/" + System.currentTimeMillis() + ".jpg");

            // Upload the file
            storageReference.putFile(filePath)
                    .addOnCompleteListener(task -> {
                        uploadProgressDialog.dismiss(); // Dismiss the upload dialog

                        if (task.isSuccessful()) {
                            // Image uploaded successfully, get download URL
                            storageReference.getDownloadUrl().addOnCompleteListener(downloadTask -> {
                                if (downloadTask.isSuccessful()) {
                                    String imageUrl = downloadTask.getResult().toString();
                                    FirebaseUser user = firebaseAuth.getCurrentUser();

                                    updateProfilePicAuth(imageUrl, user);

                                }
                            });
                        } else {
                            Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    /**
     * Update the profile pic from both firestore user and auth
     *
     * @param imageUrl
     * @param user
     */
    private void updateProfilePicAuth(String imageUrl, FirebaseUser user) {
        // Update profile picture in Firebase Authentication
        user.updateProfile(new UserProfileChangeRequest.Builder()
                .setPhotoUri(Uri.parse(imageUrl))
                .build());

        // Update profile picture in Firestore user data
        CollectionReference userRef = db.collection("users");

        // Query the user documents with the matching UID
        userRef.whereEqualTo("uid", user.getUid())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Assuming there is only one document with the given UID
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        // Update the profilePicUrl field for the matched document
                        userRef.document(document.getId())
                                .update("profileUrl", imageUrl)
                                .addOnSuccessListener(aVoid -> {
                                    upldateprofile.setVisibility(View.GONE);
                                    displayToast("Profile Updated Successfully");
                                })
                                .addOnFailureListener(e -> {
                                    displayToast("Failed to update profile pic");
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    displayToast("Failed to retrieve user data");
                });
    }


    public void displayToast(String txt) {
        Toast.makeText(this, "" + txt, Toast.LENGTH_SHORT).show();
    }
}