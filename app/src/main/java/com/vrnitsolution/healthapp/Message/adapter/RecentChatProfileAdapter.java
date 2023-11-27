
package com.vrnitsolution.healthapp.Message.adapter;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.vrnitsolution.healthapp.Message.model.ChatroomModel;
import com.vrnitsolution.healthapp.Message.model.UserModel;
import com.vrnitsolution.healthapp.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class RecentChatProfileAdapter extends RecyclerView.Adapter<RecentChatProfileAdapter.ChatroomModelViewHolder> {
    private ArrayList<ChatroomModel> ChatroomModels;
    Context context;
    private OnResentUserClickListner OnResentUserClickListner;

    public RecentChatProfileAdapter(ArrayList<ChatroomModel> ChatroomModels, Context context, OnResentUserClickListner OnResentUserClickListner) {
        this.ChatroomModels = ChatroomModels;
        this.context = context;
        this.OnResentUserClickListner = OnResentUserClickListner;
    }

    @NonNull
    @Override
    public RecentChatProfileAdapter.ChatroomModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recent_chat_profile, parent, false);
        return new ChatroomModelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecentChatProfileAdapter.ChatroomModelViewHolder holder, int position) {
        ChatroomModel model = ChatroomModels.get(position);
        // Use addSnapshotListener for real-time updates
        getOtherUserFormChatroom(model.getUserIds()).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                    if (error != null) {
                        Log.e("Firestore", "Listen failed", error);
                        return;
                    }

                    if (value != null && value.exists()) {
                        UserModel otherUserModel = value.toObject(UserModel.class);

                        boolean lastMessageSentByMe = model.getLastMessageSenderId().equals(getActiveUser());

                        if (lastMessageSentByMe) {
                            holder.lastmessage.setText("You: " + model.getLastMessage());
                        } else {
                            holder.lastmessage.setText(model.getLastMessage());
                        }

                        holder.usernameText.setText(otherUserModel.getUsername());
                        holder.textLastMessagetime.setText(timestampToString(model.getLastMessageTimestamp()));
                        Glide.with(context).load(otherUserModel.getPhotoUrl()).into(holder.profilePic);

                        holder.itemView.setOnClickListener(v -> {
                            if (OnResentUserClickListner != null) {
                                OnResentUserClickListner.onRecentUserClick(position, otherUserModel);
                            }
                        });
                    }


            }
        });


    }

    @Override
    public int getItemCount() {
        return ChatroomModels.size();
    }

    class ChatroomModelViewHolder extends RecyclerView.ViewHolder {
        TextView usernameText;
        TextView lastmessage;
        CircleImageView profilePic;
        TextView textLastMessagetime;

        public ChatroomModelViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameText = itemView.findViewById(R.id.textName);
            lastmessage = itemView.findViewById(R.id.textEmail);
            profilePic = itemView.findViewById(R.id.profile);
            textLastMessagetime = itemView.findViewById(R.id.textLastMessagetime);
        }

    }

    public interface OnResentUserClickListner {
        void onRecentUserClick(int position,UserModel userModel);
    }

    /**
     * Return the Active user
     *
     * @return
     */
    public String getActiveUser() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        SharedPreferences preferences = context.getSharedPreferences("DoctorPrefs", MODE_PRIVATE);
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
     * Check that userIds in that collection and get that document from collection
     * @param userIds
     * @return
     */
    public DocumentReference getOtherUserFormChatroom(List<String> userIds) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        if (userIds.get(0).equals(getActiveUser())) {
            return db.collection("chatusers").document(userIds.get(1));
        } else {
            return db.collection("chatusers").document(userIds.get(0));
        }
    }


    /**
     * It get's timestamp and return the String fomatated date (12 hours format | 11:00 pm)
     * @param timestamp
     * @return
     */
    public String timestampToString(Timestamp timestamp) {
        return new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(timestamp.toDate());
    }

}
