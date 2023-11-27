package com.vrnitsolution.healthapp.Message.adapter;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.vrnitsolution.healthapp.Message.model.UserModel;
import com.vrnitsolution.healthapp.R;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class SearchUserRecyclerAdapter extends RecyclerView.Adapter<SearchUserRecyclerAdapter.UserModelViewHolder> {
    private ArrayList<UserModel> userModels;
    Context context;
    private OnSearchUserClickListner onSearchUserClickListner;

    public SearchUserRecyclerAdapter(ArrayList<UserModel> userModels, Context context, OnSearchUserClickListner onSearchUserClickListner) {
        this.userModels = userModels;
        this.context = context;
        this.onSearchUserClickListner = onSearchUserClickListner;
    }

    @NonNull
    @Override
    public SearchUserRecyclerAdapter.UserModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.profile_layout, parent, false);
        return new UserModelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchUserRecyclerAdapter.UserModelViewHolder holder, int position) {
        UserModel model = userModels.get(position);

        FirebaseAuth mauth=FirebaseAuth.getInstance();
        SharedPreferences preferences = context.getSharedPreferences("DoctorPrefs", MODE_PRIVATE);
        if (preferences != null) {
            // Retrieve doctor data from SharedPreferences
           String userid = preferences.getString("doctorId", "");
            if (userid != null) {
                if (userid.equals(model.getUserId()))
                {
                    holder.usernameText.setText(model.getUsername()+"(Me)");
                }
                else {
                    holder.usernameText.setText(model.getUsername());
                }

            } else if (mauth.getCurrentUser() !=null){
                String uid= mauth.getUid();
                if (uid.equals(model.getUserId()))
                {
                    holder.usernameText.setText(model.getUsername()+" (Me)");
                }
                else
                {
                    holder.usernameText.setText(model.getUsername());
                }
            }
        }

        holder.textEmail.setText(model.getEmail());


        Glide.with(context).load(model.getPhotoUrl()).into(holder.profilePic);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onSearchUserClickListner != null) {
                    onSearchUserClickListner.onUserClickListner(position);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return userModels.size();
    }

    class UserModelViewHolder extends RecyclerView.ViewHolder {
        TextView usernameText;
        TextView textEmail;
        CircleImageView profilePic;

        public UserModelViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameText = itemView.findViewById(R.id.textName);
            textEmail = itemView.findViewById(R.id.textEmail);
            profilePic = itemView.findViewById(R.id.profile);
        }

    }

    public interface OnSearchUserClickListner {
        void onUserClickListner(int position);
    }
}
