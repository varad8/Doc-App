package com.vrnitsolution.healthapp.GetNotification.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.Timestamp;
import com.vrnitsolution.healthapp.GetNotification.model.NotificationModel;
import com.vrnitsolution.healthapp.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {
    private ArrayList<NotificationModel> notificationModels;
    private Context context;
    private OnNotificationClickListner onNotificationClickListner;

    public NotificationAdapter(ArrayList<NotificationModel> notificationModels, Context context, OnNotificationClickListner onNotificationClickListner) {
        this.notificationModels = notificationModels;
        this.context = context;
        this.onNotificationClickListner = onNotificationClickListner;
    }

    @NonNull
    @Override
    public NotificationAdapter.NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_layout, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationAdapter.NotificationViewHolder holder, int position) {
        NotificationModel model=notificationModels.get(position);

        Glide.with(context).load(model.getDcPhoto()).into(holder.profileImage);
        holder.doctorId.setText("Sender Id:"+model.getSenderDoctorId().substring(0,20)+"..");
        holder.message.setText("Title: "+model.getTitle()+"\n"+"Message: "+model.getMessage());
        holder.timestamp.setText("Created Date :"+formatTimestamp(model.getCreatedAt()));
        holder.doctorName.setText("Dr. "+model.getSentBy());

        if (!model.getNotificationImage().isEmpty() && model.getNotificationImage() !=null)
        {
            Glide.with(context).load(model.getNotificationImage()).into(holder.messageimage);
        }
        else
        {
            holder.messageimage.setVisibility(View.GONE);
        }



        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onNotificationClickListner != null) {
                    onNotificationClickListner.onNotificationClick(position,model);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return notificationModels.size();
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView doctorName,doctorId,message,timestamp;
        CircleImageView profileImage;
        ImageView messageimage;
        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            doctorName=itemView.findViewById(R.id.doctorname);
            doctorId=itemView.findViewById(R.id.senderId);
            profileImage=itemView.findViewById(R.id.circleImageView4);
            messageimage=itemView.findViewById(R.id.messageimage);
            message=itemView.findViewById(R.id.message);
            timestamp=itemView.findViewById(R.id.timestamp);
        }
    }

    public interface OnNotificationClickListner {
        void onNotificationClick(int position, NotificationModel model);
    }

    private String formatTimestamp(Timestamp timestamp) {
        return new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault()).format(timestamp.toDate());
    }
}
