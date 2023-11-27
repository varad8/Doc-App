package com.vrnitsolution.healthapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.vrnitsolution.healthapp.R;
import com.vrnitsolution.healthapp.model.Prescription;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class PrescriptionHistoryAdapter extends RecyclerView.Adapter<PrescriptionHistoryAdapter.AppointmentViewHolder> {

    private List<Prescription> prescriptionList;
    private Context context;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(Prescription prescription);
    }

    public PrescriptionHistoryAdapter(Context context, List<Prescription> prescriptionList) {
        this.context = context;
        this.prescriptionList = prescriptionList;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    @NonNull
    @Override
    public AppointmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_prescriptionhistory_card, parent, false);
        return new AppointmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppointmentViewHolder holder, int position) {
        Prescription prescription = prescriptionList.get(position);
        SimpleDateFormat dateFormat = new SimpleDateFormat("d MMMM yyyy 'at' hh:mm a", Locale.getDefault());
        String formattedDate = dateFormat.format(prescription.getPrescriptionissueddate());

        holder.prescriptionIssuedDateTextView.setText("Prescription Issued Date: " + formattedDate);
        holder.appointmentIdTextView.setText("Appointment ID: " + prescription.getAppointmentId());
        holder.patientNameTextView.setText("Patient Name: " + prescription.getPatientName());
        holder.patientMobileNo.setText("Patient Mobile No: " + prescription.getPatientMobileNo());

        holder.itemView.setOnClickListener(view -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(prescription);
            }
        });
    }

    @Override
    public int getItemCount() {
        return prescriptionList.size();
    }

    public static class AppointmentViewHolder extends RecyclerView.ViewHolder {
        TextView appointmentIdTextView, patientNameTextView, prescriptionIssuedDateTextView;
        TextView patientMobileNo;

        public AppointmentViewHolder(@NonNull View itemView) {
            super(itemView);
            appointmentIdTextView = itemView.findViewById(R.id.appointmentid);
            patientNameTextView = itemView.findViewById(R.id.patientName);
            patientMobileNo = itemView.findViewById(R.id.patientMobileNo);
            prescriptionIssuedDateTextView = itemView.findViewById(R.id.issueddatae);
        }
    }
}
