package com.vrnitsolution.healthapp;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.vrnitsolution.healthapp.Message.model.UserModel;
import com.vrnitsolution.healthapp.model.Coordinates;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserRegistration extends AppCompatActivity implements LocationListener {
    EditText userfullname, useremail, usermobileno, userpassword;
    String name, email, mobile, password;
    ImageView togglebutton;
    private boolean toggle = false;
    String token = "";
    FirebaseAuth firebaseAuth;
    Coordinates coordinates;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private LocationManager locationManager;
    ProgressDialog progressDialog;

    private static final String LOCATION_PERMISSION = android.Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String LOCATION_COARSE = android.Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int PERMISSION_REQ_CODE = 1005;
    private CollectionReference chatCollection = db.collection("chatusers");
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int MAX_IMAGE_SIZE = 20 * 1024; // 20KB in bytes

    private Uri filePath;
    private CircleImageView previewImage;
    private AppCompatButton uploadButton;
    private ProgressDialog uploadProgressDialog;
    private AlertDialog imageSelectionDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_registration);

        Window window = this.getWindow();
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Initialize firebase auth
        firebaseAuth = FirebaseAuth.getInstance();


        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading..."); // Set your message here
        progressDialog.setCancelable(false); // Set whether the dialog can be canceled by pressing back button


        previewImage = new CircleImageView(this);
        uploadButton = new AppCompatButton(this);
        uploadProgressDialog = new ProgressDialog(this);
        uploadProgressDialog.setMessage("Uploading Image...");
        uploadProgressDialog.setCancelable(false);

        retriveLocation();
        getFcmToken();


        userfullname = findViewById(R.id.doctorName);
        useremail = findViewById(R.id.doctorEmail);
        usermobileno = findViewById(R.id.doctorMobile);
        userpassword = findViewById(R.id.doctorPassword);
        togglebutton = findViewById(R.id.togglepassword);


        togglebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (toggle == false) {
                    toggle = true;
                    togglebutton.setImageResource(R.drawable.baseline_visibility_on);
                    userpassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                } else {
                    toggle = false;
                    togglebutton.setImageResource(R.drawable.baseline_visibility_off);
                    userpassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });
    }


    /**
     * Get Device token for the Message
     */
    private void getFcmToken() {
        // Get FCM token
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                        return;
                    }

                    // Get the FCM token
                    token = task.getResult();
                    Log.d(TAG, "FCM token: " + token);

                    // Now you can save this token to your Firestore database or handle it as needed
                });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQ_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted.", Toast.LENGTH_SHORT).show();
            } else if ((!ActivityCompat.shouldShowRequestPermissionRationale(UserRegistration.this, LOCATION_PERMISSION) && !(ActivityCompat.shouldShowRequestPermissionRationale(UserRegistration.this, LOCATION_COARSE)))) {
                AlertDialog.Builder builder = new AlertDialog.Builder(UserRegistration.this);
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
     * When this method calls then get the coordinates
     */
    public void retriveLocation() {
        if (ActivityCompat.checkSelfPermission(this, LOCATION_PERMISSION) == PackageManager.PERMISSION_GRANTED) {        // Initialize location manager and request location updates
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5, this);

            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            if (location != null) {
                String lat = String.valueOf(location.getLatitude());
                String longit = String.valueOf(location.getLongitude());

                coordinates = new Coordinates(lat, longit);

                if (coordinates != null) {

                } else {
                    displayToast("Coordinates not getting.please make sure connected to the internet and location on");

                }
            }
        } else {
            requestRuntimePermission();
        }
    }


    /**
     * Get the Coordinates of your current location when user Allow the Permission of Location
     */
    @Override
    public void onLocationChanged(@NonNull Location location) {

    }

    /**
     * Checking the Location permission and requesting location permission.
     */
    private void requestRuntimePermission() {
        if ((ActivityCompat.checkSelfPermission(UserRegistration.this, LOCATION_PERMISSION) == PackageManager.PERMISSION_GRANTED) && (ActivityCompat.checkSelfPermission(UserRegistration.this, LOCATION_COARSE) == PackageManager.PERMISSION_GRANTED)) {

        } else if (ActivityCompat.shouldShowRequestPermissionRationale(UserRegistration.this, LOCATION_PERMISSION) && ActivityCompat.shouldShowRequestPermissionRationale(UserRegistration.this, LOCATION_COARSE)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("This app requires LOCATION PERMISSION  for feature to work as expected.")
                    .setTitle("Permission Required")
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(UserRegistration.this, new String[]{LOCATION_PERMISSION, LOCATION_COARSE}, PERMISSION_REQ_CODE);
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
    protected void onStart() {
        super.onStart();
        requestRuntimePermission();

    }

    private void displayToast(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }

    public void registerUser(View view) {
        name = userfullname.getText().toString().trim();
        email = useremail.getText().toString().trim();
        mobile = usermobileno.getText().toString().trim();
        password = userpassword.getText().toString().trim();


        if (name.isEmpty() || email.isEmpty() || mobile.isEmpty() || password.isEmpty()) {
            displayToast("Enter all data correctly");
        } else if (!(password.length() >= 6)) {
            displayToast("Enter atleast 6 charachter password");
        } else if (coordinates == null) {
            displayToast("Please make sure you're connected to internet and on location");

        } else {
            showImageSelectionDialog();
        }
    }


//    private void showImageSelectionDialog() {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        LayoutInflater inflater = getLayoutInflater();
//        View dialogView = inflater.inflate(R.layout.dilog_choose_image, null);
//        builder.setTitle("Choose Profile Image");
//        builder.setView(dialogView);
//
//        previewImage = dialogView.findViewById(R.id.previewImage);
//        uploadButton = dialogView.findViewById(R.id.uploadButton);
//
//        // Image selection button
//        CircleImageView chooseImage = dialogView.findViewById(R.id.chooseImage);
//        chooseImage.setOnClickListener(v -> openFileChooser());
//
//        // Upload button
//        uploadButton.setOnClickListener(v -> {
//            uploadImage();
//            // Dismiss the dialog after upload
//            builder.create().dismiss();
//        });
//
//        builder.setPositiveButton("Cancel", (dialog, which) -> dialog.dismiss());
//
//        // Set onDismissListener to reset the dialog content when dismissed
//        builder.setOnDismissListener(dialog -> resetDialog());
//
//        builder.show();
//    }

    private void showImageSelectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dilog_choose_image, null);
        builder.setTitle("Choose Profile Image");
        builder.setView(dialogView);

        previewImage = dialogView.findViewById(R.id.previewImage);
        uploadButton = dialogView.findViewById(R.id.uploadButton);

        // Image selection button
        CircleImageView chooseImage = dialogView.findViewById(R.id.chooseImage);
        chooseImage.setOnClickListener(v -> openFileChooser());

        // Upload button
        uploadButton.setOnClickListener(v -> {
            uploadImage();
        });

        builder.setPositiveButton("Cancel", (dialog, which) -> dialog.dismiss());

        // Set onDismissListener to reset the dialog content when dismissed
        builder.setOnDismissListener(dialog -> resetDialog());

        // Assign the created AlertDialog to the global variable
        imageSelectionDialog = builder.create();
        imageSelectionDialog.show();
    }

    private void uploadImage() {
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
                                    sendToRegisterDetails(name, email, mobile, password, coordinates, imageUrl);

                                    // Dismiss the image selection dialog here
                                    if (imageSelectionDialog != null && imageSelectionDialog.isShowing()) {
                                        imageSelectionDialog.dismiss();
                                    }
                                }
                            });
                        } else {
                            Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
    }

    private void resetDialog() {
        // Reset the dialog content when dismissed
        filePath = null;
        previewImage.setImageResource(0);
        uploadButton.setVisibility(View.GONE);
    }

