package com.vrnitsolution.healthapp.DoctorUI;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.icu.util.Calendar;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.vrnitsolution.healthapp.LoginActivity;
import com.vrnitsolution.healthapp.Message.model.UserModel;
import com.vrnitsolution.healthapp.R;
import com.vrnitsolution.healthapp.model.Coordinates;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class DoctorFormPage extends AppCompatActivity implements LocationListener {
    String doctorName = "", email = "", mobileNo = "", doctorId = "", messageToken = "", password = "";

    AppCompatButton addScheduleButton, uploadImageBtn;
    private Spinner spinner;
    private EditText editText, occupation, specialistIn;
    private ImageView addButton;
    private ArrayList<String> values = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    private LocationManager locationManager;

    // List of medical test items

    private static final int PICK_IMAGE_REQUEST = 1;
    private CircleImageView profileImage;
    private Uri selectedImageUri;
    private String profileImageLink = "";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    // Create a reference to the "doctors" collection
    private CollectionReference doctorsCollection = db.collection("doctors");
    private CollectionReference chatCollection = db.collection("chatusers");
    Coordinates coordinates;

    private static final String LOCATION_PERMISSION = android.Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String LOCATION_COARSE = android.Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int PERMISSION_REQ_CODE = 1005;
    ProgressDialog progressDialog;


    private String[] spinnerItems = {
            "Blood test",
            "Blood count",
            "Urinalysis",
            "Medical test",
            "Electrocardiography",
            "Hemoglobin A1C",
            "Ultrasonography",
            "Liver function tests",
            "Thyroid function tests",
            "Hemoglobin",
            "Basic metabolic panel",
            "ESR",
            "Pap smear",
            "CT scan",
            "Cardiac stress test",
            "Amniocentesis",
            "Biopsy",
            "Blood sugar test",
            "Electroencephalography",
            "Prothrombin time",
            "Magnetic resonance imaging",
            "Genetic testing",
            "Chorionic villus sampling",
            "Echocardiography",
            "X ray",
            "HIV",
            "Cancer",
            "TB",
            "Other Services",
            "Fitness Checkup",
            "ICU",
            "OPD",
    };
    private HashMap<String, String> servicehours = new HashMap<>(); // Initialize the servicehours HashMap
    // Define a Firebase Storage reference
    private StorageReference storageRef = FirebaseStorage.getInstance().getReference();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_form_page);


