package com.vrnitsolution.healthapp.DoctorUI.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vrnitsolution.healthapp.DoctorUI.model.Dosage;
import com.vrnitsolution.healthapp.R;

import java.util.List;

import android.content.Context;


public class DosageAdapter extends RecyclerView.Adapter<DosageAdapter.ViewHolder> {

    private List<Dosage> dosageList;
    private OnDosageClickListener onDosageClickListener;
    private Context context;

    public DosageAdapter(List<Dosage> dosageList, OnDosageClickListener onDosageClickListener, Context context) {
        this.dosageList = dosageList;
        this.onDosageClickListener = onDosageClickListener;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dosage, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        Dosage dosage = dosageList.get(position);
        holder.dosageNameTextView.setText(dosage.getDosageName());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onDosageClickListener != null) {
                    onDosageClickListener.onDosageClick(position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return dosageList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView dosageNameTextView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            dosageNameTextView = itemView.findViewById(R.id.tvDosageName);
        }
    }

    public interface OnDosageClickListener {
        void onDosageClick(int position);
    }
}

