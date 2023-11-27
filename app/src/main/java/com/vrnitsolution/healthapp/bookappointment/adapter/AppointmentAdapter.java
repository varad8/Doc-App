package com.vrnitsolution.healthapp.bookappointment.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.vrnitsolution.healthapp.AppointmentDetailsPage;
import com.vrnitsolution.healthapp.R;
import com.vrnitsolution.healthapp.bookappointment.model.Patient;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.AppointmentViewHolder> {
    Context context;
    ArrayList<Patient> patients;
    private OnApppointmentClickListner onApppointmentClickListner;

    public AppointmentAdapter(Context context, ArrayList<Patient> patients, OnApppointmentClickListner onApppointmentClickListner) {
        this.context = context;
        this.patients = patients;
        this.onApppointmentClickListner = onApppointmentClickListner;
    }

    @NonNull
    @Override
    public AppointmentAdapter.AppointmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v= LayoutInflater.from(context).inflate(R.layout.item_appointment_history,parent,false);
        return new AppointmentViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull AppointmentAdapter.AppointmentViewHolder holder, int position) {
        Patient patient = patients.get(position);
        holder.patientNameTextView.setText(patient.getPatientName());

        // Format scheduleTime to the desired format
        SimpleDateFormat dateFormat = new SimpleDateFormat("d MMMM yyyy 'at' hh:mm a", Locale.getDefault());
        String formattedDate = dateFormat.format(patient.getScheduleTime().toDate());
        holder.scheduleTimeTextView.setText(formattedDate);


        // Get current timestamp
        Timestamp currentTimestamp = new Timestamp(new Date());

        // Compare scheduleTime with the current timestamp
        if (patient.getScheduleTime().compareTo(currentTimestamp) < 0) {
            // Set appropriate image for past appointments
            holder.imageviewline.setImageResource(R.drawable.line3);
        } else {
            // Set appropriate image for upcoming appointments
            holder.imageviewline.setImageResource(R.drawable.line1);
        }




        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onApppointmentClickListner != null) {
                    onApppointmentClickListner.onAppointmentClick(position);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return patients.size();
    }

    public static class AppointmentViewHolder extends RecyclerView.ViewHolder {
        private TextView patientNameTextView;
        private TextView scheduleTimeTextView;
        ImageView imageviewline;
        public AppointmentViewHolder(@NonNull View itemView) {
            super(itemView);
            patientNameTextView = itemView.findViewById(R.id.textNamepateint);
            scheduleTimeTextView = itemView.findViewById(R.id.textPatinetSchedule);
            imageviewline=itemView.findViewById(R.id.imageline);
        }
    }

    public interface OnApppointmentClickListner {
        void onAppointmentClick(int position);
    }
}