//    private void uploadImage() {
//        if (filePath != null) {
//            uploadProgressDialog.show();
//
//            // Get a reference to the storage location
//            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("userProfile/" + System.currentTimeMillis() + ".jpg");
//
//            // Upload the file
//            storageReference.putFile(filePath)
//                    .addOnCompleteListener(task -> {
//                        uploadProgressDialog.dismiss();
//                        if (task.isSuccessful()) {
//                            // Image uploaded successfully, get download URL
//                            storageReference.getDownloadUrl().addOnCompleteListener(downloadTask -> {
//                                if (downloadTask.isSuccessful()) {
//                                    String imageUrl = downloadTask.getResult().toString();
//                                    sendToRegisterDetails(name, email, mobile, password, coordinates, imageUrl);
//                                }
//                            });
//                        } else {
//                            Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show();
//                        }
//                    });
//        }
//    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();

            try {
                // Check image size before processing
                if (getImageSize(filePath) > MAX_IMAGE_SIZE) {
                    displayToast("Image size exceeds the limit of 20KB");
                    uploadButton.setVisibility(View.GONE);
                    return;
                }


                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                uploadButton.setVisibility(View.VISIBLE);
                previewImage.setImageBitmap(bitmap);

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

//    private void sendToRegisterDetails(String name, String email, String mobile, String password, Coordinates coordinates, String imageUrl) {
//        //send data to Doctor Form Details Page
//        HashMap<String, Object> userdata = new HashMap<>();
//        userdata.put("username", name);
//        userdata.put("password", password);
//        userdata.put("email", email);
//        userdata.put("phoneNo", mobile);
//        userdata.put("currentCoordinates", coordinates);
//        userdata.put("messageToken", token);
//        userdata.put("profileUrl", imageUrl);
//
//
//        firebaseAuth.createUserWithEmailAndPassword(email, password)
//                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
//                    @Override
//                    public void onComplete(@NonNull Task<AuthResult> task) {
//                        if (task.isSuccessful()) {
//                            // Sign in success, update UI with the signed-in user's information
//                            Log.d(TAG, "createUserWithEmail:success");
//                            FirebaseUser user = firebaseAuth.getCurrentUser();
//                           displayToast(user.toString());
//
//                        } else {
//                            // If sign in fails, display a message to the user.
//                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
//                            displayToast("Authentication failed."+task.getException().toString());
//                        }
//                    }
//                });
//
//    }


    private void sendToRegisterDetails(String name, String email, String mobile, String password, Coordinates coordinates, String imageUrl) {
        //send data to Doctor Form Details Page
        HashMap<String, Object> userdata = new HashMap<>();
        userdata.put("username", name);
        userdata.put("email", email);
        userdata.put("phoneNo", mobile);
        userdata.put("currentCoordinates", coordinates);
        userdata.put("messageToken", token);
        userdata.put("profileUrl", imageUrl);


        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "createUserWithEmail:success");
                        FirebaseUser user = firebaseAuth.getCurrentUser();

                        //add uid
                        userdata.put("uid",user.getUid());

                        // Set display name
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(name)
                                .setPhotoUri(Uri.parse(imageUrl))  // Assuming imageUrl is a valid Uri
                                .build();

                        user.updateProfile(profileUpdates)
                                .addOnCompleteListener(updateTask -> {
                                    if (updateTask.isSuccessful()) {
                                        // Display name and profile picture updated successfully
                                        displayToast("User registration successful!");
                                        createNewUserIntheFireStore(userdata);
                                    } else {
                                        // Failed to update display name or profile picture
                                        displayToast("Failed to update user profile information.");
                                    }
                                });
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                        displayToast("Authentication failed." + task.getException().toString());
                    }
                });
    }

    private void createNewUserIntheFireStore(HashMap<String, Object> userdata) {
        db.collection("users").add(userdata).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                if (task.isSuccessful()) {
                    displayToast("New User Created");

                    FirebaseUser user = firebaseAuth.getCurrentUser();

                    if (user != null) {
                       registerToChat(userdata,user.getUid());
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                displayToast("" + e.getMessage().toString());
            }
        });
    }

    /**
     * For saving the data in chatCollection we passed the data in UserModel object that set in collection
     *
     * @param user
     * @param uid
     */
    private void registerToChat(Map<String, Object> user, String uid) {
        String userid = (String) user.get("uid");
        String username = (String) user.get("username");
        String email = (String) user.get("email");
        String messageToken = (String) user.get("messageToken");
        String profileUrl = (String) user.get("profileUrl");


        UserModel userModel = new UserModel(userid, profileUrl, username, "user", email, messageToken);


        chatCollection.document(uid).set(userModel).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    progressDialog.dismiss();
                    // When task is successful redirect to profile activity display Toast
                    displayToast("Activated chat feature");
                    finish();
                    startActivity(new Intent(UserRegistration.this, DashboardHome.class));
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                displayToast("Failed to activating chat feature");
            }
        });
    }


    public void loginUser(View view) {
        startActivity(new Intent(UserRegistration.this,LoginActivity.class));
    }
}