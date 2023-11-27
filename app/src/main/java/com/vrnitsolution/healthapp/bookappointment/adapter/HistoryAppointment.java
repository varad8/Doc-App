package com.vrnitsolution.healthapp.bookappointment.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;

import com.vrnitsolution.healthapp.AppointmentDetailsPage;
import com.vrnitsolution.healthapp.DoctorUI.AppointmentViewDoctor;
import com.vrnitsolution.healthapp.R;
import com.vrnitsolution.healthapp.bookappointment.model.Patient;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class HistoryAppointment extends RecyclerView.Adapter<HistoryAppointment.HistoryViewHolder> {
    Context context;
    ArrayList<Patient> patients;
    private OnAppointmentItemClickListner onAppointmentItemClickListner;

    public HistoryAppointment(Context context, ArrayList<Patient> patients, OnAppointmentItemClickListner onAppointmentItemClickListner) {
        this.context = context;
        this.patients = patients;
        this.onAppointmentItemClickListner = onAppointmentItemClickListner;
    }

    @NonNull
    @Override
    public HistoryAppointment.HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v= LayoutInflater.from(context).inflate(R.layout.item_appointment,parent,false);
        return new HistoryViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryAppointment.HistoryViewHolder holder, int position) {
        Patient patient=patients.get(position);

        // Format scheduleTime to the desired format
        SimpleDateFormat dateFormat = new SimpleDateFormat("d MMMM yyyy 'at' hh:mm a", Locale.getDefault());
        String formattedDate = dateFormat.format(patient.getScheduleTime().toDate());
        holder.scheduleTimeTextView.setText(formattedDate);

        holder.patientNameTextView.setText(patient.getPatientName());
        holder.scheduleTimeTextView.setText(formattedDate);


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onAppointmentItemClickListner != null) {
                    onAppointmentItemClickListner.onAppintmentItemClick(position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return patients.size();
    }

    public static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView patientNameTextView,scheduleTimeTextView;
        ImageView imageView;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            patientNameTextView = itemView.findViewById(R.id.textNamepateint);
            scheduleTimeTextView = itemView.findViewById(R.id.textPatinetSchedule);
        }
    }

    public interface OnAppointmentItemClickListner {
        void onAppintmentItemClick(int position);
    }
}
