package com.vrnitsolution.healthapp.Admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.vrnitsolution.healthapp.R;

public class AdminLogin extends AppCompatActivity {
    EditText email,password,key;
    FirebaseFirestore db=FirebaseFirestore.getInstance();
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

//        Window window = this.getWindow();
//        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        email=findViewById(R.id.adminEmail);
        password=findViewById(R.id.password);
        key=findViewById(R.id.key);


        progressDialog=new ProgressDialog(this);
        progressDialog.setMessage("Loading..");
        progressDialog.setCancelable(false);


    }


    /**
     * In that It checks the admin credentials are correct or not
     * @param view
     */
    public void adminLogin(View view) {
        String adminEmail=email.getText().toString().trim();
        String adminPassword=password.getText().toString().trim();
        String securityKey=key.getText().toString().trim();


        if (adminEmail.isEmpty() || adminPassword.isEmpty() || securityKey.isEmpty() )
        {
            displayToast("Enter valid credentials");
        }
        else
        {
            progressDialog.show();
            checkCredentials(adminEmail,adminPassword,securityKey);
        }
    }

    private void checkCredentials(String adminEmail, String adminPassword, String securityKey) {
        db.collection("adminuser")
                .whereEqualTo("email", adminEmail)
                .whereEqualTo("password", adminPassword)
                .whereEqualTo("secretKey", securityKey)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            progressDialog.dismiss();
                            // Check if there is any document matching the query
                            if (task.getResult() != null && !task.getResult().isEmpty()) {
                                // Assuming there is only one document matching the query
                                DocumentSnapshot document = task.getResult().getDocuments().get(0);

                                // Retrieve data from the document
                                String username = document.getString("username");
                                String profileUrl = document.getString("profileUrl");
                                String accountType = document.getString("accountType");

                                // Save data in SharedPreferences
                                saveDataInSharedPreferences(username, adminEmail, profileUrl, accountType);
                            } else {
                                progressDialog.dismiss();
                                // Handle the case where no matching document is found
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle failure
                    }
                });
    }

    /**
     * This method saves the data if admin login successfully to the account other it will not save (This used for when user login then it not logout when destory app)
     * @param username
     * @param email
     * @param profileUrl
     * @param accountType
     */
    private void saveDataInSharedPreferences(String username, String email, String profileUrl, String accountType) {
        SharedPreferences sharedPreferences = getSharedPreferences("adminData", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Save data in SharedPreferences
        editor.putString("username", username);
        editor.putString("email", email);
        editor.putString("profileUrl", profileUrl);
        editor.putString("accountType", accountType);

        // Apply the changes
        editor.apply();


        startActivity(new Intent(this,AdminDashboard.class));
    }


    public  void displayToast(String str)
    {
        Toast.makeText(this, ""+str, Toast.LENGTH_SHORT).show();
    }


    /**
     * When the admin open again the app it will check user already logged in or not if logged in it will redirect to Admin Dashboard
     */
    private void retrieveDataFromSharedPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("adminData", MODE_PRIVATE);

        // Retrieve data from SharedPreferences
        String savedUsername = sharedPreferences.getString("username", "");
        String savedEmail = sharedPreferences.getString("email", "");
        String savedProfileUrl = sharedPreferences.getString("profileUrl", "");
        String savedAccountType = sharedPreferences.getString("accountType", "");


        if (sharedPreferences!=null)
        {
            if (!savedEmail.isEmpty())
            {
                startActivity(new Intent(this,AdminDashboard.class));
                finish();
            }
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        retrieveDataFromSharedPreferences();
    }
}