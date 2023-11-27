package com.vrnitsolution.healthapp.Message;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.vrnitsolution.healthapp.DoctorUI.DoctorLogin;
import com.vrnitsolution.healthapp.DoctorUI.IntroActivity;
import com.vrnitsolution.healthapp.Message.adapter.ChatRecyclerAdapter;
import com.vrnitsolution.healthapp.Message.model.ChatMessageModel;
import com.vrnitsolution.healthapp.Message.model.ChatroomModel;
import com.vrnitsolution.healthapp.R;
import com.vrnitsolution.healthapp.model.Prescription;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Arrays;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageActivity extends AppCompatActivity {
    String otheruserid, username, email, accountType, fcmtoken;
    CircleImageView profileImage;
    TextView fullname, acconttype;
    String activeuser, chatroomId;
    ChatroomModel chatroomModel;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    EditText messagetxt;
    ImageView sendBtn;
    RecyclerView chatRecyclerview;
    ArrayList<ChatMessageModel> chatMessageModels;
    CircleImageView circleImageView2;
    private ChatRecyclerAdapter chatRecyclerAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);


//        Window window = this.getWindow();
//        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        profileImage = findViewById(R.id.circleImageView);
        fullname = findViewById(R.id.fullname);
        acconttype = findViewById(R.id.online);
        sendBtn=findViewById(R.id.sendBtn);
        messagetxt=findViewById(R.id.messagetxt);
        chatRecyclerview=findViewById(R.id.chattingRecylerView);
        circleImageView2=findViewById(R.id.circleImageView2);


        circleImageView2.setOnClickListener(view -> {
            finish();
        });

        chatRecyclerview.setHasFixedSize(true);
        chatRecyclerview.setLayoutManager(new LinearLayoutManager(MessageActivity.this,LinearLayoutManager.VERTICAL,false));
        chatMessageModels=new ArrayList<>();
        chatRecyclerAdapter=new ChatRecyclerAdapter(MessageActivity.this,chatMessageModels);
        chatRecyclerview.setAdapter(chatRecyclerAdapter);



        Intent intent = getIntent();

        if (intent != null) {
            otheruserid = intent.getStringExtra("userid");
            username = intent.getStringExtra("username");
            email = intent.getStringExtra("email");
            accountType = intent.getStringExtra("accountType");
            fcmtoken = intent.getStringExtra("fcmtoken");

            fullname.setText(username);
            acconttype.setText(accountType);
            activeuser = getActiveUser();

            if (!activeuser.isEmpty()) {
                chatroomId = getChatRoomId(activeuser, otheruserid);
                getOrCreateChatRoomModel(chatroomId);
                setChatToRecyclerView(chatroomId);
            }
            Glide.with(MessageActivity.this).load(intent.getStringExtra("profileImage")).into(profileImage);


        }
        sendBtn.setOnClickListener(v->{
            String message=messagetxt.getText().toString().trim();

            if (!message.isEmpty())
            {
                sendBtn.setClickable(false);
                sendMessageToUser(message,activeuser);
            }
            else
            {
                displayToast("type message");
            }

        });
    }

    /**
     * According to that chatroomid check that in present in firestore database and set the adadpter and show chats
     * @param chatroomId
     */
    private void setChatToRecyclerView(String chatroomId) {
        // Clear the existing data to avoid duplication
        chatMessageModels.clear();

        // Fetch the latest data from Firestore
        db.collection("chatrooms").document(chatroomId).collection("chats").orderBy("timestamp")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {

                        Log.e("Firestore error", error.getMessage());
                        return;
                    }

                    if (chatMessageModels!=null)
                    {
                        chatMessageModels.clear();
                    }

                    // Check if there are any new documents
                    if (value != null && !value.isEmpty()) {
                        for (DocumentSnapshot document : value.getDocuments()) {
                            // Convert each document to a ChatMessageModel
                            ChatMessageModel messageModel = document.toObject(ChatMessageModel.class);
                            if (messageModel != null) {
                                // Add the message to the list
                                chatMessageModels.add(messageModel);
                            }
                        }

                        // Notify the adapter that the data has changed
                        chatRecyclerAdapter.notifyDataSetChanged();
                        chatRecyclerview.smoothScrollToPosition(chatMessageModels.size());
                    }
                });
    }


    /**
     * When this method calls it will save the message to the firestore collection
     * @param message
     * @param activeUser
     */
    private void sendMessageToUser(String message, String activeUser) {
        chatroomModel.setLastMessageTimestamp(Timestamp.now());
        chatroomModel.setLastMessageSenderId(activeUser);
        chatroomModel.setLastMessage(message);
        db.collection("chatrooms").document(chatroomId).set(chatroomModel);

        ChatMessageModel chatMessageModel = new ChatMessageModel(message, activeUser, Timestamp.now());

        db.collection("chatrooms").document(chatroomId).collection("chats").add(chatMessageModel).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                messagetxt.setText("");
                sendBtn.setClickable(true);

                // Play a sound when the message is sent
                playMessageSentSound();
            }
        });
    }

    /**
     * When message sent then it will play mp3 sound
     */
    private void playMessageSentSound() {
        // Use MediaPlayer to play the sound
        MediaPlayer mediaPlayer = MediaPlayer.create(MessageActivity.this, R.raw.sentmessage); // R.raw.message_sent_sound is the ID of your sound file
        mediaPlayer.start();

        // Release the MediaPlayer when the sound finishes playing
        mediaPlayer.setOnCompletionListener(mp -> {
            mp.release();
        });
    }


    /**
     * Here we getting the chatrooom or creating chatroom model for that users
     *
     * @param chatroomId In that when chatroom model not exitst then create new model im not set listner if you want uncomment the code of listner
     */
    private void getOrCreateChatRoomModel(String chatroomId) {
        db.collection("chatrooms").document(chatroomId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                chatroomModel = task.getResult().toObject(ChatroomModel.class);
                if (chatroomModel == null) {
                    chatroomModel = new ChatroomModel(chatroomId, Arrays.asList(activeuser, otheruserid), Timestamp.now(), "");
                }

                db.collection("chatrooms").document(chatroomId).set(chatroomModel);
                //                        .addOnCompleteListener(task1 -> {
                //                    if (task.isSuccessful())
                //                    {
                //                        displayToast("Chat Room activated");
                //                    }
                //                }).addOnFailureListener(exception->{
                //                    displayToast(exception.getMessage());
                //                });
            }
        });
    }


    /**
     * Get Unique ChatroomId according to the userid
     *
     * @param userid1
     * @param userid2
     * @return
     */
    public static String getChatRoomId(String userid1, String userid2) {
        if (userid1.hashCode() < userid2.hashCode()) {
            return userid1 + "_" + userid2;
        } else {
            return userid2 + "_" + userid1;
        }

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
     * Using this method displaying toast messages
     *
     * @param txt
     */

    public void displayToast(String txt) {
        Toast.makeText(this, "" + txt, Toast.LENGTH_SHORT).show();
    }

}