//        Window window = this.getWindow();
//        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        addScheduleButton = findViewById(R.id.addScheduleButton);
        uploadImageBtn = findViewById(R.id.uploadImageBtn);
        specialistIn = findViewById(R.id.specialist);
        occupation = findViewById(R.id.specialist);

        //Initialize progress dialog
        progressDialog=new ProgressDialog(DoctorFormPage.this);
        progressDialog.setMessage("Wait while registering account..");
        progressDialog.setCancelable(false);



        /** Check if data is received successfully */
        //get data from RegisterActivity
        HashMap<String, Object> doctorData = (HashMap<String, Object>) getIntent().getSerializableExtra("doctordata");

        /** Check if data is received successfully */
        if (doctorData != null) {
            // Extract data from the HashMap
            doctorName = (String) doctorData.get("doctorName");
            email = (String) doctorData.get("email");
            mobileNo = (String) doctorData.get("mobileNo");
            doctorId = (String) doctorData.get("doctorId");
            messageToken = (String) doctorData.get("messageToken");
            password = (String) doctorData.get("password");


        } else {
            // Handle the case when data is not received
            Toast.makeText(this, "Failed to receive data contact with admin", Toast.LENGTH_SHORT).show();
        }

        addScheduleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCustomScheduleDialog();
            }
        });

        /** when click on UploadImageBtn then upload the URI into Firebase Storage Bucket
         * It Visible when it get selectedURI
         * */

        uploadImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedImageUri != null) {
                    uploadImageToFirebaseStorage(selectedImageUri);
                } else {
                    Toast.makeText(DoctorFormPage.this, "Image Not selected please select profile image", Toast.LENGTH_SHORT).show();
                }
            }
        });


        spinner = findViewById(R.id.spinner);
        editText = findViewById(R.id.editText);
        addButton = findViewById(R.id.addButton);
        profileImage = findViewById(R.id.profileImage);

        // Initialize the spinner with the list of medical test items
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, spinnerItems);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedValue = spinner.getSelectedItem().toString();


                if (!values.contains(selectedValue)) {
                    values.add(selectedValue);
                    adapter.notifyDataSetChanged();
                    editText.setText("");
                    editText.append(values.toString());
                } else if (values.contains(selectedValue)) {
                    Toast.makeText(getApplicationContext(), "Value already exists!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Please enter a value.", Toast.LENGTH_SHORT).show();
                }

            }
        });

        //select image from device
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openImagePicker();
            }
        });


    }


    /**
     * Checking the Location permission and requesting location permission.
     */
    private void requestRuntimePermission() {
        if ((ActivityCompat.checkSelfPermission(DoctorFormPage.this, LOCATION_PERMISSION) == PackageManager.PERMISSION_GRANTED) && (ActivityCompat.checkSelfPermission(DoctorFormPage.this, LOCATION_COARSE) == PackageManager.PERMISSION_GRANTED)) {
            Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(DoctorFormPage.this, LOCATION_PERMISSION) && ActivityCompat.shouldShowRequestPermissionRationale(DoctorFormPage.this, LOCATION_COARSE)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("This app requires LOCATION PERMISSION  for feature to work as expected.")
                    .setTitle("Permission Required")
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(DoctorFormPage.this, new String[]{LOCATION_PERMISSION, LOCATION_COARSE}, PERMISSION_REQ_CODE);
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQ_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted.", Toast.LENGTH_SHORT).show();
            } else if ((!ActivityCompat.shouldShowRequestPermissionRationale(DoctorFormPage.this, LOCATION_PERMISSION) && !(ActivityCompat.shouldShowRequestPermissionRationale(DoctorFormPage.this, LOCATION_COARSE)))) {
                AlertDialog.Builder builder = new AlertDialog.Builder(DoctorFormPage.this);
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

    //Shcedule Custom Dialog
    private void showCustomScheduleDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);

        View dialogView = LayoutInflater.from(this).inflate(R.layout.time_picker_dailog, null);
        builder.setView(dialogView);

        // Dynamically create and add checkboxes for each day of the week
        String[] daysOfWeek = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        for (String day : daysOfWeek) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(day);
            ((LinearLayout) dialogView).addView(checkBox);
        }

        EditText openingTimeEditText = dialogView.findViewById(R.id.openingTimeEditText);
        EditText closingTimeEditText = dialogView.findViewById(R.id.closingTimeEditText);

        openingTimeEditText.setOnClickListener(v -> showTimePickerDialog(openingTimeEditText));
        closingTimeEditText.setOnClickListener(v -> showTimePickerDialog(closingTimeEditText));


        builder.setPositiveButton("Add", (dialog, which) -> {
            for (int i = 0; i < ((LinearLayout) dialogView).getChildCount(); i++) {
                View view = ((LinearLayout) dialogView).getChildAt(i);
                if (view instanceof CheckBox) {
                    CheckBox checkBox = (CheckBox) view;
                    String day = checkBox.getText().toString();
                    String timeRange = checkBox.isChecked()
                            ? openingTimeEditText.getText().toString() + " - " + closingTimeEditText.getText().toString()
                            : "Closed";
                    servicehours.put(day, timeRange);
                }
            }

            if (!servicehours.isEmpty()) {
                // Display the servicehours object as a Toast
                Toast.makeText(getApplicationContext(), "Service Hours  Added", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Schedule Not Added please ! add again", Toast.LENGTH_SHORT).show();
            }
            // Close the dialog
            dialog.dismiss();
        });


        builder.show();
    }

    /**
     * This function Selecting time form time picker is an Timepicker dialog
     */
    private void showTimePickerDialog(final EditText timeEditText) {
        final Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int existingMinute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, hourOfDay, minute) -> {
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minute);
                    SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                    timeEditText.setText(sdf.format(calendar.getTime()));
                }, hour, existingMinute, false);

        timePickerDialog.show();
    }


    /**
     * Image Choose and Uploading function Image Picker intent to get image URI object
     */
    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();

            // Check image size
            if (getImageSizeInKB(selectedImageUri) <= 20) {
                Glide.with(getApplicationContext()).load(selectedImageUri).into(profileImage);
                // Visible Upload Button
                uploadImageBtn.setVisibility(View.VISIBLE);
            } else {
                Toast.makeText(this, "Image size must be less than 20KB", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * This function checks the file size of the image and returns the size in KB.
     */
    private int getImageSizeInKB(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            if (inputStream != null) {
                int size = inputStream.available() / 1024; // Convert to KB
                inputStream.close();
                return size;
            } else {
                return -1; // Error occurred
            }
        } catch (IOException e) {
            e.printStackTrace();
            return -1; // Error occurred
        }
    }



    /**
     * This function calls then its save the image in firebase storage doctorProfile folder and gettting downloadUrl form that
     */
    private void uploadImageToFirebaseStorage(Uri imageUri) {
        // Generate a unique name for the image in Firebase Storage
        String imageName = "profile_image_" + System.currentTimeMillis() + ".jpg";

        StorageReference imageRef = storageRef.child("doctorProfile/" + imageName);

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading Image");
        progressDialog.setMessage("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        UploadTask uploadTask = imageRef.putFile(imageUri);

        uploadTask.addOnSuccessListener(taskSnapshot -> {
            // Image upload was successful
            // Get the full download URL of the uploaded image
            imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                progressDialog.dismiss(); // Dismiss the progress dialog
                uploadImageBtn.setVisibility(View.GONE);
                String imageUrl = uri.toString();
                //here you get the Image URL
                profileImageLink = imageUrl;
                Toast.makeText(this, "Image uploaded successfully", Toast.LENGTH_SHORT).show();
            }).addOnFailureListener(e -> {
                progressDialog.dismiss(); // Dismiss the progress dialog
                // Handle any errors while getting the download URL
                Toast.makeText(this, "Error getting download URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }).addOnFailureListener(e -> {
            progressDialog.dismiss(); // Dismiss the progress dialog
            // Handle any errors during image upload
            Toast.makeText(this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });

        // Set up a progress listener to update the dialog's progress
        uploadTask.addOnProgressListener(taskSnapshot -> {
            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
            progressDialog.setMessage("Uploading: " + (int) progress + "%");
        });
    }


    /**
     * when onClick check filled's cannot be empty or null then proceed to data saving
     */
    public void uploadData(View view) {
        final String doctorOccupation = occupation.getText().toString().trim();
        final String doctorSpecialistIn = specialistIn.getText().toString().trim();


        if (doctorOccupation.isEmpty()) {
            Toast.makeText(this, "Doctor Occupation is empty", Toast.LENGTH_SHORT).show();
        } else if (doctorSpecialistIn.isEmpty()) {
            Toast.makeText(this, "Doctor Specialist In data is empty", Toast.LENGTH_SHORT).show();
        } else if (servicehours.isEmpty()) {
            Toast.makeText(this, "Schedule data is empty", Toast.LENGTH_SHORT).show();
        } else if (values.isEmpty()) {
            Toast.makeText(this, "Services not added", Toast.LENGTH_SHORT).show();
        } else if (profileImageLink.isEmpty()) {
            Toast.makeText(this, "Image Not Uploaded", Toast.LENGTH_LONG).show();
        } else if (coordinates == null) {
            Toast.makeText(this, "Your location coordinates not getting", Toast.LENGTH_SHORT).show();
            retriveLocation();
        } else {
            //here user actual register the account

            HashMap<String, Object> doctordata = new HashMap<>();
            doctordata.put("availableservices", values);
            doctordata.put("doctorName", doctorName);
            doctordata.put("doctorId", doctorId);
            doctordata.put("email", email);
            doctordata.put("mobileNo", mobileNo);
            doctordata.put("messageToken", messageToken);
            doctordata.put("occupation", doctorOccupation);
            doctordata.put("servicehours", servicehours);
            doctordata.put("specialistIn", doctorSpecialistIn);
            doctordata.put("photoUrl", profileImageLink);
            doctordata.put("coordinates", coordinates);
            doctordata.put("accountType", "doctor");
            doctordata.put("account_status", "Not Approved");
            doctordata.put("password", password);



            if (coordinates.getLongitude().isEmpty() && coordinates.getLongitude().isEmpty())
            {
                Toast.makeText(this, "On Location to get Location coordinates", Toast.LENGTH_SHORT).show();
            }else {
                progressDialog.show();
                doctorRegisterToFirebase(doctordata, email, doctorId);
            }

        }
    }


    /**
     * Saving that data into firestore collection (doctors) means doctor registered account
     */

    private void doctorRegisterToFirebase(HashMap<String, Object> doctordata, String email, String doctorId) {
        // Check if the email or doctorId already exists
        Query emailQuery = doctorsCollection.whereEqualTo("email", email);
        Query doctorIdQuery = doctorsCollection.whereEqualTo("doctorId", doctorId);


        // Perform the email check
        Task<QuerySnapshot> emailTask = emailQuery.get();
        emailTask
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Email already exists
                        progressDialog.dismiss();
                        Toast.makeText(this, "Email already exists.", Toast.LENGTH_SHORT).show();
                    } else {
                        // Email is unique, proceed with the doctorId check
                        Task<QuerySnapshot> doctorIdTask = doctorIdQuery.get();
                        doctorIdTask
                                .addOnSuccessListener(queryDocumentSnapshots1 -> {
                                    if (!queryDocumentSnapshots1.isEmpty()) {
                                        // Doctor ID already exists
                                        progressDialog.dismiss();
                                        Toast.makeText(this, "Doctor ID already exists.", Toast.LENGTH_SHORT).show();
                                    } else {
                                        // If both email and doctorId are unique, proceed with data upload
                                        doctorsCollection.add(doctordata)
                                                .addOnSuccessListener(documentReference -> {
                                                    progressDialog.dismiss();
                                                    String documentId = documentReference.getId();
                                                    Toast.makeText(this, "Account created successfully.", Toast.LENGTH_SHORT).show();

                                                    // Accessing fields from doctordata HashMap
                                                    String doctorId1 = (String) doctordata.get("doctorId");
                                                    String doctorName = (String) doctordata.get("doctorName");
                                                    String email1 = (String) doctordata.get("email");
                                                    String messageToken = (String) doctordata.get("messageToken");
                                                    String profileImageLink = (String) doctordata.get("photoUrl");

                                                    UserModel userModel = new UserModel();
                                                    userModel.setUserId(doctorId1);
                                                    userModel.setUsername(doctorName);
                                                    userModel.setPhotoUrl(profileImageLink);
                                                    userModel.setAccountType("doctor");
                                                    userModel.setEmail(email1);
                                                    userModel.setToken(messageToken);

                                                    registerToChat(userModel, doctorId1);


                                                })
                                                .addOnFailureListener(e -> {
                                                    progressDialog.dismiss();
                                                    Toast.makeText(this, "Error uploading doctor data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                });
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    // Handle any errors during the doctorId check
                                    progressDialog.dismiss();
                                    Toast.makeText(this, "Error checking doctorId availability: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle any errors during the email check
                    progressDialog.dismiss();
                    Toast.makeText(this, "Error checking email availability: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    /**
     * Add that userModel object to that chatmessage collection with document id is userid
     *
     * @param userModel
     * @param doctorId1
     */
    private void registerToChat(UserModel userModel, String doctorId1) {
        chatCollection.document(doctorId1).set(userModel).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(DoctorFormPage.this, "Message feature Activated", Toast.LENGTH_SHORT).show();
                    finish();
                    startActivity(new Intent(DoctorFormPage.this, DoctorLogin.class));
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(DoctorFormPage.this, "Failed to activate chat feature", Toast.LENGTH_SHORT).show();
                Log.e("FAILURETOCHAT", e.getMessage());
            }
        });

    }



    /**
     * When this method calls then get the coordinates
     */
    public void retriveLocation() {
        if (ActivityCompat.checkSelfPermission(this, LOCATION_PERMISSION) == PackageManager.PERMISSION_GRANTED) {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5, this);
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {
                String lat = String.valueOf(location.getLatitude());
                String longit = String.valueOf(location.getLongitude());
                coordinates = new Coordinates(lat, longit);
            }
        } else {
            requestRuntimePermission();
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
    }

    @Override
    protected void onStart() {
        super.onStart();
        requestRuntimePermission();
        retriveLocation();
    }
}


