package com.vrnitsolution.healthapp.DoctorUI.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.vrnitsolution.healthapp.DoctorUI.model.Dosage;
import com.vrnitsolution.healthapp.DoctorUI.model.ProfileForMessage;
import com.vrnitsolution.healthapp.R;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class PatientProfileAdapter extends RecyclerView.Adapter<PatientProfileAdapter.PatientViewHolder> {
    private ArrayList<ProfileForMessage> profileForMessages;
    private Context context;
    private OnProfileClickListner onProfileClickListner;

    public PatientProfileAdapter(ArrayList<ProfileForMessage> profileForMessages, Context context, OnProfileClickListner onProfileClickListner) {
        this.profileForMessages = profileForMessages;
        this.context = context;
        this.onProfileClickListner = onProfileClickListner;
    }

    @NonNull
    @Override
    public PatientProfileAdapter.PatientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.profile_layout, parent, false);
        return new PatientProfileAdapter.PatientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PatientProfileAdapter.PatientViewHolder holder, int position) {
        ProfileForMessage profile = profileForMessages.get(position);


        holder.textName.setText(profile.getUsername());
        holder.textEmial.setText(profile.getEmail());

        // Check if profileImage is not null before loading it with Glide
        if (profile.getProfileUrl() != null && !profile.getProfileUrl().isEmpty()) {
            String profileImage = profile.getProfileUrl();
            Glide.with(context).load(profileImage).into(holder.profile);
        } else {
            Glide.with(context).load(R.drawable.usericon).into(holder.profile);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onProfileClickListner != null) {
                    onProfileClickListner.onProfileClicks(position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return profileForMessages.size();
    }

    static  class PatientViewHolder extends RecyclerView.ViewHolder {
        CircleImageView profile;
        TextView textName,textEmial;
        public PatientViewHolder(@NonNull View itemView) {
            super(itemView);
            profile=itemView.findViewById(R.id.profile);
            textName=itemView.findViewById(R.id.textName);
            textEmial=itemView.findViewById(R.id.textEmail);
        }
    }


    public interface OnProfileClickListner {
        void onProfileClicks(int position);
    }
}
