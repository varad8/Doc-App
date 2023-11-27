package com.vrnitsolution.healthapp.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.vrnitsolution.healthapp.R;
import com.vrnitsolution.healthapp.model.DosageUser;

import java.util.List;

public class DosageAdapter extends RecyclerView.Adapter<DosageAdapter.ViewHolder> {
    private List<DosageUser> dosageList;

    public DosageAdapter(List<DosageUser> dosageList) {
        this.dosageList = dosageList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dosagelist, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DosageUser dosage = dosageList.get(position);

        // Set TextView values here
        holder.textViewDosageAfterMill.setText("Dosage After Mill"+dosage.getDosageAfterMill());
        holder.textViewDosageName.setText("Dosage Name: "+dosage.getDosageName());
        holder.textViewDosageRemark.setText("Dosage Remark: "+dosage.getDosageRemark());
        holder.textViewDosageTime.setText("Dosage Time :"+dosage.getDosageTime());
    }

    @Override
    public int getItemCount() {
        return dosageList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewDosageAfterMill;
        TextView textViewDosageName;
        TextView textViewDosageRemark;
        TextView textViewDosageTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewDosageAfterMill = itemView.findViewById(R.id.textViewDosageAfterMill);
            textViewDosageName = itemView.findViewById(R.id.textViewDosageName);
            textViewDosageRemark = itemView.findViewById(R.id.textViewDosageRemark);
            textViewDosageTime = itemView.findViewById(R.id.textViewDosageTime);
        }
    }
}
